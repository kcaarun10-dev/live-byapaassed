package com.example.ui.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.APIMatch
import com.example.data.models.MatchSource
import com.example.data.models.Stream
import com.example.data.repository.StreamedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface StreamPickerUiState {
    object Idle : StreamPickerUiState
    object Loading : StreamPickerUiState
    data class StreamsLoaded(
        val source: MatchSource,
        val streams: List<Stream>
    ) : StreamPickerUiState
    data class Error(val message: String) : StreamPickerUiState
}

class StreamPickerViewModel(
    private val matchId: String,
    private val repository: StreamedRepository
) : ViewModel() {

    private val _match = MutableStateFlow<APIMatch?>(null)
    val match: StateFlow<APIMatch?> = _match.asStateFlow()

    private val _selectedSource = MutableStateFlow<MatchSource?>(null)
    val selectedSource: StateFlow<MatchSource?> = _selectedSource.asStateFlow()

    private val _uiState = MutableStateFlow<StreamPickerUiState>(StreamPickerUiState.Idle)
    val uiState: StateFlow<StreamPickerUiState> = _uiState.asStateFlow()

    init {
        loadMatchDetails()
    }

    private fun loadMatchDetails() {
        val cached = repository.getCachedMatch(matchId)
        if (cached != null) {
            _match.value = cached
            if (cached.sources.isNotEmpty()) {
                selectSource(cached.sources.first())
            }
        } else {
            // Fallback: refresh live matches to find the match
            viewModelScope.launch {
                _uiState.value = StreamPickerUiState.Loading
                repository.getLiveMatches().onSuccess { matches ->
                    val found = matches.find { it.id == matchId }
                    if (found != null) {
                        _match.value = found
                        if (found.sources.isNotEmpty()) {
                            selectSource(found.sources.first())
                        }
                    } else {
                        _uiState.value = StreamPickerUiState.Error("Match not found or expired.")
                    }
                }.onFailure {
                    _uiState.value = StreamPickerUiState.Error("Could not load match details.")
                }
            }
        }
    }

    fun selectSource(source: MatchSource) {
        _selectedSource.value = source
        viewModelScope.launch {
            _uiState.value = StreamPickerUiState.Loading
            val result = repository.getStreams(source = source.source, id = source.id)
            result.fold(
                onSuccess = { streamList ->
                    _uiState.value = StreamPickerUiState.StreamsLoaded(
                        source = source,
                        streams = streamList
                    )
                },
                onFailure = { error ->
                    val msg = when {
                        error is java.net.UnknownHostException || error is IOException ->
                            "No internet connection. Please check your network."
                        else -> error.message ?: "Failed to fetch stream links for this source."
                    }
                    _uiState.value = StreamPickerUiState.Error(msg)
                }
            )
        }
    }

    fun retry() {
        val currentSource = _selectedSource.value
        if (currentSource != null) {
            selectSource(currentSource)
        } else {
            loadMatchDetails()
        }
    }
}
