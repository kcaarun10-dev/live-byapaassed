package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.models.APIMatch
import com.example.data.models.Stream
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WatchHistoryItem(
    val match: APIMatch,
    val stream: Stream? = null,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val positionMs: Long = 0L
)

class LocalDataManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ar_sports_local_db", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _favoritesFlow = MutableStateFlow<List<APIMatch>>(emptyList())
    val favoritesFlow: StateFlow<List<APIMatch>> = _favoritesFlow.asStateFlow()

    private val _historyFlow = MutableStateFlow<List<WatchHistoryItem>>(emptyList())
    val historyFlow: StateFlow<List<WatchHistoryItem>> = _historyFlow.asStateFlow()

    private val _remindersFlow = MutableStateFlow<Set<String>>(emptySet())
    val remindersFlow: StateFlow<Set<String>> = _remindersFlow.asStateFlow()

    private val _recentSearchesFlow = MutableStateFlow<List<String>>(emptyList())
    val recentSearchesFlow: StateFlow<List<String>> = _recentSearchesFlow.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        // Load Favorites
        val favsJson = prefs.getString("favorites_list", null)
        if (!favsJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<APIMatch>>() {}.type
                val list: List<APIMatch> = gson.fromJson(favsJson, type) ?: emptyList()
                _favoritesFlow.value = list
            } catch (_: Exception) {}
        }

        // Load History
        val histJson = prefs.getString("watch_history", null)
        if (!histJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<WatchHistoryItem>>() {}.type
                val list: List<WatchHistoryItem> = gson.fromJson(histJson, type) ?: emptyList()
                _historyFlow.value = list
            } catch (_: Exception) {}
        }

        // Load Reminders
        val rems = prefs.getStringSet("reminders_set", emptySet()) ?: emptySet()
        _remindersFlow.value = rems

        // Load Recent Searches
        val searchesJson = prefs.getString("recent_searches", null)
        if (!searchesJson.isNullOrBlank()) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val list: List<String> = gson.fromJson(searchesJson, type) ?: emptyList()
                _recentSearchesFlow.value = list
            } catch (_: Exception) {}
        }
    }

    // FAVORITES
    fun toggleFavorite(match: APIMatch) {
        val current = _favoritesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == match.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, match)
        }
        _favoritesFlow.value = current
        prefs.edit().putString("favorites_list", gson.toJson(current)).apply()
    }

    fun isFavorite(matchId: String): Boolean {
        return _favoritesFlow.value.any { it.id == matchId }
    }

    // WATCH HISTORY
    fun addToHistory(match: APIMatch, stream: Stream? = null, positionMs: Long = 0L) {
        val current = _historyFlow.value.toMutableList()
        current.removeAll { it.match.id == match.id }
        current.add(
            0,
            WatchHistoryItem(
                match = match,
                stream = stream,
                lastWatchedTimestamp = System.currentTimeMillis(),
                positionMs = positionMs
            )
        )
        // Keep max 50 items
        val trimmed = if (current.size > 50) current.take(50) else current
        _historyFlow.value = trimmed
        prefs.edit().putString("watch_history", gson.toJson(trimmed)).apply()
    }

    fun removeFromHistory(matchId: String) {
        val current = _historyFlow.value.toMutableList()
        current.removeAll { it.match.id == matchId }
        _historyFlow.value = current
        prefs.edit().putString("watch_history", gson.toJson(current)).apply()
    }

    fun clearHistory() {
        _historyFlow.value = emptyList()
        prefs.edit().remove("watch_history").apply()
    }

    // REMINDERS
    fun toggleReminder(matchId: String): Boolean {
        val current = _remindersFlow.value.toMutableSet()
        val added = if (current.contains(matchId)) {
            current.remove(matchId)
            false
        } else {
            current.add(matchId)
            true
        }
        _remindersFlow.value = current
        prefs.edit().putStringSet("reminders_set", current).apply()
        return added
    }

    fun hasReminder(matchId: String): Boolean {
        return _remindersFlow.value.contains(matchId)
    }

    // RECENT SEARCHES
    fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val current = _recentSearchesFlow.value.toMutableList()
        current.removeAll { it.equals(q, ignoreCase = true) }
        current.add(0, q)
        val trimmed = if (current.size > 15) current.take(15) else current
        _recentSearchesFlow.value = trimmed
        prefs.edit().putString("recent_searches", gson.toJson(trimmed)).apply()
    }

    fun removeRecentSearch(query: String) {
        val current = _recentSearchesFlow.value.toMutableList()
        current.removeAll { it.equals(query, ignoreCase = true) }
        _recentSearchesFlow.value = current
        prefs.edit().putString("recent_searches", gson.toJson(current)).apply()
    }

    fun clearRecentSearches() {
        _recentSearchesFlow.value = emptyList()
        prefs.edit().remove("recent_searches").apply()
    }
}
