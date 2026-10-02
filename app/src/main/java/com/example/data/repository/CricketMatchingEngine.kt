package com.example.data.repository

import com.example.data.models.CricketEventWithChannel
import com.example.data.models.CricketMatchStatus
import com.example.data.models.EventDecoderCricketEvent
import com.example.data.models.TimChannel
import com.example.data.remote.CricketAdminConfig
import java.util.Locale

/**
 * =========================================================================================
 * CRICKET MATCHING ENGINE:
 * =========================================================================================
 * Architectural Roles:
 * - Event Decoder = WHAT MATCH IS PLAYING (Event metadata)
 * - TimStreams    = WHERE THE MATCH CAN BE WATCHED (Broadcaster Channels & Streams)
 * - Matcher       = CONNECTS THE EVENT TO THE APPROPRIATE CHANNEL
 *
 * Signal 1: Provider / Source (e.g. "Willow Cricket Event Info")
 * Signal 2: Cricket Category / Genre
 * Signal 3: Broadcaster / Channel Name
 *
 * Rules:
 * - Never match using match_id directly (different namespaces).
 * - Channels are broadcasters, NOT individual matches (multiple events can share one channel).
 * - Only matched/eligible channels are associated with events.
 * - If no matching channel exists, mark CHANNEL_NOT_FOUND (never fabricate links).
 * =========================================================================================
 */
