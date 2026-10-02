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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.api.ApiClient
import com.example.data.models.APIMatch
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MatchCard(
    match: APIMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedTime = remember(match.date) {
        if (match.date <= 0L) "Live Stream"
        else {
            try {
                val sdf = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                sdf.format(Date(match.date))
            } catch (_: Exception) {
                "Live Stream"
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GlassBorderSilver.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("match_card_${match.id}"),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(GlassObsidian)
            ) {
                val posterUrl = ApiClient.getPosterUrl(match.poster)
                if (posterUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(posterUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = match.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    GlassSurface.copy(alpha = 0.85f),
                                    GlassSurface
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = GlassWhiteStrong,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, GlassBorderSilver)
                    ) {
                        Text(
                            text = match.category.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    MatchStatusBadge(match = match)
                }

                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // Bottom time strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhiteSubtle)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassSilverMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = GlassIceBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "HD Feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassIceBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
