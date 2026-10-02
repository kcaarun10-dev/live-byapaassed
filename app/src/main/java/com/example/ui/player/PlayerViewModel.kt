package com.example.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.player.PlaybackState
import com.example.player.StreamPlayerManager
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(
    application: Application,
    private val embedUrl: String
) : AndroidViewModel(application) {

    private val playerManager = StreamPlayerManager(
        context = application.applicationContext,
        coroutineScope = viewModelScope
    )

    val playbackState: StateFlow<PlaybackState> = playerManager.playbackState

    val player = playerManager.player

    init {
        playerManager.startPlayback(embedUrl)
    }

    fun getPlayerManager(): StreamPlayerManager = playerManager

    fun retry() {
        playerManager.retry()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
