package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.StreamedApiService
import com.example.data.models.APIMatch
import com.example.data.models.Sport
import com.example.data.models.SportCategory
import com.example.data.models.Stream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StreamedRepository(
    private val api: StreamedApiService = ApiClient.apiService
) {
    // In-memory cache of matches to allow quick lookup when navigating to details
    private val matchCache = mutableMapOf<String, APIMatch>()
    private var cachedSports: List<Sport>? = null

    suspend fun getLiveMatches(): Result<List<APIMatch>> = withContext(Dispatchers.IO) {
        try {
            val matches = api.getLiveMatches()
            // Strictly filter to Football (Soccer) and Cricket only and mark live
            val filteredMatches = matches
                .filter { match -> SportCategory.isAllowedSport(match.category) }
                .map { it.copy(forceLive = true) }
            filteredMatches.forEach { matchCache[it.id] = it }
            Result.success(filteredMatches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayMatches(): Result<List<APIMatch>> = withContext(Dispatchers.IO) {
        try {
            val matches = api.getTodayMatches()
            // Strictly filter to Football (Soccer) and Cricket only
            val filteredMatches = matches.filter { match ->
                SportCategory.isAllowedSport(match.category)
            }
            filteredMatches.forEach { matchCache[it.id] = it }
            Result.success(filteredMatches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSports(): Result<List<Sport>> = withContext(Dispatchers.IO) {
        try {
            cachedSports?.let { return@withContext Result.success(it) }
            val sports = api.getSports()
            val filteredSports = sports.filter { sport ->
                SportCategory.isAllowedSport(sport.id) || SportCategory.isAllowedSport(sport.name)
            }
            cachedSports = filteredSports
            Result.success(filteredSports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchesBySport(sport: String): Result<List<APIMatch>> = withContext(Dispatchers.IO) {
        try {
            val matches = api.getMatchesBySport(sport)
            val filteredMatches = matches.filter { match ->
                SportCategory.isAllowedSport(match.category)
            }
            filteredMatches.forEach { matchCache[it.id] = it }
            Result.success(filteredMatches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStreams(source: String, id: String): Result<List<Stream>> = withContext(Dispatchers.IO) {
        try {
            val streams = api.getStreams(source, id)
            Result.success(streams)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCachedMatch(id: String): APIMatch? {
        return matchCache[id]
    }

    fun cacheMatch(match: APIMatch) {
        matchCache[match.id] = match
    }
}
