package com.example.data.api

import com.example.data.models.APIMatch
import com.example.data.models.Sport
import com.example.data.models.Stream
import retrofit2.http.GET
import retrofit2.http.Path

interface StreamedApiService {

    @GET("/api/matches/live")
    suspend fun getLiveMatches(): List<APIMatch>

    @GET("/api/matches/all-today")
    suspend fun getTodayMatches(): List<APIMatch>

    @GET("/api/matches/{sport}")
    suspend fun getMatchesBySport(@Path("sport") sport: String): List<APIMatch>

    @GET("/api/sports")
    suspend fun getSports(): List<Sport>

    @GET("/api/stream/{source}/{id}")
    suspend fun getStreams(
        @Path("source") source: String,
        @Path("id") id: String
    ): List<Stream>
}
