package com.example.ui.favorites

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.SoftGlassSurface
import com.example.ui.components.StreamCard
import com.example.ui.theme.GlassBorderActive
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassLiveRed
import com.example.ui.theme.GlassMidnight
import com.example.ui.theme.GlassObsidian
import com.example.ui.theme.GlassSilverMuted
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassTextMuted
import com.example.ui.theme.GlassTextSecondary
import com.example.ui.theme.GlassWhiteStrong
import com.example.ui.theme.GlassWhiteSubtle

@Composable
fun FavoritesScreen(
    localDataManager: LocalDataManager,
    onMatchClick: (APIMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by localDataManager.favoritesFlow.collectAsState()
    val watchHistory by localDataManager.historyFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: LIVE, 1: UPCOMING, 2: HISTORY
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val tabs = listOf("FAVORITES", "HISTORY")

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
            // Header
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
                            text = "MY SPORTS",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Saved locally on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlassSilverMuted
                        )
                    }

                    if (selectedTab == 1 && watchHistory.isNotEmpty()) {
                        IconButton(onClick = { showClearHistoryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = GlassSilverMuted
                            )
                        }
                    }
                }
            }

            // Glass Tabs
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tabs.forEachIndexed { index, tabName ->
                        val isSelected = selectedTab == index
                        val count = if (index == 0) favorites.size else watchHistory.size

                        Surface(
                            color = if (isSelected) GlassWhiteStrong else GlassWhiteSubtle,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GlassBorderActive else GlassBorderSubtle
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = index }
                                .testTag("favorites_tab_$index")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tabName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) Color.White else GlassTextMuted,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (isSelected) Color.White else GlassWhiteSubtle,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else GlassTextSecondary,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // FAVORITES TAB
                if (favorites.isEmpty()) {
                    item {
                        SoftGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = GlassSilverMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Favorites Added Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap the heart icon on any live stream to save it here for instant access.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(favorites) { match ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            StreamCard(
                                match = match,
                                onClick = { onMatchClick(match) },
                                onFavoriteClick = { localDataManager.toggleFavorite(match) },
                                isFavorite = true
                            )
                        }
                    }
                }
            } else {
                // HISTORY TAB
                if (watchHistory.isEmpty()) {
                    item {
                        SoftGlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 24.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = GlassSilverMuted,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Watch History is Empty",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Streams you watch will automatically be recorded here so you can easily resume them.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GlassTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(watchHistory) { item ->
                        val isFav = favorites.any { it.id == item.match.id }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            StreamCard(
                                match = item.match,
                                onClick = { onMatchClick(item.match) },
                                onFavoriteClick = { localDataManager.toggleFavorite(item.match) },
                                isFavorite = isFav
                            )
                        }
                    }
                }
            }
        }

        // Clear History Glass Confirmation Dialog
        if (showClearHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                containerColor = GlassObsidian,
                titleContentColor = Color.White,
                textContentColor = GlassTextSecondary,
                title = {
                    Text(
                        text = "Clear Watch History?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = "This will remove all your locally recorded streams from watch history.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            localDataManager.clearHistory()
                            showClearHistoryDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlassLiveRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text("Cancel", color = GlassSilverMuted)
                    }
                }
            )
        }
    }
}
