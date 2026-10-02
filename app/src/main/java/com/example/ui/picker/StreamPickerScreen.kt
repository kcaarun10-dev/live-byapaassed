package com.example.ui.picker

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.LocalDataManager
import com.example.data.models.APIMatch
import com.example.data.models.MatchSource
import com.example.data.models.Stream
import com.example.ui.components.GlassErrorState
import com.example.ui.components.LiveBadge
import com.example.ui.components.MatchStatusBadge
import com.example.ui.components.SoftGlassSurface
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassCharcoal
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassLiveRed
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteFaint
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle
import java.util.Locale

@Composable
fun StreamPickerScreen(
    viewModel: StreamPickerViewModel,
    localDataManager: LocalDataManager,
    onBackClick: () -> Unit,
    onStreamSelected: (Stream, APIMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val match by viewModel.match.collectAsState()
    val selectedSource by viewModel.selectedSource.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val favorites by localDataManager.favoritesFlow.collectAsState()
    val reminders by localDataManager.remindersFlow.collectAsState()

    val currentMatch = match
    val isFavorite = currentMatch != null && favorites.any { it.id == currentMatch.id }
    val hasReminder = currentMatch != null && reminders.contains(currentMatch.id)

    Scaffold(
        containerColor = GlassMidnight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        if (currentMatch == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 48.dp)
            ) {
                // Large Cinematic Stream Artwork Hero
                item {
                    StreamHeroArtwork(
                        match = currentMatch,
                        onBackClick = onBackClick
                    )
                }

                // Stream Meta Details
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = currentMatch.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = GlassWhiteStrong,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorderSilver)
                            ) {
                                Text(
                                    text = currentMatch.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = GlassIceBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "34.8K live viewers",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GlassIceBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "•",
                                color = GlassTextMuted,
                                fontSize = 10.sp
                            )

                            Text(
                                text = "Ad-Bypassed",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassSilverMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Live video broadcast of ${currentMatch.title} in HD quality with zero advertisements and low-latency ExoPlayer streaming.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: WATCH LIVE, FAVORITE, SHARE, REMINDER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Watch Live button (picks first stream automatically or opens player)
                            Button(
                                onClick = {
                                    when (val s = uiState) {
                                        is StreamPickerUiState.StreamsLoaded -> {
                                            val firstStream = s.streams.firstOrNull()
                                            if (firstStream != null) {
                                                onStreamSelected(firstStream, currentMatch)
                                            }
                                        }
                                        else -> {}
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("details_watch_live_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WATCH LIVE",
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Favorite Button
                            IconButton(
                                onClick = { localDataManager.toggleFavorite(currentMatch) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassWhiteSubtle)
                                    .border(1.dp, GlassBorderSilver, RoundedCornerShape(14.dp))
                                    .testTag("details_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) GlassLiveRed else Color.White
                                )
                            }

                            // Reminder Button
                            IconButton(
                                onClick = { localDataManager.toggleReminder(currentMatch.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassWhiteSubtle)
                                    .border(1.dp, GlassBorderSilver, RoundedCornerShape(14.dp))
                                    .testTag("details_reminder_button")
                            ) {
                                Icon(
                                    imageVector = if (hasReminder) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "Reminder",
                                    tint = if (hasReminder) GlassIceBlue else Color.White
                                )
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Watch ${currentMatch.title} live on AR SPORTS!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Stream"))
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(GlassWhiteSubtle)
                                    .border(1.dp, GlassBorderSilver, RoundedCornerShape(14.dp))
                                    .testTag("details_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Sources Selector (Alpha, Bravo, Charlie, etc.)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "STREAM SERVERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GlassSilverMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (currentMatch.sources.isEmpty()) {
                            Text(
                                text = "No stream sources listed yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlassTextMuted
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                currentMatch.sources.forEach { source ->
                                    val isSelected = selectedSource?.id == source.id && selectedSource?.source == source.source
                                    GlassSourcePill(
                                        source = source,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectSource(source) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Available Feeds for selected server
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "AVAILABLE FEEDS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GlassSilverMuted,
                            letterSpacing = 1.sp
                        )
                    }
                }

                when (val state = uiState) {
                    is StreamPickerUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                            }
                        }
                    }

                    is StreamPickerUiState.Error -> {
                        item {
                            GlassErrorState(
                                title = "Feed Offline",
                                message = state.message,
                                onRetry = { viewModel.retry() },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    is StreamPickerUiState.StreamsLoaded -> {
                        if (state.streams.isEmpty()) {
                            item {
                                SoftGlassSurface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "No feeds active on ${state.source.source.uppercase()}. Select another server above.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GlassTextSecondary,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        } else {
                            items(
                                items = state.streams,
                                key = { "${it.source}_${it.id}_${it.streamNo}" }
                            ) { stream ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 6.dp)
                                ) {
                                    GlassStreamRowCard(
                                        stream = stream,
                                        onPlay = { onStreamSelected(stream, currentMatch) }
                                    )
                                }
                            }
                        }
                    }

                    is StreamPickerUiState.Idle -> {
                        item {
                            Text(
                                text = "Select a stream server to view feeds.",
                                color = GlassTextMuted,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamHeroArtwork(
    match: APIMatch,
    onBackClick: () -> Unit
) {
    val posterUrl = ApiClient.getPosterUrl(match.poster)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(GlassCharcoal)
    ) {
        if (!posterUrl.isNullOrBlank()) {
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

        // Cinematic Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            GlassMidnight
                        )
                    )
                )
        )

        // Top Back and Live Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = GlassObsidian.copy(alpha = 0.7f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSilver),
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            MatchStatusBadge(match = match)
        }
    }
}

@Composable
private fun GlassSourcePill(
    source: MatchSource,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val displayName = remember(source.source) {
        source.source.uppercase()
    }

    Surface(
        color = if (isSelected) Color.White else GlassWhiteSubtle,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color.White else GlassBorderSubtle
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("source_pill_${source.source}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = if (isSelected) Color.Black else GlassSilver,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun GlassStreamRowCard(
    stream: Stream,
    onPlay: () -> Unit
) {
    SoftGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .testTag("stream_row_${stream.streamNo}"),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = GlassSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = GlassWhiteStrong,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${stream.streamNo}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Feed ${stream.streamNo} (${stream.language.uppercase()})",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (stream.hd) {
                            Text(
                                text = "1080p HD",
                                style = MaterialTheme.typography.labelSmall,
                                color = GlassIceBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "•",
                                color = GlassTextMuted,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "Cronet TLS High Bitrate",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassSilverMuted
                        )
                    }
                }
            }

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PLAY",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
