package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.APIMatch
import com.example.data.repository.StreamedRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(
        val liveMatches: List<APIMatch>,
        val todayMatches: List<APIMatch>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repository: StreamedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Initial user-facing load
        loadMatches(isSilent = false)

        // Secret background periodic refresh without disrupting user or showing loading spinners
        startSecretBackgroundRefresh()
    }

    private fun startSecretBackgroundRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(20_000L) // Refresh every 20 seconds silently
                loadMatches(isSilent = true)
            }
        }
    }

    fun loadMatches(isSilent: Boolean = false) {
        viewModelScope.launch {
            // Only display the full screen loading state on initial/manual load if no data exists
            if (!isSilent && _uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            try {
                val liveDeferred = async { repository.getLiveMatches() }
                val todayDeferred = async { repository.getTodayMatches() }

                val liveResult = liveDeferred.await()
                val todayResult = todayDeferred.await()

                val liveMatches = liveResult.getOrNull() ?: emptyList()
                val todayMatches = todayResult.getOrNull() ?: emptyList()

                if (liveMatches.isNotEmpty() || todayMatches.isNotEmpty()) {
                    _uiState.value = HomeUiState.Success(
                        liveMatches = liveMatches,
                        todayMatches = todayMatches
                    )
                } else if (!isSilent) {
                    if (liveResult.isFailure && todayResult.isFailure) {
                        val error = liveResult.exceptionOrNull() ?: todayResult.exceptionOrNull()
                        _uiState.value = HomeUiState.Error(mapErrorMessage(error ?: IOException("Failed to load matches")))
                    } else {
                        // Empty list response
                        _uiState.value = HomeUiState.Success(
                            liveMatches = emptyList(),
                            todayMatches = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                if (!isSilent && _uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error(mapErrorMessage(e))
                }
            }
        }
    }

    private fun mapErrorMessage(throwable: Throwable): String {
        return when {
            throwable is java.net.UnknownHostException || throwable is IOException ->
                "No internet connection. Please check your network."
            else -> throwable.message ?: "Failed to load matches. Please try again."
        }
    }
}
