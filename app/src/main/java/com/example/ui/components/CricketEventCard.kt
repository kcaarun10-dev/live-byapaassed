package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.api.AppImageLoader
import com.example.data.models.CricketEventWithChannel
import com.example.data.models.CricketMatchStatus
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@Composable
fun CricketEventCard(
    eventWithChannel: CricketEventWithChannel,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val event = eventWithChannel.event
    val channel = eventWithChannel.channel
    val status = eventWithChannel.matchStatus
    val isLive = event.tag.equals("LIVE", ignoreCase = true)
    val isMatched = status == CricketMatchStatus.MATCHED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                if (isLive) Color.White.copy(alpha = 0.5f) else GlassBorderSilver.copy(alpha = 0.4f),
                RoundedCornerShape(22.dp)
            )
            .testTag("cricket_card_${event.matchId}"),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Poster & Backdrop Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(GlassObsidian)
            ) {
                // Instant Visual Stadium Backdrop (renders instantly)
                InstantSportBackdrop(
                    category = "cricket",
                    title = event.name
                )

                if (!event.poster.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(event.poster)
                            .crossfade(150)
                            .build(),
                        imageLoader = AppImageLoader.get(context),
                        contentDescription = event.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Dark Glass Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    GlassSurface.copy(alpha = 0.75f),
                                    GlassSurface
                                )
                            )
                        )
                )

                // Top Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category / Tournament Pill
                    Surface(
                        color = GlassObsidian.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, GlassBorderSilver)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = (event.sourceTag ?: event.categoryName ?: "CRICKET").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Live or Status Badge
                    if (isLive) {
                        LiveBadge()
                    } else {
                        Surface(
                            color = GlassWhiteSubtle,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.5.dp, GlassBorderSubtle)
                        ) {
                            Text(
                                text = event.tag.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassSilverMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Viewers badge if present
                if (event.viewers != null && event.viewers > 0) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = GlassIceBlue,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${event.viewers} watching",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Details and Action Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Event Name (From Event Decoder)
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Matched Channel Info (From TimStreams)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMatched && channel != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = GlassIceBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = channel.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = GlassIceBlue
                            )
                            if (channel.flag != null && channel.flag.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• ${channel.flag.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlassSilverMuted
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = GlassSilverMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Stream currently unavailable",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassSilverMuted
                            )
                        }
                    }

                    // Watch Button
                    if (isMatched) {
                        Button(
                            onClick = onWatchClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("cricket_watch_${event.matchId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WATCH",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, GlassBorderSubtle)
                        ) {
                            Text(
                                text = "OFFLINE",
                                color = GlassSilverMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
