package com.example.player

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.common.Tracks

data class VideoQuality(
    val width: Int,
    val height: Int,
    val bitrate: Int = 0,
    val label: String
) {
    companion object {
        val AUTO = VideoQuality(
            width = -1,
            height = -1,
            bitrate = 0,
            label = "Auto"
        )

        fun fromFormat(format: Format): VideoQuality {
            val h = format.height
            val w = format.width
            val label = when {
                h >= 2160 -> "4K"
                h >= 1440 -> "1440p"
                h >= 1080 -> "1080p"
                h >= 720 -> "720p"
                h >= 480 -> "480p"
                h >= 360 -> "360p"
                h >= 240 -> "240p"
                h > 0 -> "${h}p"
                else -> "${w}x${h}"
            }
            return VideoQuality(
                width = w,
                height = h,
                bitrate = format.bitrate,
                label = label
            )
        }

        fun extractAvailableQualities(tracks: Tracks): List<VideoQuality> {
            val list = mutableListOf<VideoQuality>()
            for (group in tracks.groups) {
                if (group.type == C.TRACK_TYPE_VIDEO) {
                    for (i in 0 until group.length) {
                        if (group.isTrackSupported(i)) {
                            val format = group.getTrackFormat(i)
                            if (format.height > 0 && format.width > 0) {
                                list.add(fromFormat(format))
                            }
                        }
                    }
                }
            }
            // Deduplicate by resolution and sort descending
            return list.distinctBy { "${it.width}x${it.height}" }
                .sortedWith(compareByDescending<VideoQuality> { it.height }.thenByDescending { it.bitrate })
        }
    }
}
