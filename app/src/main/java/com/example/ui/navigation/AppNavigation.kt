package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.api.ApiClient
import com.example.data.local.LocalDataManager
import com.example.data.remote.AdminConfig
import com.example.data.remote.AdminConfigManager
import com.example.data.repository.StreamedRepository
import com.example.ui.category.CategoryScreen
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.GlassNavigationDock
import com.example.ui.favorites.FavoritesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.live.LiveScreen
import com.example.ui.picker.StreamPickerScreen
import com.example.ui.picker.StreamPickerViewModel
import com.example.ui.player.PlayerScreen
import com.example.ui.search.SearchScreen
import com.example.ui.theme.GlassMidnight
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AppRoutes {
    const val HOME = "home"
    const val LIVE = "live"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val CATEGORY = "category/{slug}"
    const val STREAMS = "streams/{matchId}"
    const val PLAYER = "player/{encodedEmbedUrl}?title={title}&hd={hd}&poster={poster}"

    fun buildCategoryRoute(slug: String): String {
        return "category/$slug"
    }

    fun buildStreamsRoute(matchId: String): String {
        return "streams/$matchId"
    }

    fun buildPlayerRoute(
        embedUrl: String,
        title: String,
        isHd: Boolean = true,
        poster: String? = null
    ): String {
        val encodedUrl = URLEncoder.encode(embedUrl, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val encodedPoster = URLEncoder.encode(poster ?: "", StandardCharsets.UTF_8.toString())
        return "player/$encodedUrl?title=$encodedTitle&hd=$isHd&poster=$encodedPoster"
    }
}

@Composable
fun AppNavigation(
    repository: StreamedRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val localDataManager = remember { LocalDataManager(context.applicationContext) }
    val adminConfigManager = remember { com.example.data.remote.AdminConfigManager(context.applicationContext) }

    val homeViewModel: HomeViewModel = remember {
        HomeViewModel(repository)
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.HOME

    // Show floating navigation dock only on top-level tabs
    val isTopLevelDestination = currentRoute in listOf(
        AppRoutes.HOME,
        AppRoutes.LIVE,
        AppRoutes.SEARCH,
        AppRoutes.FAVORITES
    )

    val adminConfig by adminConfigManager.configFlow.collectAsState()
    val isUpdateAvailable by adminConfigManager.updateAvailableFlow.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        adminConfigManager.fetchRemoteConfig()
    }

    androidx.compose.runtime.LaunchedEffect(isUpdateAvailable) {
        if (isUpdateAvailable) {
            showUpdateDialog = true
        }
    }

    Scaffold(
        containerColor = GlassMidnight,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = AppRoutes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                // HOME
                composable(AppRoutes.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        localDataManager = localDataManager,
                        adminConfigManager = adminConfigManager,
                        onMatchClick = { match ->
                            repository.cacheMatch(match)
                            localDataManager.addToHistory(match)
                            navController.navigate(AppRoutes.buildStreamsRoute(match.id))
                        },
                        onSearchClick = {
                            navController.navigate(AppRoutes.SEARCH)
                        },
                        onCategoryClick = { category ->
                            navController.navigate(AppRoutes.buildCategoryRoute(category.apiSlug))
                        }
                    )
                }

                // LIVE
                composable(AppRoutes.LIVE) {
                    LiveScreen(
                        viewModel = homeViewModel,
                        localDataManager = localDataManager,
                        onMatchClick = { match ->
                            repository.cacheMatch(match)
                            localDataManager.addToHistory(match)
                            navController.navigate(AppRoutes.buildStreamsRoute(match.id))
                        }
                    )
                }

                // SEARCH
                composable(AppRoutes.SEARCH) {
                    SearchScreen(
                        viewModel = homeViewModel,
                        localDataManager = localDataManager,
                        onMatchClick = { match ->
                            repository.cacheMatch(match)
                            localDataManager.addToHistory(match)
                            navController.navigate(AppRoutes.buildStreamsRoute(match.id))
                        }
                    )
                }

                // FAVORITES
                composable(AppRoutes.FAVORITES) {
                    FavoritesScreen(
                        localDataManager = localDataManager,
                        onMatchClick = { match ->
                            repository.cacheMatch(match)
                            localDataManager.addToHistory(match)
                            navController.navigate(AppRoutes.buildStreamsRoute(match.id))
                        }
                    )
                }

                // CATEGORY SCREEN
                composable(
                    route = AppRoutes.CATEGORY,
                    arguments = listOf(
                        navArgument("slug") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val slug = backStackEntry.arguments?.getString("slug") ?: "all"
                    CategoryScreen(
                        categorySlug = slug,
                        repository = repository,
                        localDataManager = localDataManager,
                        onBackClick = { navController.popBackStack() },
                        onMatchClick = { match ->
                            repository.cacheMatch(match)
                            localDataManager.addToHistory(match)
                            navController.navigate(AppRoutes.buildStreamsRoute(match.id))
                        }
                    )
                }

                // STREAMS (Stream Details & Server Picker)
                composable(
                    route = AppRoutes.STREAMS,
                    arguments = listOf(
                        navArgument("matchId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
                    val pickerViewModel = remember(matchId) {
                        StreamPickerViewModel(matchId, repository)
                    }
                    StreamPickerScreen(
                        viewModel = pickerViewModel,
                        localDataManager = localDataManager,
                        onBackClick = { navController.popBackStack() },
                        onStreamSelected = { stream, match ->
                            localDataManager.addToHistory(match, stream)
                            val fullPosterUrl = ApiClient.getPosterUrl(match.poster)
                            navController.navigate(
                                AppRoutes.buildPlayerRoute(
                                    embedUrl = stream.embedUrl,
                                    title = match.title,
                                    isHd = stream.hd,
                                    poster = fullPosterUrl
                                )
                            )
                        }
                    )
                }

                // FULLSCREEN PLAYER
                composable(
                    route = AppRoutes.PLAYER,
                    arguments = listOf(
                        navArgument("encodedEmbedUrl") { type = NavType.StringType },
                        navArgument("title") {
                            type = NavType.StringType
                            defaultValue = "Live Sports Broadcast"
                        },
                        navArgument("hd") {
                            type = NavType.BoolType
                            defaultValue = true
                        },
                        navArgument("poster") {
                            type = NavType.StringType
                            defaultValue = ""
                        }
                    )
                ) { backStackEntry ->
                    val encodedEmbedUrl = backStackEntry.arguments?.getString("encodedEmbedUrl") ?: ""
                    val encodedTitle = backStackEntry.arguments?.getString("title") ?: "Live Stream"
                    val isHd = backStackEntry.arguments?.getBoolean("hd") ?: true
                    val encodedPoster = backStackEntry.arguments?.getString("poster") ?: ""

                    val embedUrl = remember(encodedEmbedUrl) {
                        try {
                            URLDecoder.decode(encodedEmbedUrl, StandardCharsets.UTF_8.toString())
                        } catch (_: Exception) {
                            encodedEmbedUrl
                        }
                    }

                    val title = remember(encodedTitle) {
                        try {
                            URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
                        } catch (_: Exception) {
                            encodedTitle
                        }
                    }

                    val posterUrl = remember(encodedPoster) {
                        try {
                            URLDecoder.decode(encodedPoster, StandardCharsets.UTF_8.toString()).ifBlank { null }
                        } catch (_: Exception) {
                            null
                        }
                    }

                    PlayerScreen(
                        embedUrl = embedUrl,
                        matchTitle = title,
                        isHd = isHd,
                        posterUrl = posterUrl,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            // Floating Soft Glass Navigation Dock
            if (isTopLevelDestination) {
                GlassNavigationDock(
                    currentRoute = currentRoute,
                    onNavigate = { targetRoute ->
                        if (currentRoute != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(AppRoutes.HOME) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Version Mismatch / Update Dialog across entire app
            val activeConfig = adminConfig
            if (showUpdateDialog && activeConfig != null) {
                val versionName = adminConfigManager.getInstalledVersionName()
                AppUpdateDialog(
                    config = activeConfig,
                    currentVersionName = versionName,
                    onDismiss = { showUpdateDialog = false }
                )
            }
        }
    }
}