class CricketMatchingEngine(
    private val config: CricketAdminConfig = CricketAdminConfig()
) {

    fun matchEventsWithChannels(
        events: List<EventDecoderCricketEvent>,
        channels: List<TimChannel>
    ): List<CricketEventWithChannel> {
        if (events.isEmpty()) return emptyList()

        // Filter cricket-capable channels from live TimStreams dataset
        val cricketCapableChannels = filterCricketChannels(channels)

        return events.map { event ->
            val matchedChannel = findBestMatchingChannel(event, cricketCapableChannels)
            val matchStatus = determineStatus(matchedChannel)

            CricketEventWithChannel(
                event = event,
                channel = matchedChannel,
                matchStatus = matchStatus
            )
        }
    }

    /**
     * Filters live TimStreams dataset for cricket-capable channels using:
     * 1. Remote Genre mappings (e.g. genre in [2])
     * 2. Cricket-related channel names & identifiers
     */
    private fun filterCricketChannels(channels: List<TimChannel>): List<TimChannel> {
        val cricketGenreCodes = config.genreMappings["cricket"] ?: listOf(2)

        return channels.filter { channel ->
            // Check Genre signal
            val genreNum = when (val g = channel.genre) {
                is Number -> g.toInt()
                is String -> g.toDoubleOrNull()?.toInt()
                else -> null
            }
            val matchesGenre = genreNum != null && cricketGenreCodes.contains(genreNum)

            // Check Channel Name / URL keyword signal
            val nameNormalized = channel.name.lowercase(Locale.ROOT)
            val urlNormalized = channel.url.lowercase(Locale.ROOT)
            val hasCricketKeywords = nameNormalized.contains("cricket") ||
                    urlNormalized.contains("cricket") ||
                    nameNormalized.contains("willow") ||
                    urlNormalized.contains("willow") ||
                    nameNormalized.contains("star sports") ||
                    urlNormalized.contains("star-sports") ||
                    nameNormalized.contains("sky sports cricket") ||
                    urlNormalized.contains("sky-sports-cricket") ||
                    nameNormalized.contains("supersport cricket") ||
                    urlNormalized.contains("supersport-cricket") ||
                    nameNormalized.contains("ptv sports") ||
                    urlNormalized.contains("ptv-sports") ||
                    nameNormalized.contains("sony ten") ||
                    urlNormalized.contains("sony-ten") ||
                    nameNormalized.contains("tnt sports") ||
                    nameNormalized.contains("fox cricket") ||
                    urlNormalized.contains("fox-cricket")

            matchesGenre || hasCricketKeywords
        }
    }

    /**
     * Resolves the corresponding TimStreams channel for an Event Decoder event.
     */
    private fun findBestMatchingChannel(
        event: EventDecoderCricketEvent,
        cricketChannels: List<TimChannel>
    ): TimChannel? {
        if (cricketChannels.isEmpty()) return null

        val eventSource = event.source?.trim() ?: ""
        val eventSourceTag = event.sourceTag?.trim() ?: ""
        val eventName = event.name.lowercase(Locale.ROOT)

        // 1. Check Source Mappings from Firebase / Admin Config
        if (eventSource.isNotBlank()) {
            val configuredTargetIdentifiers = config.sourceMappings[eventSource]
            if (configuredTargetIdentifiers != null && configuredTargetIdentifiers.isNotEmpty()) {
                val directMatch = cricketChannels.firstOrNull { channel ->
                    configuredTargetIdentifiers.any { id ->
                        channel.url.equals(id, ignoreCase = true) ||
                                channel.name.contains(id, ignoreCase = true) ||
                                channel.url.contains(id, ignoreCase = true)
                    }
                }
                if (directMatch != null) return directMatch
            }

            // Fuzzy match on event.source words (e.g. "Willow", "Star", "Sky", "Sony", "PTV")
            val sourceLower = eventSource.lowercase(Locale.ROOT)
            val broadcasterMatch = cricketChannels.firstOrNull { channel ->
                val chName = channel.name.lowercase(Locale.ROOT)
                val chUrl = channel.url.lowercase(Locale.ROOT)
                (sourceLower.contains("willow") && (chName.contains("willow") || chUrl.contains("willow"))) ||
                        (sourceLower.contains("star") && (chName.contains("star") || chUrl.contains("star"))) ||
                        (sourceLower.contains("sky") && (chName.contains("sky") || chUrl.contains("sky"))) ||
                        (sourceLower.contains("sony") && (chName.contains("sony") || chUrl.contains("sony"))) ||
                        (sourceLower.contains("ptv") && (chName.contains("ptv") || chUrl.contains("ptv"))) ||
                        (sourceLower.contains("supersport") && (chName.contains("supersport") || chUrl.contains("supersport"))) ||
                        (sourceLower.contains("fox") && (chName.contains("fox") || chUrl.contains("fox")))
            }
            if (broadcasterMatch != null) return broadcasterMatch
        }

        // 2. Check event.source_tag (e.g. Tournament / Broadcaster info)
        if (eventSourceTag.isNotBlank()) {
            val tagLower = eventSourceTag.lowercase(Locale.ROOT)
            val tagMatch = cricketChannels.firstOrNull { channel ->
                val chName = channel.name.lowercase(Locale.ROOT)
                val chUrl = channel.url.lowercase(Locale.ROOT)
                chName.contains(tagLower) || tagLower.contains(chName) || chUrl.contains(tagLower)
            }
            if (tagMatch != null) return tagMatch
        }

        // 3. Check event name keywords for specific broadcaster mentions
        val nameMatch = cricketChannels.firstOrNull { channel ->
            val chName = channel.name.lowercase(Locale.ROOT)
            chName.isNotBlank() && eventName.contains(chName)
        }
        if (nameMatch != null) return nameMatch

        // 4. Default to first active cricket channel if only general cricket source exists
        if (eventSource.contains("Cricket", ignoreCase = true) || event.categoryName.equals("Cricket", ignoreCase = true)) {
            val defaultCricket = cricketChannels.firstOrNull { it.streams.isNotEmpty() }
            if (defaultCricket != null) return defaultCricket
        }

        return null
    }

    private fun determineStatus(channel: TimChannel?): CricketMatchStatus {
        return when {
            channel == null -> CricketMatchStatus.CHANNEL_NOT_FOUND
            channel.streams.isEmpty() -> CricketMatchStatus.STREAM_NOT_FOUND
            channel.streams.none { it.url.isNotBlank() } -> CricketMatchStatus.SOURCE_UNAVAILABLE
            else -> CricketMatchStatus.MATCHED
        }
    }
}
