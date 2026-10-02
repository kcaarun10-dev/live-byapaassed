package com.example.ui.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocalDataManager
import com.example.data.models.APIMatch
import com.example.data.models.SportCategory
import com.example.ui.components.GlassCategoryCarousel
import com.example.ui.components.SoftGlassSurface
import com.example.ui.components.StreamCard
import com.example.ui.home.HomeUiState
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteFaint
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@Composable
fun SearchScreen(
    viewModel: HomeViewModel,
    localDataManager: LocalDataManager,
    onMatchClick: (APIMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val favorites by localDataManager.favoritesFlow.collectAsState()
    val recentSearches by localDataManager.recentSearchesFlow.collectAsState()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf(SportCategory.ALL) }

    val allMatches = when (val state = uiState) {
        is HomeUiState.Success -> state.liveMatches + state.todayMatches
        else -> emptyList()
    }

    val searchResults = if (searchQuery.isBlank() && selectedCategoryFilter == SportCategory.ALL) {
        emptyList()
    } else {
        allMatches.filter { match ->
            val matchesQuery = searchQuery.isBlank() ||
                    match.title.contains(searchQuery, ignoreCase = true) ||
                    match.category.contains(searchQuery, ignoreCase = true) ||
                    match.teams?.home?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    match.teams?.away?.name?.contains(searchQuery, ignoreCase = true) == true

            val matchesCategory = selectedCategoryFilter == SportCategory.ALL ||
                    match.category.equals(selectedCategoryFilter.apiSlug, ignoreCase = true)

            matchesQuery && matchesCategory
        }.distinctBy { it.id }
    }

    Scaffold(
        containerColor = GlassMidnight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Screen Header
            item {
                Text(
                    text = "SEARCH STREAMS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            }

            // Floating Glass Search Input
            item {
                SoftGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = GlassWhiteSubtle
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search Football & Cricket matches, teams…",
                                color = GlassTextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GlassSilverMuted
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = GlassSilverMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                localDataManager.addRecentSearch(searchQuery)
                                focusManager.clearFocus()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
            }

            // Category Filter Pills
            item {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    GlassCategoryCarousel(
                        categories = SportCategory.CATEGORIES,
                        selectedCategory = selectedCategoryFilter,
                        onCategorySelected = { selectedCategoryFilter = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // Recent Searches (if query is empty)
            if (searchQuery.isBlank() && recentSearches.isNotEmpty() && selectedCategoryFilter == SportCategory.ALL) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT SEARCHES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassSilverMuted,
                            letterSpacing = 1.sp
                        )
                        TextButton(onClick = { localDataManager.clearRecentSearches() }) {
                            Text(
                                text = "Clear All",
                                color = GlassTextSecondary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        items(recentSearches) { term ->
                            Surface(
                                color = GlassWhiteSubtle,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                                modifier = Modifier.clickable {
                                    searchQuery = term
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = GlassSilverMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = term,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Trending Football & Cricket Tags
            if (searchQuery.isBlank() && selectedCategoryFilter == SportCategory.ALL) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Text(
                            text = "TRENDING FOOTBALL & CRICKET",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GlassSilverMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        val trendingTags = listOf(
                            "Premier League", "Champions League", "IPL", "T20 World Cup",
                            "Real Madrid", "Barcelona", "Arsenal", "Manchester United",
                            "India vs England", "Test Match", "La Liga", "Serie A"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(trendingTags) { tag ->
                                Surface(
                                    color = GlassWhiteSubtle,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderSubtle),
                                    modifier = Modifier.clickable {
                                        searchQuery = tag
                                        localDataManager.addRecentSearch(tag)
                                    }
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Results
            if (searchQuery.isNotBlank() || selectedCategoryFilter != SportCategory.ALL) {
                item {
                    Text(
                        text = "SEARCH RESULTS (${searchResults.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GlassSilverMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                    )
                }

                if (searchResults.isEmpty()) {
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
                                    text = "No Live Streams Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Try searching for a different sport, team, or event.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(searchResults) { match ->
                        val isFav = favorites.any { it.id == match.id }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            StreamCard(
                                match = match,
                                onClick = {
                                    if (searchQuery.isNotBlank()) {
                                        localDataManager.addRecentSearch(searchQuery)
                                    }
                                    onMatchClick(match)
                                },
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
