package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.VideoQuality
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityPickerBottomSheet(
    availableQualities: List<VideoQuality>,
    selectedQuality: VideoQuality,
    onQualitySelected: (VideoQuality) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GlassObsidian,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(GlassBorderSilver, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("quality_bottom_sheet")
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Surface(
                    color = GlassWhiteStrong,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "VIDEO QUALITY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Select a specific resolution or Auto for adaptive bitrate",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassSilverMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Auto Item
                item {
                    QualityItemRow(
                        title = "Auto (Adaptive)",
                        subtitle = "Adapts dynamically to network bandwidth (ABR)",
                        badgeText = "AUTO",
                        isSelected = selectedQuality == VideoQuality.AUTO,
                        onClick = {
                            onQualitySelected(VideoQuality.AUTO)
                            onDismiss()
                        }
                    )
                }

                // Discovered resolutions
                items(availableQualities) { quality ->
                    val isSelected = selectedQuality.width == quality.width && selectedQuality.height == quality.height
                    val bitrateText = if (quality.bitrate > 0) {
                        val mbps = quality.bitrate / 1_000_000.0
                        if (mbps >= 1.0) String.format("%.1f Mbps", mbps)
                        else "${quality.bitrate / 1000} kbps"
                    } else null

                    val badgeText = when {
                        quality.height >= 1080 -> "FHD"
                        quality.height >= 720 -> "HD"
                        else -> "SD"
                    }

                    val subtitle = buildString {
                        append("${quality.width}x${quality.height}")
                        if (bitrateText != null) {
                            append(" • ")
                            append(bitrateText)
                        }
                    }

                    QualityItemRow(
                        title = quality.label,
                        subtitle = subtitle,
                        badgeText = badgeText,
                        isSelected = isSelected,
                        onClick = {
                            onQualitySelected(quality)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityItemRow(
    title: String,
    subtitle: String,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) GlassWhiteStrong else GlassWhiteSubtle,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color.White else GlassBorderSubtle
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Quality Badge
                Surface(
                    color = when (badgeText) {
                        "AUTO" -> Color.White.copy(alpha = 0.2f)
                        "FHD", "HD" -> GlassIceBlue.copy(alpha = 0.2f)
                        else -> Color.White.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = when (badgeText) {
                            "AUTO" -> Color.White
                            "FHD", "HD" -> GlassIceBlue
                            else -> GlassSilverMuted
                        },
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassSilverMuted
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.White,
                    unselectedColor = GlassTextMuted
                )
            )
        }
    }
}
