package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AdminConfig
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@Composable
fun AppUpdateDialog(
    config: AdminConfig,
    currentVersionName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!config.isForceUpdate) {
                onDismiss()
            }
        },
        containerColor = GlassObsidian,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = GlassWhiteStrong,
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NEW VERSION AVAILABLE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Version Badge
                Surface(
                    color = GlassWhiteSubtle,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorderSilver)
                ) {
                    Text(
                        text = "v$currentVersionName  ➔  v${config.latestVersionName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GlassIceBlue,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = config.updateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (config.isForceUpdate) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This update is required to continue live sports video streaming.",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassSilverMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (config.downloadUrl.isNotBlank()) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(config.downloadUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("download_update_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DOWNLOAD APK UPDATE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        },
        dismissButton = {
            if (!config.isForceUpdate) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Later",
                        color = GlassSilverMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    )
}
