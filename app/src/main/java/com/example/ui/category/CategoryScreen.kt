package com.example.ui.category

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalDataManager
import com.example.data.models.APIMatch
import com.example.data.models.CricketEventWithChannel
import com.example.data.models.SportCategory
import com.example.data.models.toAPIMatch
import com.example.data.repository.StreamedRepository
import com.example.ui.components.CricketEventCard
import com.example.ui.components.GlassErrorState
import com.example.ui.components.LiveBadge
import com.example.ui.components.SoftGlassSurface
import com.example.ui.components.StreamCard
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

sealed interface CategoryUiState {
    object Loading : CategoryUiState
    data class GeneralSuccess(val matches: List<APIMatch>) : CategoryUiState
    data class CricketSuccess(val cricketEvents: List<CricketEventWithChannel>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}

@Composable
fun CategoryScreen(
    categorySlug: String,
    repository: StreamedRepository,
    localDataManager: LocalDataManager,
    onBackClick: () -> Unit,
    onMatchClick: (APIMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val category = remember(categorySlug) { SportCategory.findBySlug(categorySlug) }
    val favorites by localDataManager.favoritesFlow.collectAsState()

    var uiState by remember { mutableStateOf<CategoryUiState>(CategoryUiState.Loading) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: LIVE, 1: UPCOMING, 2: POPULAR
    val tabs = listOf("LIVE", "UPCOMING", "POPULAR")

    LaunchedEffect(categorySlug) {
        uiState = CategoryUiState.Loading
        if (categorySlug.equals("cricket", ignoreCase = true)) {
            val result = repository.cricketRepo.getCricketEventsWithChannels()
            result.onSuccess { cricketList ->
                uiState = CategoryUiState.CricketSuccess(cricketList)
            }.onFailure { err ->
                uiState = CategoryUiState.Error(err.message ?: "Failed to load Cricket streams.")
            }
        } else {
            val result = repository.getMatchesBySport(category.apiSlug)
            result.onSuccess { matches ->
                uiState = CategoryUiState.GeneralSuccess(matches)
            }.onFailure { err ->
                uiState = CategoryUiState.Error(err.message ?: "Failed to load ${category.name} streams.")
            }
        }
    }

    Scaffold(
        containerColor = GlassMidnight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (val state = uiState) {
            is CategoryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading ${category.name} live feeds…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            is CategoryUiState.Error -> {
                GlassErrorState(
                    title = "Streams Unavailable",
                    message = state.message,
                    onRetry = {
                        uiState = CategoryUiState.Loading
                    },
                    onGoBack = onBackClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is CategoryUiState.CricketSuccess -> {
                val cricketEvents = state.cricketEvents
                val displayedEvents = when (selectedTab) {
                    0 -> cricketEvents.filter { it.event.tag.equals("LIVE", ignoreCase = true) }.ifEmpty { cricketEvents }
                    1 -> cricketEvents.filter { !it.event.tag.equals("LIVE", ignoreCase = true) }.ifEmpty { cricketEvents }
                    else -> cricketEvents.filter { (it.event.viewers ?: 0) > 0 }.ifEmpty { cricketEvents }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    // Category Hero Banner
                    item {
                        CategoryHeroBanner(
                            categoryName = "CRICKET",
                            streamCount = cricketEvents.size,
                            onBackClick = onBackClick
                        )
                    }

                    // Glass Tabs
                    item {
                        GlassTabsRow(
                            tabs = tabs,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }

                    if (displayedEvents.isEmpty()) {
                        item {
                            EmptyStateCard(message = "No cricket live matches found at this time.")
                        }
                    } else {
                        items(displayedEvents) { eventWithChannel ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                CricketEventCard(
                                    eventWithChannel = eventWithChannel,
                                    onWatchClick = {
                                        val apiMatch = eventWithChannel.toAPIMatch()
                                        repository.cacheMatch(apiMatch)
                                        localDataManager.addToHistory(apiMatch)
                                        onMatchClick(apiMatch)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            is CategoryUiState.GeneralSuccess -> {
                val matches = state.matches
                val displayedMatches = when (selectedTab) {
                    0 -> matches // LIVE
                    1 -> matches.reversed() // UPCOMING
                    else -> matches.filter { it.popular || it.sources.size > 1 }.ifEmpty { matches }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 48.dp)
                ) {
                    // Category Hero Banner
                    item {
                        CategoryHeroBanner(
                            categoryName = category.name,
                            streamCount = matches.size,
                            onBackClick = onBackClick
                        )
                    }

                    // Glass Tabs (LIVE | UPCOMING | POPULAR)
                    item {
                        GlassTabsRow(
                            tabs = tabs,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }

                    if (displayedMatches.isEmpty()) {
                        item {
                            EmptyStateCard(message = "No streams in this tab")
                        }
                    } else {
                        items(displayedMatches) { match ->
                            val isFav = favorites.any { it.id == match.id }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
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
    }
}

@Composable
private fun CategoryHeroBanner(
    categoryName: String,
    streamCount: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161C2A),
                        GlassObsidian,
                        GlassMidnight
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("category_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                LiveBadge(label = "$streamCount STREAMS")
            }

            Column {
                Text(
                    text = categoryName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Official live video broadcasts & match feeds",
                    style = MaterialTheme.typography.bodySmall,
                    color = GlassSilverMuted
                )
            }
        }
    }
}

@Composable
private fun GlassTabsRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tabName ->
            val isSelected = selectedTab == index
            Surface(
                color = if (isSelected) GlassWhiteStrong else GlassWhiteSubtle,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GlassBorderActive else GlassBorderSubtle
                ),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) Color.White else GlassTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    SoftGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
