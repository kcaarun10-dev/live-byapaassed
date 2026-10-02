package com.example.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassTextSecondary

@Composable
fun StreamDiagnosticsDialog(
    resolution: String,
    bitrate: String,
    liveOffsetMs: Long,
    bufferedDurationMs: Long,
    isPlaying: Boolean,
    isLowLatency: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GlassObsidian,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "STREAM DIAGNOSTICS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagRow("Playback Status", if (isPlaying) "Playing (Live Sync)" else "Paused")
                DiagRow("Video Resolution", resolution)
                DiagRow("Video Bitrate", bitrate)
                DiagRow("Live Offset", if (liveOffsetMs <= 0) "0s (Live Edge)" else "${liveOffsetMs / 1000}s behind")
                DiagRow("Buffer Available", "${bufferedDurationMs / 1000}s")
                DiagRow("Low Latency Mode", if (isLowLatency) "Enabled (Sub-second)" else "Standard ABR")
                DiagRow("Network Engine", "Chromium Cronet HTTP/2")
                DiagRow("TLS Handshake", "BoringSSL Browser Masked")
                DiagRow("Video Renderer", "MediaCodec Native Surface")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = GlassSilverMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (label.contains("Live") || label.contains("Resolution")) GlassIceBlue else Color.White
        )
    }
}
