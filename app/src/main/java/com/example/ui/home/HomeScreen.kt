package com.example.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalDataManager
import com.example.data.models.APIMatch
import com.example.data.models.SportCategory
import com.example.data.remote.AdminConfigManager
import com.example.ui.components.AdminAnnouncementBanner
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.FeaturedHeroCard
import com.example.ui.components.GlassCategoryCarousel
import com.example.ui.components.GlassErrorState
import com.example.ui.components.LiveBadge
import com.example.ui.components.SoftGlassSurface
import com.example.ui.components.StreamCard
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassSilver
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    localDataManager: LocalDataManager,
    adminConfigManager: AdminConfigManager? = null,
    onMatchClick: (APIMatch) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (SportCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites by localDataManager.favoritesFlow.collectAsState()
    val watchHistory by localDataManager.historyFlow.collectAsState()

    val adminConfig by adminConfigManager?.configFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val announcement by adminConfigManager?.announcementFlow?.collectAsState() ?: remember { mutableStateOf(null) }
    val isUpdateAvailable by adminConfigManager?.updateAvailableFlow?.collectAsState() ?: remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adminConfigManager?.fetchRemoteConfig()
    }

    LaunchedEffect(isUpdateAvailable) {
        if (isUpdateAvailable) {
            showUpdateDialog = true
        }
    }

    var selectedCategory by remember { mutableStateOf(SportCategory.ALL) }

    Scaffold(
        containerColor = GlassMidnight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading live sports feeds…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                GlassErrorState(
                    title = "Stream Feed Unavailable",
                    message = state.message,
                    onRetry = { viewModel.loadMatches() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is HomeUiState.Success -> {
                val allMatches = state.liveMatches + state.todayMatches
                val liveList = state.liveMatches.ifEmpty { allMatches.take(8) }
                val featuredMatch = liveList.firstOrNull() ?: allMatches.firstOrNull()

                val filteredLive = if (selectedCategory == SportCategory.ALL) {
                    liveList
                } else {
                    liveList.filter { it.category.equals(selectedCategory.apiSlug, ignoreCase = true) }
                }

                val popularMatches = allMatches.filter { it.popular || it.sources.size > 1 }.take(10)
                val upcomingMatches = state.todayMatches.filter { it !in state.liveMatches }.take(10)
                val recommendedMatches = allMatches.shuffled().take(8)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // Top Floating Glass Header
                    item {
                        HomeTopHeader(
                            onSearchClick = onSearchClick,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                        )
                    }

                    // Admin Announcement Banner (if pushed from Firebase/Admin Panel)
                    if (announcement != null) {
                        item {
                            AdminAnnouncementBanner(
                                announcement = announcement!!,
                                onDismiss = {
                                    adminConfigManager?.dismissAnnouncement(announcement!!.id)
                                }
                            )
                        }
                    }

                    // Hero Featured Live Stream
                    if (featuredMatch != null) {
                        item {
                            FeaturedHeroCard(
                                match = featuredMatch,
                                onWatchClick = { onMatchClick(featuredMatch) },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Category Pill Carousel
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "FOOTBALL & CRICKET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = GlassSilverMuted,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                            GlassCategoryCarousel(
                                categories = SportCategory.CATEGORIES,
                                selectedCategory = selectedCategory,
                                onCategorySelected = { cat ->
                                    selectedCategory = cat
                                    if (cat != SportCategory.ALL) {
                                        onCategoryClick(cat)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }

                    // Continue Watching (if history exists)
                    if (watchHistory.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "CONTINUE WATCHING",
                                subtitle = "Pick up where you left off"
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                items(watchHistory) { historyItem ->
                                    val isFav = favorites.any { it.id == historyItem.match.id }
                                    Box(modifier = Modifier.width(280.dp)) {
                                        StreamCard(
                                            match = historyItem.match,
                                            onClick = { onMatchClick(historyItem.match) },
                                            onFavoriteClick = { localDataManager.toggleFavorite(historyItem.match) },
                                            isFavorite = isFav
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // LIVE NOW Section
                    item {
                        SectionHeader(
                            title = if (selectedCategory == SportCategory.ALL) "LIVE NOW" else "LIVE ${selectedCategory.name}",
                            subtitle = "${filteredLive.size} active live streams"
                        )
                    }

                    if (filteredLive.isEmpty()) {
                        item {
                            SoftGlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "No active live streams in this category right now.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlassTextSecondary,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                items(filteredLive) { match ->
                                    val isFav = favorites.any { it.id == match.id }
                                    Box(modifier = Modifier.width(300.dp)) {
                                        StreamCard(
                                            match = match,
                                            onClick = { onMatchClick(match) },
                                            onFavoriteClick = { localDataManager.toggleFavorite(match) },
                                            isFavorite = isFav
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // POPULAR STREAMS
                    if (popularMatches.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeader(
                                title = "POPULAR STREAMS",
                                subtitle = "Top watched live sporting events"
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                items(popularMatches) { match ->
                                    val isFav = favorites.any { it.id == match.id }
                                    Box(modifier = Modifier.width(280.dp)) {
                                        StreamCard(
                                            match = match,
                                            onClick = { onMatchClick(match) },
                                            onFavoriteClick = { localDataManager.toggleFavorite(match) },
                                            isFavorite = isFav
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // UPCOMING STREAMS
                    if (upcomingMatches.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeader(
                                title = "UPCOMING STREAMS",
                                subtitle = "Scheduled live broadcasts today"
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                items(upcomingMatches) { match ->
                                    val isFav = favorites.any { it.id == match.id }
                                    Box(modifier = Modifier.width(280.dp)) {
                                        StreamCard(
                                            match = match,
                                            onClick = { onMatchClick(match) },
                                            onFavoriteClick = { localDataManager.toggleFavorite(match) },
                                            isFavorite = isFav
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // RECOMMENDED FOR YOU
                    if (recommendedMatches.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            SectionHeader(
                                title = "RECOMMENDED",
                                subtitle = "Handpicked sports live streams"
                            )
                        }
                        items(recommendedMatches) { match ->
                            val isFav = favorites.any { it.id == match.id }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                            ) {
                                StreamCard(
                                    match = match,
                                    onClick = { onMatchClick(match) },
                                    onFavoriteClick = { localDataManager.toggleFavorite(match) },
                                    isFavorite = isFav
                                )
                            }
                        }
                    }
                }
            }
        }

        // App Update Dialog (Triggered when Firebase latestVersionCode > installedVersionCode)
        if (showUpdateDialog && adminConfig != null) {
            val versionName = adminConfigManager?.getInstalledVersionName() ?: "1.0.0"
            AppUpdateDialog(
                config = adminConfig!!,
                currentVersionName = versionName,
                onDismiss = { showUpdateDialog = false }
            )
        }
    }
}

@Composable
private fun HomeTopHeader(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftGlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & LIVE badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "AR SPORTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Football & Cricket • v-a.r.u.n.1",
                        style = MaterialTheme.typography.labelSmall,
                        color = GlassSilverMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Action Icons (Search)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("home_search_icon")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = GlassSilverMuted
        )
    }
}
