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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsBottomSheet(
    currentQualityLabel: String,
    currentSpeed: Float,
    isLowLatency: Boolean,
    onQualityClick: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    onPipClick: () -> Unit,
    onLowLatencyToggle: (Boolean) -> Unit,
    onDiagnosticsClick: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    var showSpeedSelector by remember { mutableStateOf(false) }

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
                .testTag("player_settings_sheet")
        ) {
            Text(
                text = "PLAYER SETTINGS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showSpeedSelector) {
                // Playback speed selector view
                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Playback Speed",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassSilverMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    speeds.forEach { speed ->
                        val isSelected = currentSpeed == speed
                        Surface(
                            color = if (isSelected) GlassWhiteStrong else GlassWhiteSubtle,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color.White else GlassBorderSubtle
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeedSelected(speed)
                                    showSpeedSelector = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${speed}x",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else GlassTextSecondary
                                )
                                if (isSelected) {
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassIceBlue
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Main Settings Rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsRowItem(
                        icon = Icons.Default.Tune,
                        title = "Video Quality",
                        subtitle = currentQualityLabel,
                        onClick = {
                            onDismiss()
                            onQualityClick()
                        }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Speed,
                        title = "Playback Speed",
                        subtitle = "${currentSpeed}x",
                        onClick = { showSpeedSelector = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Audio Track",
                        subtitle = "Original (Stereo)",
                        onClick = {}
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Subtitles,
                        title = "Subtitles",
                        subtitle = "Off",
                        onClick = {}
                    )

                    SettingsRowItem(
                        icon = Icons.Default.PictureInPictureAlt,
                        title = "Picture-in-Picture",
                        subtitle = "Watch while browsing other apps",
                        onClick = {
                            onDismiss()
                            onPipClick()
                        }
                    )

                    // Low Latency Toggle Row
                    Surface(
                        color = GlassWhiteSubtle,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = GlassWhiteStrong,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = GlassIceBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Low Latency Mode",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sub-second live stream synchronization",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassSilverMuted
                                    )
                                }
                            }

                            Switch(
                                checked = isLowLatency,
                                onCheckedChange = onLowLatencyToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GlassIceBlue,
                                    uncheckedThumbColor = GlassSilverMuted,
                                    uncheckedTrackColor = GlassSurfaceElevated
                                )
                            )
                        }
                    }

                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "Stream Diagnostics",
                        subtitle = "Bitrate, FPS, Codecs, TLS Handshake",
                        onClick = {
                            onDismiss()
                            onDiagnosticsClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = GlassWhiteSubtle,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
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
                Surface(
                    color = GlassWhiteStrong,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassSilverMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = GlassSilverMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
