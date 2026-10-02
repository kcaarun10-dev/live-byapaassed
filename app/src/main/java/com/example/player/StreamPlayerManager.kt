package com.example.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cronet.CronetDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.extractor.ExtractedStream
import com.example.extractor.StreamExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.chromium.net.CronetEngine
import java.io.IOException
import java.util.concurrent.Executors

sealed interface PlaybackState {
    object Idle : PlaybackState
    data class Loading(val message: String) : PlaybackState
    data class Playing(val isLive: Boolean = true) : PlaybackState
    data class Buffering(val message: String = "Buffering stream…") : PlaybackState
    data class Error(val message: String, val canRetry: Boolean = true) : PlaybackState
}

class StreamPlayerManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val isHdStream: Boolean = false
) {
    private var cronetEngine: CronetEngine? = null
    private var currentEmbedUrl: String? = null
    private val streamExtractor = StreamExtractor(context)
    private val executorService = Executors.newSingleThreadExecutor()

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _availableQualities = MutableStateFlow<List<VideoQuality>>(emptyList())
    val availableQualities: StateFlow<List<VideoQuality>> = _availableQualities.asStateFlow()

    private val _selectedQuality = MutableStateFlow<VideoQuality>(VideoQuality.AUTO)
    val selectedQuality: StateFlow<VideoQuality> = _selectedQuality.asStateFlow()

    private val _isLiveStream = MutableStateFlow(true)
    val isLiveStream: StateFlow<Boolean> = _isLiveStream.asStateFlow()

    private val _isFirstFrameRendered = MutableStateFlow(false)
    val isFirstFrameRendered: StateFlow<Boolean> = _isFirstFrameRendered.asStateFlow()

    private var retryCount = 0
    private val MAX_RETRIES = 5
    private var loadJob: Job? = null

    // Eager player initialization ensures PlayerView always has a valid player instance
    // eliminating black screen delays when opening the player.
    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        // For HD streams, configure default track parameters to prefer highest bitrate
        if (isHdStream) {
            trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .setForceHighestSupportedBitrate(true)
                .build()
        }

        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_BUFFERING -> {
                        _playbackState.value = PlaybackState.Buffering("Buffering stream…")
                    }
                    Player.STATE_READY -> {
                        val isLive = isCurrentMediaItemLive
                        _isLiveStream.value = isLive
                        _playbackState.value = PlaybackState.Playing(isLive)
                    }
                    Player.STATE_ENDED -> {
                        _playbackState.value = PlaybackState.Idle
                    }
                    Player.STATE_IDLE -> {
                        // Player idle
                    }
                }
            }

            override fun onRenderedFirstFrame() {
                _isFirstFrameRendered.value = true
            }

            override fun onTracksChanged(tracks: Tracks) {
                val extractedQualities = VideoQuality.extractAvailableQualities(tracks)
                if (extractedQualities.isNotEmpty()) {
                    _availableQualities.value = extractedQualities

                    // If stream is labeled as HD and user hasn't explicitly picked a non-auto resolution,
                    // automatically lock to the highest resolution (1080p/720p)
                    if (isHdStream && _selectedQuality.value == VideoQuality.AUTO) {
                        val topHdQuality = extractedQualities.firstOrNull { it.height >= 720 }
                            ?: extractedQualities.firstOrNull()
                        if (topHdQuality != null) {
                            _selectedQuality.value = topHdQuality
                            applyQualityConstraints(topHdQuality)
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                handlePlaybackError(error)
            }
        })
    }

    init {
        initCronetEngine()
    }

    private fun initCronetEngine() {
        try {
            val providers = org.chromium.net.CronetProvider.getAllProviders(context)
            val provider = providers.firstOrNull { it.isEnabled } ?: providers.firstOrNull()
            cronetEngine = provider?.createBuilder()
                ?.enableHttp2(true)
                ?.enableQuic(true)
                ?.enableBrotli(true)
                ?.build()
                ?: CronetEngine.Builder(context)
                    .enableHttp2(true)
                    .enableQuic(true)
                    .enableBrotli(true)
                    .build()
        } catch (_: Exception) {
            try {
                cronetEngine = CronetEngine.Builder(context)
                    .enableHttp2(true)
                    .enableQuic(true)
                    .enableBrotli(true)
                    .build()
            } catch (_: Exception) {}
        }
    }

    fun startPlayback(embedUrl: String) {
        currentEmbedUrl = embedUrl
        retryCount = 0
        _isFirstFrameRendered.value = false
        loadJob?.cancel()
        loadJob = coroutineScope.launch {
            extractAndPlay(embedUrl, initialSeekMs = 0L)
        }
    }

    private suspend fun extractAndPlay(embedUrl: String, initialSeekMs: Long) {
        _playbackState.value = PlaybackState.Loading("Extracting ad-free stream…")
        try {
            val extracted = streamExtractor.extract(embedUrl)
            retryCount = 0 // Reset on successful extraction
            _playbackState.value = PlaybackState.Loading("Initializing native player…")
            setupPlayerWithStream(extracted, initialSeekMs)
        } catch (e: Exception) {
            val errorMsg = when {
                e is java.net.UnknownHostException || e is IOException ->
                    "No internet connection. Please check your network."
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Stream extraction timed out. Please retry."
                else ->
                    "Could not extract stream. Please retry."
            }
            _playbackState.value = PlaybackState.Error(errorMsg, canRetry = true)
        }
    }

    private fun setupPlayerWithStream(extracted: ExtractedStream, initialSeekMs: Long) {
        val engine = cronetEngine ?: run {
            initCronetEngine()
            cronetEngine ?: return
        }

        val dataSourceFactory = CronetDataSource.Factory(
            engine,
            executorService
        ).setDefaultRequestProperties(extracted.headers)

        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)
            .createMediaSource(MediaItem.fromUri(extracted.url))

        player.apply {
            setMediaSource(mediaSource)
            applyQualityConstraints(_selectedQuality.value)
            prepare()
            if (initialSeekMs > 0) {
                seekTo(initialSeekMs)
            }
            playWhenReady = true
        }
    }

    fun setQuality(quality: VideoQuality) {
        _selectedQuality.value = quality
        applyQualityConstraints(quality)
    }

    private fun applyQualityConstraints(quality: VideoQuality) {
        player.trackSelectionParameters = if (quality == VideoQuality.AUTO) {
            val builder = player.trackSelectionParameters
                .buildUpon()
                .clearVideoSizeConstraints()
            if (isHdStream) {
                builder.setForceHighestSupportedBitrate(true)
            }
            builder.build()
        } else {
            player.trackSelectionParameters
                .buildUpon()
                .setMaxVideoSize(quality.width, quality.height)
                .setMinVideoSize(quality.width, quality.height)
                .setForceHighestSupportedBitrate(isHdStream)
                .build()
        }
    }

    fun seekToLiveEdge() {
        player.seekToDefaultPosition()
        player.play()
    }

    private fun handlePlaybackError(error: PlaybackException) {
        val cause = error.cause
        var isForbidden = false
        var isNotFound = false

        if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS) {
            if (cause is HttpDataSource.InvalidResponseCodeException) {
                if (cause.responseCode == 403) isForbidden = true
                if (cause.responseCode == 404) isNotFound = true
            }
        }

        // Deep check for 403 or 404 in cause hierarchy
        var currentCause: Throwable? = cause
        while (currentCause != null) {
            if (currentCause is HttpDataSource.InvalidResponseCodeException) {
                if (currentCause.responseCode == 403) isForbidden = true
                if (currentCause.responseCode == 404) isNotFound = true
            }
            currentCause = currentCause.cause
        }

        if (isForbidden && currentEmbedUrl != null && retryCount < MAX_RETRIES) {
            retryCount++
            val currentPos = player.currentPosition
            _playbackState.value = PlaybackState.Loading("Token refreshed. Resuming stream ($retryCount/$MAX_RETRIES)…")
            loadJob?.cancel()
            loadJob = coroutineScope.launch {
                extractAndPlay(currentEmbedUrl!!, currentPos)
            }
            return
        }

        val displayMessage = when {
            isNotFound -> "Stream offline or not available yet."
            isForbidden -> "Stream token expired and retry limit reached."
            cause is java.net.UnknownHostException -> "No internet connection."
            else -> error.message ?: "Playback error occurred. Please retry."
        }

        _playbackState.value = PlaybackState.Error(displayMessage, canRetry = true)
    }

    fun retry() {
        val url = currentEmbedUrl ?: return
        startPlayback(url)
    }

    fun release() {
        loadJob?.cancel()
        loadJob = null
        player.stop()
        player.release()
    }
}
