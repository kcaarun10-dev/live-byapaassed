package com.example.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.player.PlaybackState
import com.example.player.VideoQuality
import com.example.ui.components.GlassErrorState
import com.example.ui.components.LiveBadge
import com.example.ui.components.SoftGlassSurface
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassLiveRed
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteFaint
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    embedUrl: String,
    matchTitle: String,
    isHd: Boolean = true,
    posterUrl: String? = null,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val playerManager = remember(embedUrl, isHd) {
        com.example.player.StreamPlayerManager(
            context = context.applicationContext,
            coroutineScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main),
            isHdStream = isHd
        ).apply {
            startPlayback(embedUrl)
        }
    }

    val playbackState by playerManager.playbackState.collectAsState()
    val availableQualities by playerManager.availableQualities.collectAsState()
    val selectedQuality by playerManager.selectedQuality.collectAsState()
    val isLiveStream by playerManager.isLiveStream.collectAsState()
    val isFirstFrameRendered by playerManager.isFirstFrameRendered.collectAsState()

    var isPlaying by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var showQualitySheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableFloatStateOf(1.0f) }
    var isLowLatency by remember { mutableStateOf(true) }

    // DVR & Progress state
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var bufferedPositionMs by remember { mutableLongStateOf(0L) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    var resizeModeIndex by remember { mutableIntStateOf(0) } // 0: FIT, 1: ZOOM, 2: FILL
    val resizeModes = listOf(
        AspectRatioFrameLayout.RESIZE_MODE_FIT,
        AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
        AspectRatioFrameLayout.RESIZE_MODE_FILL
    )
    val resizeLabels = listOf("Fit", "Zoom", "Stretch")

    // Polling player state every 500ms
    LaunchedEffect(playerManager.player) {
        while (true) {
            val p = playerManager.player
            isPlaying = p.isPlaying
            if (!isDraggingSlider) {
                currentPositionMs = p.currentPosition
                durationMs = if (p.duration > 0) p.duration else 0L
                bufferedPositionMs = p.bufferedPosition
            }
            delay(500)
        }
    }

    // Handle Back Press
    BackHandler {
        if (isLandscape) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            isLandscape = false
        } else {
            onBackClick()
        }
    }

    // Keep screen on & immersive window setup
    DisposableEffect(activity) {
        val window = activity?.window
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            if (window != null) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                WindowCompat.setDecorFitsSystemWindows(window, true)
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            }
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            playerManager.release()
        }
    }

    // Auto-hide controls after 4.5 seconds of inactivity when playing
    LaunchedEffect(showControls, isPlaying, showQualitySheet, showSettingsSheet, showDiagnosticsDialog) {
        if (showControls && isPlaying && !showQualitySheet && !showSettingsSheet && !showDiagnosticsDialog) {
            delay(4500)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    },
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < width / 2) {
                            // Seek backward 10s
                            val p = playerManager.player
                            val newPos = (p.currentPosition - 10000).coerceAtLeast(0)
                            p.seekTo(newPos)
                        } else {
                            // Seek forward 10s
                            val p = playerManager.player
                            val newPos = p.currentPosition + 10000
                            p.seekTo(newPos)
                        }
                    }
                )
            }
    ) {
        // Native ExoPlayer Video Surface
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("player_view"),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    keepScreenOn = true
                    resizeMode = resizeModes[resizeModeIndex]
                    player = playerManager.player
                }
            },
            update = { playerView ->
                playerView.player = playerManager.player
                playerView.resizeMode = resizeModes[resizeModeIndex]
            }
        )

        // Poster / Skeleton Warmup Layer (Prevents black screen while stream connects)
        AnimatedVisibility(
            visible = !isFirstFrameRendered && playbackState !is PlaybackState.Error,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF08090C))
            ) {
                if (!posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.65f),
                                    GlassObsidian.copy(alpha = 0.9f),
                                    GlassMidnight
                                )
                            )
                        )
                )

                // Warmup Loader Card
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SoftGlassSurface(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = GlassObsidian.copy(alpha = 0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            val statusText = when (val state = playbackState) {
                                is PlaybackState.Loading -> state.message
                                is PlaybackState.Buffering -> state.message
                                is PlaybackState.Playing -> "Rendering stream…"
                                else -> "Connecting to live sports broadcast…"
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = matchTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassSilverMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isHd) {
                                    Surface(
                                        color = GlassIceBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassIceBlue)
                                    ) {
                                        Text(
                                            text = "HIGH RESOLUTION HD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GlassIceBlue,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = GlassWhiteStrong,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Ad-Bypassed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Error State Overlay
        if (playbackState is PlaybackState.Error) {
            val errorState = playbackState as PlaybackState.Error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                GlassErrorState(
                    title = "Stream Unavailable",
                    message = errorState.message,
                    onRetry = { playerManager.retry() },
                    onGoBack = onBackClick
                )
            }
        }

        // Soft Glass Custom Overlay Controls
        AnimatedVisibility(
            visible = showControls && playbackState !is PlaybackState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // TOP CONTROL BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("player_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Text(
                                text = matchTitle.ifBlank { "Live Sports Broadcast" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LiveBadge()
                                if (isHd) {
                                    Surface(
                                        color = GlassIceBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassIceBlue)
                                    ) {
                                        Text(
                                            text = "HD",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GlassIceBlue,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "AR SPORTS ExoPlayer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlassSilverMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Top Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Diagnostics button
                        IconButton(onClick = { showDiagnosticsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Stream Diagnostics",
                                tint = GlassSilverMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Settings button
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Player Settings",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // CENTER PLAY/PAUSE & SEEK BUTTONS
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    // Replay 10s
                    IconButton(
                        onClick = {
                            val p = playerManager.player
                            val newPos = (p.currentPosition - 10000).coerceAtLeast(0)
                            p.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Replay 10s",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Center Play / Pause toggle
                    Surface(
                        color = GlassObsidian.copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GlassBorderSilver),
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                if (playerManager.player.isPlaying) {
                                    playerManager.player.pause()
                                    isPlaying = false
                                } else {
                                    playerManager.player.play()
                                    isPlaying = true
                                }
                            }
                            .testTag("play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            val p = playerManager.player
                            val newPos = p.currentPosition + 10000
                            p.seekTo(newPos)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // BOTTOM FLOATING GLASS PLAYER CONTROL PANEL
                SoftGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = GlassObsidian.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // DVR Timeline Slider
                        if (durationMs > 0) {
                            val progress = if (isDraggingSlider) sliderPosition else (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                            Slider(
                                value = progress,
                                onValueChange = {
                                    isDraggingSlider = true
                                    sliderPosition = it
                                },
                                onValueChangeFinished = {
                                    val targetMs = (sliderPosition * durationMs).toLong()
                                    playerManager.player.seekTo(targetMs)
                                    isDraggingSlider = false
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.White,
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = GlassWhiteStrong
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                            )
                        }

                        // Time & Live Sync Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Position / Time
                            val timeStr = formatMs(if (isDraggingSlider) (sliderPosition * durationMs).toLong() else currentPositionMs)
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            // "GO LIVE" Prominent Silver Glass Button
                            Surface(
                                color = GlassWhiteStrong,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSilver),
                                modifier = Modifier
                                    .clickable { playerManager.seekToLiveEdge() }
                                    .testTag("go_live_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Go Live",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "GO LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Secondary Control Icons (Quality, Speed, Aspect, Orientation)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Quality Selector Pill
                                Surface(
                                    color = GlassWhiteSubtle,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorderSubtle),
                                    modifier = Modifier.clickable { showQualitySheet = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = selectedQuality.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                // Aspect ratio toggle
                                Surface(
                                    color = GlassWhiteSubtle,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorderSubtle),
                                    modifier = Modifier.clickable {
                                        resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                                    }
                                ) {
                                    Text(
                                        text = resizeLabels[resizeModeIndex],
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassSilverMuted,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                // Fullscreen / Orientation Toggle
                                IconButton(
                                    onClick = {
                                        if (isLandscape) {
                                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                            isLandscape = false
                                        } else {
                                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                            isLandscape = true
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Toggle Orientation",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Resolution Quality Picker Modal Bottom Sheet
        if (showQualitySheet) {
            QualityPickerBottomSheet(
                availableQualities = availableQualities,
                selectedQuality = selectedQuality,
                onQualitySelected = { quality ->
                    playerManager.setQuality(quality)
                },
                onDismiss = {
                    showQualitySheet = false
                }
            )
        }

        // Full Player Settings Bottom Sheet
        if (showSettingsSheet) {
            PlayerSettingsBottomSheet(
                currentQualityLabel = selectedQuality.label,
                currentSpeed = currentSpeed,
                isLowLatency = isLowLatency,
                onQualityClick = { showQualitySheet = true },
                onSpeedSelected = { speed ->
                    currentSpeed = speed
                    playerManager.player.setPlaybackSpeed(speed)
                },
                onPipClick = {
                    try {
                        activity?.enterPictureInPictureMode()
                    } catch (_: Exception) {}
                },
                onLowLatencyToggle = { isLowLatency = it },
                onDiagnosticsClick = { showDiagnosticsDialog = true },
                onDismiss = { showSettingsSheet = false }
            )
        }

        // Stream Diagnostics Dialog
        if (showDiagnosticsDialog) {
            val resStr = "${selectedQuality.width}x${selectedQuality.height}".takeIf { selectedQuality.width > 0 } ?: "1920x1080 (ABR)"
            val bitrateStr = if (selectedQuality.bitrate > 0) "${selectedQuality.bitrate / 1_000_000.0} Mbps" else "Adaptive High Bitrate"
            val liveOffset = (durationMs - currentPositionMs).coerceAtLeast(0L)

            StreamDiagnosticsDialog(
                resolution = resStr,
                bitrate = bitrateStr,
                liveOffsetMs = liveOffset,
                bufferedDurationMs = (bufferedPositionMs - currentPositionMs).coerceAtLeast(0L),
                isPlaying = isPlaying,
                isLowLatency = isLowLatency,
                onDismiss = { showDiagnosticsDialog = false }
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
