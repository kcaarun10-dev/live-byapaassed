package com.example.data.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Sport(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)

data class TeamInfo(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("badge")
    val badge: String? = null
)

data class MatchTeams(
    @SerializedName("home")
    val home: TeamInfo? = null,
    @SerializedName("away")
    val away: TeamInfo? = null
)

data class MatchSource(
    @SerializedName("source")
    val source: String = "",
    @SerializedName("id")
    val id: String = ""
)

data class APIMatch(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("date")
    val date: Long = 0L,
    @SerializedName("poster")
    val poster: String? = null,
    @SerializedName("popular")
    val popular: Boolean = false,
    @SerializedName("teams")
    val teams: MatchTeams? = null,
    @SerializedName("sources")
    val sources: List<MatchSource> = emptyList(),
    val forceLive: Boolean = false
) {
    val dateInMillis: Long
        get() = if (date in 1..99_999_999_999L) date * 1000L else date

    fun isLive(): Boolean {
        if (forceLive) return true
        if (date <= 0L) return true
        val now = System.currentTimeMillis()
        val matchTime = dateInMillis
        // Match started or within 5 min of starting, up to 4 hours after start
        return now >= matchTime - (5 * 60 * 1000L) && now <= matchTime + (4 * 60 * 60 * 1000L)
    }

    fun isUpcoming(): Boolean {
        if (forceLive) return false
        if (date <= 0L) return false
        val now = System.currentTimeMillis()
        val matchTime = dateInMillis
        return matchTime > now + (5 * 60 * 1000L)
    }

    fun getFormattedTime(): String {
        if (date <= 0L) return "LIVE"
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(dateInMillis))
        } catch (_: Exception) {
            "UPCOMING"
        }
    }
}

data class Stream(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("streamNo")
    val streamNo: Int = 1,
    @SerializedName("language")
    val language: String = "EN",
    @SerializedName("hd")
    val hd: Boolean = true,
    @SerializedName("embedUrl")
    val embedUrl: String = "",
    @SerializedName("source")
    val source: String = ""
)
