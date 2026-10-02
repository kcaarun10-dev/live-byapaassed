package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.api.ApiClient
import com.example.data.models.APIMatch
import com.example.data.models.SportCategory
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
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextPrimary
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteFaint
import com.example.ui.theme.GlassWhiteHighlight
import com.example.ui.theme.GlassWhiteMedium
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

/**
 * Premium Soft Glass Surface Container with subtle reflection & silver border.
 */
@Composable
fun SoftGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = GlassWhiteSubtle,
    borderColor: Color = GlassBorderSilver,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.border(borderWidth, borderColor, shape),
        shape = shape,
        color = backgroundColor,
        shadowElevation = 0.dp
    ) {
        Box {
            // Subtle top highlight reflection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                GlassWhiteHighlight.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            content()
        }
    }
}

/**
 * Frosted Glass LIVE Badge (Strictly NO GREEN: pure white/silver with subtle live pulse).
 */
@Composable
fun LiveBadge(
    modifier: Modifier = Modifier,
    label: String = "LIVE"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = GlassObsidian.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GlassBorderSilver.copy(alpha = 0.7f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GlassLiveRed.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Frosted Glass UPCOMING Badge with Soft Ice Blue glass highlight.
 */
@Composable
fun UpcomingBadge(
    modifier: Modifier = Modifier,
    label: String = "UPCOMING"
) {
    Surface(
        color = GlassObsidian.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GlassIceBlue.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(GlassIceBlue)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = GlassIceBlue,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Smart Status Badge: automatically displays LIVE with pulsating dot or UPCOMING with time tag.
 */
@Composable
fun MatchStatusBadge(
    match: APIMatch,
    modifier: Modifier = Modifier
) {
    if (match.isUpcoming()) {
        val label = if (match.date > 0L) "UPCOMING • ${match.getFormattedTime()}" else "UPCOMING"
        UpcomingBadge(modifier = modifier, label = label)
    } else {
        LiveBadge(modifier = modifier, label = "LIVE")
    }
}

/**
 * Horizontal Soft Glass Category Pill Carousel.
 */
@Composable
fun GlassCategoryCarousel(
    categories: List<SportCategory>,
    selectedCategory: SportCategory,
    onCategorySelected: (SportCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedCategory.id
            GlassCategoryPill(
                category = category,
                isSelected = isSelected,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun GlassCategoryPill(
    category: SportCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) GlassWhiteStrong else GlassWhiteFaint
    val borderColor = if (isSelected) GlassBorderActive else GlassBorderSubtle
    val textColor = if (isSelected) Color.White else GlassTextSecondary
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("category_pill_${category.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = fontWeight,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Cinematic Live Stream Video Card with Soft Glass overlay & tags.
 */
@Composable
fun StreamCard(
    match: APIMatch,
    onClick: () -> Unit,
    onFavoriteClick: (APIMatch) -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier,
    viewerCount: String = "12.4K"
) {
    val posterUrl = ApiClient.getPosterUrl(match.poster)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = BorderStroke(1.dp, GlassBorderSilver.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("stream_card_${match.id}")
    ) {
        Column {
            // Thumbnail container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
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

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MatchStatusBadge(match = match)

                    // Favorite Icon Button
                    Surface(
                        color = GlassObsidian.copy(alpha = 0.7f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, GlassBorderSubtle),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onFavoriteClick(match) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) GlassLiveRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom thumbnail metadata (Category & Viewers)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Pill
                    Surface(
                        color = GlassWhiteStrong,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, GlassBorderSilver.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = match.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Viewer Count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(GlassObsidian.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = GlassSilverMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewerCount,
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassSilverMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Text Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "HD 1080p",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassIceBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "•",
                        color = GlassTextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Native Ad-Bypassed",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassSilverMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Large Featured Hero Stream Card for Home and Category Screens.
 */
@Composable
fun FeaturedHeroCard(
    match: APIMatch,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewerCount: String = "48.2K"
) {
    val posterUrl = ApiClient.getPosterUrl(match.poster)

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        border = BorderStroke(1.dp, GlassBorderSilver.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onWatchClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
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

            // Cinematic Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color(0xFF0C0E14).copy(alpha = 0.6f),
                                Color(0xFF0C0E14).copy(alpha = 0.98f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MatchStatusBadge(match = match)

                    Surface(
                        color = GlassWhiteStrong,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, GlassBorderSilver)
                    ) {
                        Text(
                            text = match.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Bottom Section
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = GlassIceBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$viewerCount watching now",
                            style = MaterialTheme.typography.labelSmall,
                            color = GlassIceBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // WATCH LIVE Glass Button
                    Button(
                        onClick = onWatchClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("featured_watch_live_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WATCH LIVE",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating Soft Glass Bottom Navigation Dock.
 * Only 4 destinations: HOME | LIVE | SEARCH | FAVORITES.
 */
@Composable
fun GlassNavigationDock(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem("home", "HOME", Icons.Default.PlayArrow),
        NavigationItem("live", "LIVE", Icons.Default.FiberManualRecord),
        NavigationItem("search", "SEARCH", Icons.Outlined.Search),
        NavigationItem("favorites", "FAVORITES", Icons.Default.FavoriteBorder)
    )

    Surface(
        color = GlassObsidian.copy(alpha = 0.88f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, GlassBorderSilver.copy(alpha = 0.5f)),
        shadowElevation = 12.dp,
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 14.dp)
            .fillMaxWidth()
            .testTag("glass_navigation_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute.startsWith(item.route)
                val activeColor = Color.White
                val inactiveColor = GlassTextMuted

                Surface(
                    color = if (isSelected) GlassWhiteStrong else Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    border = if (isSelected) BorderStroke(1.dp, GlassBorderSilver) else null,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) }
                        .testTag("nav_item_${item.route}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(18.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = activeColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Floating Soft Glass Mini Player Dock.
 */
@Composable
fun GlassMiniPlayer(
    matchTitle: String,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onExpandClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GlassSurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GlassBorderSilver),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onExpandClick)
            .testTag("mini_player_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                LiveBadge()
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = matchTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Tap to expand fullscreen",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassSilverMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPlayPauseToggle) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GlassSilverMuted
                    )
                }
            }
        }
    }
}

/**
 * Centered Soft Glass Error & Offline State Card.
 */
@Composable
fun GlassErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    onGoBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        SoftGlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = GlassLiveRed.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = GlassLiveRed,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(text = "RETRY", fontWeight = FontWeight.Bold)
                }

                if (onGoBack != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onGoBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassWhiteSubtle,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "GO BACK", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
