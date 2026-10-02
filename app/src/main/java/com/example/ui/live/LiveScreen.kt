package com.example.ui.live

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalDataManager
import com.example.data.models.APIMatch
import com.example.data.models.SportCategory
import com.example.ui.components.GlassCategoryCarousel
import com.example.ui.components.GlassErrorState
import com.example.ui.components.LiveBadge
import com.example.ui.components.MatchStatusBadge
import com.example.ui.components.SoftGlassSurface
import com.example.ui.components.StreamCard
import com.example.ui.home.HomeUiState
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassIceBlue
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteFaint
import com.example.ui.theme.GlassWhiteStrong

enum class MatchFilterType(val label: String) {
    ALL("ALL MATCHES"),
    LIVE("LIVE NOW"),
    UPCOMING("UPCOMING")
}

@Composable
fun LiveScreen(
    viewModel: HomeViewModel,
    localDataManager: LocalDataManager,
    onMatchClick: (APIMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites by localDataManager.favoritesFlow.collectAsState()
    var selectedCategory by remember { mutableStateOf(SportCategory.ALL) }
    var selectedFilterType by remember { mutableStateOf(MatchFilterType.ALL) }

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
                            text = "Loading Football & Cricket streams…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTextSecondary
                        )
                    }
                }
            }

            is HomeUiState.Error -> {
                GlassErrorState(
                    title = "Streams Unavailable",
                    message = state.message,
                    onRetry = { viewModel.loadMatches() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is HomeUiState.Success -> {
                // Combine and deduplicate
                val allMatches = (state.liveMatches + state.todayMatches).distinctBy { it.id }

                // Category filter (Football / Cricket / All)
                val categoryFiltered = if (selectedCategory == SportCategory.ALL) {
                    allMatches
                } else {
                    allMatches.filter { it.category.equals(selectedCategory.apiSlug, ignoreCase = true) }
                }

                // Status filter (Live / Upcoming / All)
                val filteredList = when (selectedFilterType) {
                    MatchFilterType.ALL -> categoryFiltered
                    MatchFilterType.LIVE -> categoryFiltered.filter { it.isLive() }
                    MatchFilterType.UPCOMING -> categoryFiltered.filter { it.isUpcoming() }
                }

                val liveCount = categoryFiltered.count { it.isLive() }
                val upcomingCount = categoryFiltered.count { it.isUpcoming() }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    // Screen Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "BROADCASTS",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$liveCount Live • $upcomingCount Upcoming",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassSilverMuted
                                )
                            }
                            LiveBadge(label = "$liveCount LIVE")
                        }
                    }

                    // Category Pill Selector (Football & Cricket)
                    item {
                        GlassCategoryCarousel(
                            categories = SportCategory.CATEGORIES,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                    }

                    // Match Status Filter Tabs (ALL / LIVE NOW / UPCOMING)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MatchFilterType.values().forEach { filter ->
                                val isSelected = selectedFilterType == filter
                                Surface(
                                    color = if (isSelected) GlassWhiteStrong else GlassWhiteFaint,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isSelected) GlassBorderActive else GlassBorderSubtle),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFilterType = filter }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = filter.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else GlassTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Streams List
                    if (filteredList.isEmpty()) {
                        item {
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
                                        text = "No Matches Found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "There are no ${selectedFilterType.label.lowercase()} matches in ${selectedCategory.name} right now.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GlassTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredList) { match ->
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
