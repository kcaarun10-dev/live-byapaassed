package com.example.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://streamed.pk"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: StreamedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StreamedApiService::class.java)
    }

    /**
     * Image URL helper for team badge.
     * Example: "https://streamed.pk/api/images/badge/{badge}.webp"
     */
    fun getBadgeUrl(badge: String?): String? {
        if (badge.isNullOrBlank()) return null
        if (badge.startsWith("http://") || badge.startsWith("https://")) return badge
        val cleanBadge = badge.removePrefix("/").removeSuffix(".webp")
        return "$BASE_URL/api/images/badge/$cleanBadge.webp"
    }

    /**
     * Image URL helper for match poster.
     * Example: "https://streamed.pk{poster}.webp"
     */
    fun getPosterUrl(poster: String?): String? {
        if (poster.isNullOrBlank()) return null
        if (poster.startsWith("http://") || poster.startsWith("https://")) return poster
        val cleanPoster = if (poster.startsWith("/")) poster else "/$poster"
        val fullPath = if (cleanPoster.endsWith(".webp") || cleanPoster.endsWith(".jpg") || cleanPoster.endsWith(".png")) {
            cleanPoster
        } else {
            "$cleanPoster.webp"
        }
        return "$BASE_URL$fullPath"
    }
}
