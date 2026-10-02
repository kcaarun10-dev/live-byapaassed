package com.example.data.models

import com.google.gson.annotations.SerializedName

/**
 * Event Decoder Cricket Event Data Model
 * Source API: https://event-decoder-lite.lovable.app/api/public/streams
 * Identifies WHAT match is playing (Metadata source).
 */
data class EventDecoderCricketEvent(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("match_id")
    val matchId: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("tag")
    val tag: String = "UPCOMING",
    @SerializedName("source_tag")
    val sourceTag: String? = null,
    @SerializedName("poster")
    val poster: String? = null,
    @SerializedName("uri_name")
    val uriName: String? = null,
    @SerializedName("starts_at")
    val startsAt: String? = null,
    @SerializedName("ends_at")
    val endsAt: String? = null,
    @SerializedName("always_live")
    val alwaysLive: Boolean? = null,
    @SerializedName("locale")
    val locale: String? = null,
    @SerializedName("category_name")
    val categoryName: String? = "Cricket",
    @SerializedName("iframe")
    val iframe: String? = null,
    @SerializedName("substreams")
    val substreams: Any? = null,
    @SerializedName("viewers")
    val viewers: Int? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("category")
    val category: String? = null
)

/**
 * TimStreams Playback / Channel Provider Data Model
 * Source API: https://timst.top/api/channels
 * Identifies WHERE the match can be watched (Playback source).
 */
data class TimChannelResponse(
    @SerializedName("channels")
    val channels: List<TimChannel> = emptyList()
)

data class TimChannel(
    @SerializedName("url")
    val url: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("logo")
    val logo: String? = null,
    @SerializedName("genre")
    val genre: Any? = null,
    @SerializedName("flag")
    val flag: String? = null,
    @SerializedName("vip")
    val vip: Boolean = false,
    @SerializedName("viewers")
    val viewers: Int = 0,
    @SerializedName("streams")
    val streams: List<TimStream> = emptyList()
)

data class TimStream(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("vip")
    val vip: Boolean = false
)

/**
 * Joined Cricket Event representation.
 * Joins Event Decoder (event metadata) with TimStreams (playback channel).
 */
data class CricketEventWithChannel(
    val event: EventDecoderCricketEvent,
    val channel: TimChannel?,
    val matchStatus: CricketMatchStatus
)

enum class CricketMatchStatus {
    MATCHED,
    CHANNEL_NOT_FOUND,
    STREAM_NOT_FOUND,
    SOURCE_UNAVAILABLE
}

/**
 * Helper to convert a CricketEventWithChannel into the app's unified APIMatch model
 * so it integrates seamlessly with existing Player, Favorites, and History pipelines.
 */
fun CricketEventWithChannel.toAPIMatch(): APIMatch {
    val streamList = channel?.streams?.mapIndexed { index, s ->
        Stream(
            id = "${channel.url}_$index",
            streamNo = index + 1,
            language = if (channel.flag != null && channel.flag.isNotBlank()) channel.flag.uppercase() else "EN",
            hd = true,
            embedUrl = s.url,
            source = channel.name.ifBlank { "TimStreams" }
        )
    } ?: emptyList()

    return APIMatch(
        id = "cricket_${event.matchId.ifBlank { event.id?.toString() ?: event.name.hashCode().toString() }}",
        title = event.name,
        category = "cricket",
        date = 0L,
        poster = event.poster,
        popular = event.tag.equals("LIVE", ignoreCase = true) || (event.viewers ?: 0) > 100,
        teams = null,
        sources = if (channel != null && streamList.isNotEmpty()) {
            listOf(MatchSource(source = channel.url, id = event.matchId))
        } else emptyList()
    )
}
