package tv.darshini.app.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tv.darshini.app.diagnostics.ExternalPlayerProbeActivity
import tv.darshini.app.ui.model.isArchivePlayable
import tv.darshini.domain.model.Channel
import tv.darshini.domain.model.Episode
import tv.darshini.domain.model.ExternalPlaybackMode
import tv.darshini.domain.model.Movie
import tv.darshini.domain.repository.ChannelRepository
import tv.darshini.app.ui.screens.dashboard.DashboardScreen
import tv.darshini.app.ui.screens.multiview.MultiViewScreen
import tv.darshini.app.ui.screens.home.HomeScreen
import tv.darshini.app.ui.screens.movies.MoviesScreen
import tv.darshini.app.ui.screens.player.PlayerScreen
import tv.darshini.app.ui.screens.plugins.PluginsScreen
import tv.darshini.app.ui.screens.provider.ProviderSetupScreen
import tv.darshini.app.ui.screens.series.SeriesScreen
import tv.darshini.app.ui.screens.settings.SettingsScreen
import tv.darshini.app.ui.screens.welcome.WelcomeScreen
import tv.darshini.app.ui.screens.downloads.DownloadsScreen
import tv.darshini.app.MainActivity
import tv.darshini.domain.model.AppLandingDestination
import tv.darshini.domain.model.AppTopLevelDestination
import tv.darshini.domain.model.MovieDetailPresentationHint
import tv.darshini.domain.model.Series
import tv.darshini.domain.model.SeriesDetailPresentationHint
import java.io.Serializable
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine


private const val PLAYER_REQUEST_KEY = "player_request"
internal const val MOVIE_DETAIL_PRESENTATION_HINT_KEY = "movie_detail_presentation_hint"
internal const val SERIES_DETAIL_PRESENTATION_HINT_KEY = "series_detail_presentation_hint"
private const val TAG = "AppNavigation"

data class PlayerNavigationRequest(
    val streamUrl: String,
    val title: String,
    val channelId: String? = null,
    val internalId: Long = -1L,
    val categoryId: Long? = null,
    val providerId: Long? = null,
    val isVirtual: Boolean = false,
    val combinedProfileId: Long? = null,
    val combinedSourceFilterProviderId: Long? = null,
    val contentType: String = "LIVE",
    val artworkUrl: String? = null,
    val archiveStartMs: Long? = null,
    val archiveEndMs: Long? = null,
    val archiveTitle: String? = null,
    val returnRoute: String? = null,
    val seriesId: Long? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeId: Long? = null
) : Serializable

object Routes {
    const val PROVIDER_SETUP = "provider_setup?providerId={providerId}&importUri={importUri}"
    const val HOME = "home"
    const val LIVE_TV = "live_tv"
    const val LIVE_TV_DESTINATION = "live_tv?categoryId={categoryId}"
    const val MOVIES = "movies"
    const val SERIES = "series"
    const val DOWNLOADS = "downloads"
    const val EPG = "epg"
    const val EPG_DESTINATION = "epg?categoryId={categoryId}&anchorTime={anchorTime}&favoritesOnly={favoritesOnly}"
    const val SETTINGS = "settings"
    const val SETTINGS_DESTINATION = "settings?backupUri={backupUri}"
    const val PLUGINS = "plugins"
    const val PLAYER = "player"
    const val SEARCH = "search"
    const val SEARCH_DESTINATION = "search?query={query}"
    const val MOVIE_DETAIL = "movie_detail/{movieId}?returnRoute={returnRoute}"
    const val SERIES_DETAIL = "series_detail/{seriesId}?returnRoute={returnRoute}"
    const val WELCOME = "welcome"
    const val PARENTAL_CONTROL_GROUPS = "parental_control_groups/{providerId}"
    const val MULTI_VIEW = "multi_view"


    fun providerSetup(providerId: Long? = null, importUri: String? = null): String {
        val encodedImportUri = Uri.encode(importUri ?: "")
        return "provider_setup?providerId=${providerId ?: -1L}&importUri=$encodedImportUri"
    }
    fun liveTv(categoryId: Long? = null) = if (categoryId == null) LIVE_TV else "$LIVE_TV?categoryId=$categoryId"
    fun epg(categoryId: Long? = null, anchorTime: Long? = null, favoritesOnly: Boolean? = null): String {
        val resolvedCategoryId = categoryId ?: -1L
        val resolvedAnchorTime = anchorTime ?: -1L
        val resolvedFavoritesOnly = favoritesOnly ?: false
        return "$EPG?categoryId=$resolvedCategoryId&anchorTime=$resolvedAnchorTime&favoritesOnly=$resolvedFavoritesOnly"
    }

    fun livePlayer(
        channel: Channel,
        categoryId: Long? = channel.categoryId,
        providerId: Long? = channel.providerId,
        isVirtual: Boolean = false,
        combinedProfileId: Long? = null,
        combinedSourceFilterProviderId: Long? = null,
        returnRoute: String? = null
    ): PlayerNavigationRequest {
        val effectiveCategoryId = categoryId ?: ChannelRepository.ALL_CHANNELS_ID
        return player(
            streamUrl = channel.streamUrl,
            title = channel.name,
            channelId = channel.epgChannelId,
            internalId = channel.id,
            categoryId = effectiveCategoryId,
            providerId = providerId,
            isVirtual = isVirtual,
            combinedProfileId = combinedProfileId,
            combinedSourceFilterProviderId = combinedSourceFilterProviderId,
            contentType = "LIVE",
            returnRoute = returnRoute
        )
    }

    fun moviePlayer(movie: Movie): PlayerNavigationRequest {
        return player(
            streamUrl = movie.streamUrl,
            title = movie.name,
            internalId = movie.id,
            categoryId = movie.categoryId,
            providerId = movie.providerId,
            contentType = "MOVIE",
            artworkUrl = movie.posterUrl ?: movie.backdropUrl
        )
    }

    fun episodePlayer(episode: Episode): PlayerNavigationRequest {
        return player(
            streamUrl = episode.streamUrl,
            title = "${episode.title} - S${episode.seasonNumber}E${episode.episodeNumber}",
            internalId = episode.id,
            providerId = episode.providerId,
            contentType = "SERIES_EPISODE",
            artworkUrl = episode.coverUrl,
            seriesId = episode.seriesId.takeIf { it > 0L },
            seasonNumber = episode.seasonNumber,
            episodeNumber = episode.episodeNumber,
            episodeId = episode.episodeId.takeIf { it > 0L }
        )
    }

    fun search(query: String? = null): String =
        if (query.isNullOrBlank()) SEARCH else "$SEARCH?query=${Uri.encode(query)}"

    fun settings(backupUri: String? = null): String =
        if (backupUri.isNullOrBlank()) SETTINGS else "$SETTINGS?backupUri=${Uri.encode(backupUri)}"

    fun player(
        streamUrl: String,
        title: String,
        channelId: String? = null,
        internalId: Long = -1L,
        categoryId: Long? = null,
        providerId: Long? = null,
        isVirtual: Boolean = false,
        combinedProfileId: Long? = null,
        combinedSourceFilterProviderId: Long? = null,
        contentType: String = "LIVE",
        artworkUrl: String? = null,
        archiveStartMs: Long? = null,
        archiveEndMs: Long? = null,
        archiveTitle: String? = null,
        returnRoute: String? = null,
        seriesId: Long? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeId: Long? = null
    ): PlayerNavigationRequest {
        return PlayerNavigationRequest(
            streamUrl = streamUrl,
            title = title,
            channelId = channelId,
            internalId = internalId,
            categoryId = categoryId,
            providerId = providerId,
            isVirtual = isVirtual,
            combinedProfileId = combinedProfileId,
            combinedSourceFilterProviderId = combinedSourceFilterProviderId,
            contentType = contentType,
            artworkUrl = artworkUrl,
            archiveStartMs = archiveStartMs,
            archiveEndMs = archiveEndMs,
            archiveTitle = archiveTitle,
            returnRoute = returnRoute,
            seriesId = seriesId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeId = episodeId
        )
    }

    fun movieDetail(movieId: Long, returnRoute: String? = null) =
        "movie_detail/$movieId?returnRoute=${Uri.encode(returnRoute ?: "")}"
    fun seriesDetail(seriesId: Long, returnRoute: String? = null) =
        "series_detail/$seriesId?returnRoute=${Uri.encode(returnRoute ?: "")}"
    fun parentalControlGroups(providerId: Long) = "parental_control_groups/$providerId"
}

/** Accepts app-supported media schemes while still rejecting obviously unsafe ones. */
private fun isStreamUrlSafe(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val scheme = url.substringBefore("://").lowercase()
    return scheme in setOf("http", "https", "rtsp", "rtmp", "rtsps", "mms", "xtream", "stalker", "content", "file")
}

internal fun safePlayerNavigationRequest(request: PlayerNavigationRequest?): PlayerNavigationRequest? =
    request?.takeIf { isStreamUrlSafe(it.streamUrl) }

/** Navigate only when the current destination is fully resumed – prevents double-navigation during transitions. */
private fun NavHostController.navigateIfResumed(route: String, builder: NavOptionsBuilder.() -> Unit = {}): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return false
    navigate(route, builder)
    return true
}

private suspend fun Lifecycle.awaitResumed() {
    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) return
    suspendCancellableCoroutine { continuation ->
        lateinit var observer: LifecycleEventObserver
        observer = LifecycleEventObserver { _, _ ->
            when {
                currentState.isAtLeast(Lifecycle.State.RESUMED) -> {
                    removeObserver(observer)
                    if (continuation.isActive) continuation.resume(Unit)
                }
                currentState == Lifecycle.State.DESTROYED -> {
                    removeObserver(observer)
                    continuation.cancel()
                }
            }
        }
        addObserver(observer)
        continuation.invokeOnCancellation { removeObserver(observer) }
    }
}

private fun NavHostController.navigateToPlayer(request: PlayerNavigationRequest): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return false
    currentBackStackEntry?.savedStateHandle?.set(PLAYER_REQUEST_KEY, request)
    navigate(Routes.PLAYER) { launchSingleTop = true }
    return true
}

private fun NavHostController.navigateToMovieDetail(movie: Movie, returnRoute: String? = null): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return false
    currentBackStackEntry?.savedStateHandle?.set(MOVIE_DETAIL_PRESENTATION_HINT_KEY, movie.toMovieDetailPresentationHint())
    navigate(Routes.movieDetail(movie.id, returnRoute))
    return true
}

private fun Movie.toMovieDetailPresentationHint(): MovieDetailPresentationHint? {
    if (variants.isEmpty()) return null
    return MovieDetailPresentationHint(
        providerId = providerId,
        logicalGroupId = logicalGroupId,
        variants = variants,
        duplicateConfidence = duplicateConfidence
    )
}

private fun NavHostController.navigateToSeriesDetail(series: Series, returnRoute: String? = null): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return false
    currentBackStackEntry?.savedStateHandle?.set(SERIES_DETAIL_PRESENTATION_HINT_KEY, series.toSeriesDetailPresentationHint())
    navigate(Routes.seriesDetail(series.id, returnRoute))
    return true
}

private fun Series.toSeriesDetailPresentationHint(): SeriesDetailPresentationHint? {
    if (variants.isEmpty()) return null
    return SeriesDetailPresentationHint(
        providerId = providerId,
        logicalGroupId = logicalGroupId,
        variants = variants,
        duplicateConfidence = duplicateConfidence
    )
}

private fun NavHostController.navigateToExternalPlayer(request: PlayerNavigationRequest): Boolean {
    if (currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return false
    currentBackStackEntry?.savedStateHandle?.set(PLAYER_REQUEST_KEY, request)
    navigate(Routes.PLAYER) { launchSingleTop = true }
    return true
}

internal fun AppLandingDestination.toAppRoute(): String = when (this) {
    AppLandingDestination.HOME -> Routes.HOME
    AppLandingDestination.LIVE_TV -> Routes.LIVE_TV
    AppLandingDestination.MOVIES -> Routes.MOVIES
    AppLandingDestination.SERIES -> Routes.SERIES
    AppLandingDestination.GUIDE -> Routes.EPG
    AppLandingDestination.DOWNLOADS -> Routes.DOWNLOADS
    AppLandingDestination.PLUGINS -> Routes.PLUGINS
    AppLandingDestination.SETTINGS -> Routes.SETTINGS
}

internal fun AppTopLevelDestination.toAppRoute(): String = when (this) {
    AppTopLevelDestination.HOME -> Routes.HOME
    AppTopLevelDestination.LIVE_TV -> Routes.LIVE_TV
    AppTopLevelDestination.MOVIES -> Routes.MOVIES
    AppTopLevelDestination.SERIES -> Routes.SERIES
    AppTopLevelDestination.DOWNLOADS -> Routes.DOWNLOADS
    AppTopLevelDestination.GUIDE -> Routes.EPG
    AppTopLevelDestination.SEARCH -> Routes.SEARCH
    AppTopLevelDestination.PLUGINS -> Routes.PLUGINS
    AppTopLevelDestination.SETTINGS -> Routes.SETTINGS
}

@Composable
fun AppNavigation(mainActivity: MainActivity) {
    Log.d("AppNavigation", "AppNavigation composable called")
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val externalNavigationRequest = mainActivity.externalNavigationRequestFlow.collectAsStateWithLifecycle().value
    val topLevelDestinations = mainActivity.preferencesRepository.appTopLevelDestinations
        .collectAsStateWithLifecycle(initialValue = AppTopLevelDestination.defaultOrder)
        .value
    val appLandingDestination = mainActivity.preferencesRepository.appLandingDestination
        .collectAsStateWithLifecycle(initialValue = AppLandingDestination.HOME)
        .value
    val landingRoute = AppTopLevelDestination.resolveLandingDestination(
        preferred = appLandingDestination,
        destinations = topLevelDestinations
    ).toAppRoute()
    Log.d("AppNavigation", "landingRoute = $landingRoute, currentBackStackEntry = ${currentBackStackEntry?.destination?.route}")

    LaunchedEffect(externalNavigationRequest, currentBackStackEntry) {
        val entry = currentBackStackEntry ?: return@LaunchedEffect
        entry.lifecycle.awaitResumed()
        when (val request = externalNavigationRequest) {
            is ExternalNavigationRequest.Player -> {
                if (navController.navigateToExternalPlayer(request.request)) {
                    mainActivity.clearExternalNavigationRequest()
                }
            }

            is ExternalNavigationRequest.Destination -> {
                if (navController.navigateIfResumed(request.destination.toRoute()) { launchSingleTop = true }) {
                    mainActivity.clearExternalNavigationRequest()
                }
            }

            is ExternalNavigationRequest.ImportM3u -> {
                if (navController.navigateIfResumed(Routes.providerSetup(importUri = request.uri)) { launchSingleTop = true }) {
                    mainActivity.clearExternalNavigationRequest()
                }
            }

            is ExternalNavigationRequest.ImportBackup -> {
                if (navController.navigateIfResumed(Routes.settings(backupUri = request.uri)) { launchSingleTop = true }) {
                    mainActivity.clearExternalNavigationRequest()
                }
            }

            is ExternalNavigationRequest.Search -> {
                if (navController.navigateIfResumed(Routes.search(request.query)) { launchSingleTop = true }) {
                    mainActivity.clearExternalNavigationRequest()
                }
            }

            null -> Unit
        }
    }

    // NAV-M02/NAV-H02: Single helper replacing repeated tab lambdas without serializing
    // each tab's full UI tree into saved state on every switch.
    fun tabNavigate(route: String) {
        val entry = navController.currentBackStackEntry ?: return
        if (!entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        val currentRoute = entry.destination?.route
        if (currentRoute == route || currentRoute?.startsWith("$route?") == true) return

        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Helper: launch content in an external player or show chooser based on mode
    // - channel content: pass channelId (resolved via ChannelRepository)
    // - movie/episode content: pass movieId (resolved via MovieRepository)
    // - if streamUrl is an already-resolved HTTP URL, pass it directly (skips repo lookup)
    fun launchExternalPlayerForChannel(
        externalPlaybackMode: ExternalPlaybackMode,
        channelId: Long = -1L,
        movieId: Long = -1L,
        streamUrl: String? = null
    ) {
        Log.d("AppNavigation", "launchExternalPlayer: mode=$externalPlaybackMode channelId=$channelId movieId=$movieId hasStreamUrl=${streamUrl != null}")
        val intent = Intent(mainActivity, ExternalPlayerProbeActivity::class.java).apply {
            // If we have a resolved HTTP(S) URL, pass it directly
            if (streamUrl != null && !streamUrl.startsWith("xtream://")) {
                putExtra(ExternalPlayerProbeActivity.EXTRA_STREAM_URL, streamUrl)
            } else if (movieId > 0L) {
                // Movie/Episode content: resolve via MovieRepository
                putExtra(ExternalPlayerProbeActivity.EXTRA_MOVIE_ID, movieId)
                if (streamUrl != null) {
                    putExtra(ExternalPlayerProbeActivity.EXTRA_STREAM_URL, streamUrl)
                }
            } else if (channelId > 0L) {
                // Channel content: resolve via ChannelRepository
                putExtra(ExternalPlayerProbeActivity.EXTRA_CHANNEL_ID, channelId)
            }
            if (externalPlaybackMode == ExternalPlaybackMode.ASK_EVERY_TIME) {
                putExtra(ExternalPlayerProbeActivity.EXTRA_SHOW_CHOOSER, true)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        mainActivity.startActivity(intent)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onNavigateToHome = dropUnlessResumed {
                    navController.navigate(landingRoute) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToSetup = dropUnlessResumed {
                    navController.navigate(Routes.providerSetup()) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.PROVIDER_SETUP,
            arguments = listOf(
                navArgument("providerId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("importUri") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments?.getLong("providerId")?.takeIf { it != -1L }
            val importUri = backStackEntry.arguments?.getString("importUri")?.takeIf { it.isNotBlank() }
            
            ProviderSetupScreen(
                editProviderId = providerId,
                initialImportUri = importUri,
                onBack = { navController.popBackStack() },
                onProviderAdded = dropUnlessResumed {
                    navController.navigate(landingRoute) {
                        popUpTo(Routes.PROVIDER_SETUP) { inclusive = true }
                    }
                }
            )
        }
// ...

        composable(Routes.HOME) {
            val externalPlaybackMode = mainActivity.preferencesRepository.playerExternalPlaybackMode
                .collectAsStateWithLifecycle(ExternalPlaybackMode.INTERNAL_PLAYER).value
            Log.d("AppNavigation", "HOME composable: externalPlaybackMode = $externalPlaybackMode")
            DashboardScreen(
                onNavigate = { route -> tabNavigate(route) },
                onAddProvider = dropUnlessResumed {
                    navController.navigate(Routes.providerSetup(null))
                },
                onRecentChannelClick = { channel, combinedProfileId ->
                    Log.d("AppNavigation", "onRecentChannelClick: mode = $externalPlaybackMode, channel = ${channel.name}")
                    when (externalPlaybackMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            Log.d("AppNavigation", "Navigating to INTERNAL player (recent channel)")
                            navController.navigateToPlayer(
                                Routes.livePlayer(
                                    channel = channel,
                                    categoryId = tv.darshini.domain.model.VirtualCategoryIds.RECENT,
                                    providerId = channel.providerId,
                                    isVirtual = true,
                                    combinedProfileId = combinedProfileId,
                                    returnRoute = Routes.HOME
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player with mode = $externalPlaybackMode (recent channel)")
                            launchExternalPlayerForChannel(externalPlaybackMode, channel.id)
                        }
                    }
                },
                onFavoriteChannelClick = { channel, combinedProfileId ->
                    Log.d("AppNavigation", "onFavoriteChannelClick: mode = $externalPlaybackMode, channel = ${channel.name}")
                    when (externalPlaybackMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            Log.d("AppNavigation", "Navigating to INTERNAL player (favorite channel)")
                            navController.navigateToPlayer(
                                Routes.livePlayer(
                                    channel = channel,
                                    categoryId = tv.darshini.domain.model.VirtualCategoryIds.FAVORITES,
                                    providerId = channel.providerId,
                                    isVirtual = true,
                                    combinedProfileId = combinedProfileId,
                                    returnRoute = Routes.HOME
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player with mode = $externalPlaybackMode (favorite channel)")
                            launchExternalPlayerForChannel(externalPlaybackMode, channel.id)
                        }
                    }
                },
                onMovieClick = { movie ->
                    navController.navigateToMovieDetail(movie, Routes.HOME)
                },
                onSeriesClick = { series ->
                    navController.navigateToSeriesDetail(series, Routes.HOME)
                },
                onPlaybackHistoryClick = { history ->
                    Log.d("AppNavigation", "onPlaybackHistoryClick: mode = $externalPlaybackMode, contentType = ${history.contentType}")
                    when (history.contentType) {
                        tv.darshini.domain.model.ContentType.LIVE -> {
                            when (externalPlaybackMode) {
                                ExternalPlaybackMode.INTERNAL_PLAYER -> {
                                    Log.d("AppNavigation", "Navigating to INTERNAL player (history live)")
                                    navController.navigateToPlayer(
                                        Routes.player(
                                            streamUrl = history.streamUrl,
                                            title = history.title,
                                            internalId = history.contentId,
                                            providerId = history.providerId,
                                            contentType = history.contentType.name,
                                            returnRoute = Routes.HOME
                                        ) as PlayerNavigationRequest
                                    )
                                }
                                ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                                    Log.d("AppNavigation", "Launching EXTERNAL player with mode = $externalPlaybackMode (history live)")
                                    launchExternalPlayerForChannel(externalPlaybackMode, history.contentId)
                                }
                            }
                        }
                        tv.darshini.domain.model.ContentType.MOVIE -> {
                            navController.navigateIfResumed(Routes.movieDetail(history.contentId, Routes.HOME)) { launchSingleTop = true }
                        }
                        tv.darshini.domain.model.ContentType.SERIES -> {
                            navController.navigateIfResumed(Routes.seriesDetail(history.contentId, Routes.HOME)) { launchSingleTop = true }
                        }
                        tv.darshini.domain.model.ContentType.SERIES_EPISODE -> {
                            // Episodes use xtream:// URLs that need SeriesRepository resolution
                            // (not yet available), so always use internal player
                            navController.navigateToPlayer(
                                Routes.player(
                                    streamUrl = history.streamUrl,
                                    title = history.title,
                                    internalId = history.contentId,
                                    providerId = history.providerId,
                                    contentType = history.contentType.name,
                                    returnRoute = Routes.HOME,
                                    seriesId = history.seriesId,
                                    seasonNumber = history.seasonNumber,
                                    episodeNumber = history.episodeNumber
                                )
                            )
                        }
                    }
                },
                currentRoute = Routes.HOME
            )
        }

        composable(
            route = Routes.LIVE_TV_DESTINATION,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry ->
            val initialCategoryId = backStackEntry.arguments?.getLong("categoryId")?.takeIf { it != -1L }
            val externalPlaybackMode = mainActivity.preferencesRepository.playerExternalPlaybackMode
                .collectAsStateWithLifecycle(ExternalPlaybackMode.INTERNAL_PLAYER).value
            Log.d("AppNavigation", "LIVE_TV composable: externalPlaybackMode = $externalPlaybackMode")
            Log.d("AppNavigation", "LIVE_TV: rendering HomeScreen")
            HomeScreen(
                onChannelClick = { channel, category, provider, combinedProfileId, combinedSourceFilterProviderId ->
                    Log.d("AppNavigation", "onChannelClick: mode = $externalPlaybackMode, channel = ${channel.name}")
                    when (externalPlaybackMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            Log.d("AppNavigation", "Navigating to INTERNAL player")
                            navController.navigateToPlayer(
                                Routes.livePlayer(
                                    channel = channel,
                                    categoryId = category?.id,
                                    providerId = provider?.id,
                                    isVirtual = category?.isVirtual == true,
                                    combinedProfileId = combinedProfileId,
                                    combinedSourceFilterProviderId = combinedSourceFilterProviderId,
                                    returnRoute = Routes.liveTv(category?.id)
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player with mode = $externalPlaybackMode")
                            launchExternalPlayerForChannel(externalPlaybackMode, channel.id)
                        }
                    }
                },
                onNavigate = { route -> tabNavigate(route) },
                currentRoute = Routes.LIVE_TV,
                initialCategoryId = initialCategoryId
            )
        }
// ... (rest of file)

        composable(Routes.MOVIES) {
            val externalPlaybackMode = mainActivity.preferencesRepository.playerExternalPlaybackMode
                .collectAsStateWithLifecycle(ExternalPlaybackMode.INTERNAL_PLAYER).value
            Log.d("AppNavigation", "MOVIES: externalPlaybackMode = $externalPlaybackMode")
            MoviesScreen(
                onMovieClick = { movie ->
                    navController.navigateToMovieDetail(movie, Routes.MOVIES)
                },
                onContinueWatchingPlay = { history ->
                    Log.d("AppNavigation", "onContinueWatchingPlay: navigating to movie detail, content = ${history.title}")
                    navController.navigateIfResumed(Routes.movieDetail(history.contentId, Routes.MOVIES)) { launchSingleTop = true }
                },
                onNavigate = { route -> tabNavigate(route) },
                currentRoute = Routes.MOVIES
            )
        }

        composable(Routes.SERIES) {
            SeriesScreen(
                onSeriesClick = { series ->
                    navController.navigateToSeriesDetail(series, Routes.SERIES)
                },
                onSeriesIdClick = { seriesId ->
                    navController.navigateIfResumed(Routes.seriesDetail(seriesId, Routes.SERIES))
                },
                onNavigate = { route -> tabNavigate(route) },
                currentRoute = Routes.SERIES
            )
        }

        composable(Routes.DOWNLOADS) {
            DownloadsScreen(
                onNavigate = { route -> tabNavigate(route) },
                currentRoute = Routes.DOWNLOADS
            )
        }

        composable(
            route = Routes.EPG_DESTINATION,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("anchorTime") { type = NavType.LongType; defaultValue = -1L },
                navArgument("favoritesOnly") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val epgCategoryId = backStackEntry.arguments?.getLong("categoryId")?.takeIf { it != -1L }
            val epgAnchorTime = backStackEntry.arguments?.getLong("anchorTime")?.takeIf { it != -1L }
            val epgFavoritesOnly = backStackEntry.arguments?.getBoolean("favoritesOnly") ?: false
            val epgPlaybackMode = mainActivity.preferencesRepository.playerExternalPlaybackMode
                .collectAsStateWithLifecycle(ExternalPlaybackMode.INTERNAL_PLAYER).value
            tv.darshini.app.ui.screens.epg.FullEpgScreen(
                currentRoute = Routes.EPG,
                initialCategoryId = epgCategoryId,
                initialAnchorTime = epgAnchorTime,
                initialFavoritesOnly = epgFavoritesOnly,
                onPlayChannel = { channel, categoryId, isVirtual, combinedProfileId, returnRoute ->
                    Log.d("AppNavigation", "EPG onPlayChannel: mode = $epgPlaybackMode, channel = ${channel.name}")
                    when (epgPlaybackMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            navController.navigateToPlayer(
                                Routes.livePlayer(
                                    channel = channel,
                                    categoryId = categoryId,
                                    providerId = channel.providerId,
                                    isVirtual = isVirtual,
                                    combinedProfileId = combinedProfileId,
                                    returnRoute = returnRoute
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player from EPG with mode = $epgPlaybackMode")
                            launchExternalPlayerForChannel(epgPlaybackMode, channel.id)
                        }
                    }
                },
                onPlayArchive = { channel, program, categoryId, isVirtual, combinedProfileId, returnRoute ->
                    if (!channel.isArchivePlayable(program)) {
                        return@FullEpgScreen
                    }
                    Log.d("AppNavigation", "EPG onPlayArchive: mode = $epgPlaybackMode, channel = ${channel.name}")
                    when (epgPlaybackMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            navController.navigateToPlayer(
                                Routes.player(
                                    streamUrl = channel.streamUrl,
                                    title = channel.name,
                                    channelId = channel.epgChannelId,
                                    internalId = channel.id,
                                    categoryId = categoryId,
                                    providerId = channel.providerId,
                                    isVirtual = isVirtual,
                                    combinedProfileId = combinedProfileId,
                                    contentType = "LIVE",
                                    archiveStartMs = program.startTime,
                                    archiveEndMs = program.endTime,
                                    archiveTitle = "${channel.name}: ${program.title}",
                                    returnRoute = returnRoute
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player from EPG archive with mode = $epgPlaybackMode")
                            launchExternalPlayerForChannel(epgPlaybackMode, channel.id)
                        }
                    }
                },
                onNavigate = { route -> tabNavigate(route) }
            )
        }

        composable(
            route = Routes.SETTINGS_DESTINATION,
            arguments = listOf(
                navArgument("backupUri") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val backupUri = backStackEntry.arguments?.getString("backupUri")?.takeIf { it.isNotBlank() }
            SettingsScreen(
                onNavigate = { route -> tabNavigate(route) },
                onAddProvider = dropUnlessResumed {
                    navController.navigate(Routes.providerSetup(null))
                },
                onEditProvider = { provider ->
                    navController.navigateIfResumed(Routes.providerSetup(provider.id))
                },
                onNavigateToParentalControl = { providerId ->
                    navController.navigateIfResumed(Routes.parentalControlGroups(providerId))
                },
                currentRoute = Routes.SETTINGS,
                initialBackupImportUri = backupUri
            )
        }

        composable(Routes.PLUGINS) {
            PluginsScreen(
                currentRoute = Routes.PLUGINS,
                onNavigate = { route -> tabNavigate(route) }
            )
        }

        composable(
            route = Routes.PARENTAL_CONTROL_GROUPS,
            arguments = listOf(
                navArgument("providerId") { type = NavType.LongType }
            )
        ) {
            tv.darshini.app.ui.screens.settings.parental.ParentalControlGroupScreen(
                currentRoute = Routes.SETTINGS,
                onNavigate = { route -> tabNavigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SEARCH_DESTINATION,
            arguments = listOf(
                navArgument("query") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            tv.darshini.app.ui.screens.search.SearchScreen(
                initialQuery = backStackEntry.arguments?.getString("query").orEmpty(),
                onChannelClick = { channel ->
                    navController.navigateToPlayer(
                        Routes.livePlayer(
                            channel = channel,
                            categoryId = channel.categoryId ?: ChannelRepository.ALL_CHANNELS_ID,
                            providerId = channel.providerId,
                            isVirtual = false,
                            returnRoute = Routes.search(backStackEntry.arguments?.getString("query").orEmpty())
                        )
                    )
                },
                onMovieClick = { movie ->
                     navController.navigateToMovieDetail(
                         movie,
                         Routes.search(backStackEntry.arguments?.getString("query").orEmpty())
                     )
                },
                onSeriesClick = { series ->
                     navController.navigateToSeriesDetail(
                         series,
                         Routes.search(backStackEntry.arguments?.getString("query").orEmpty())
                     )
                },
                onNavigate = { route -> tabNavigate(route) },
                currentRoute = Routes.SEARCH
            )
        }

        composable(route = Routes.PLAYER) { backStackEntry ->
            val playerRequest = backStackEntry.savedStateHandle.get<PlayerNavigationRequest>(PLAYER_REQUEST_KEY)
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<PlayerNavigationRequest>(PLAYER_REQUEST_KEY)?.also {
                    backStackEntry.savedStateHandle[PLAYER_REQUEST_KEY] = it
                }
            val safePlayerRequest = safePlayerNavigationRequest(playerRequest)
            if (safePlayerRequest == null) {
                LaunchedEffect(playerRequest) {
                    Log.w(TAG, "Missing or invalid player request; returning to previous destination")
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.PLAYER) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            } else {
                PlayerScreen(
                    streamUrl = safePlayerRequest.streamUrl,
                    title = safePlayerRequest.title,
                    epgChannelId = safePlayerRequest.channelId,
                    internalChannelId = safePlayerRequest.internalId,
                    categoryId = safePlayerRequest.categoryId,
                    providerId = safePlayerRequest.providerId,
                    isVirtual = safePlayerRequest.isVirtual,
                    combinedProfileId = safePlayerRequest.combinedProfileId,
                    combinedSourceFilterProviderId = safePlayerRequest.combinedSourceFilterProviderId,
                    contentType = safePlayerRequest.contentType,
                    artworkUrl = safePlayerRequest.artworkUrl,
                    archiveStartMs = safePlayerRequest.archiveStartMs,
                    archiveEndMs = safePlayerRequest.archiveEndMs,
                    archiveTitle = safePlayerRequest.archiveTitle,
                    returnRoute = safePlayerRequest.returnRoute,
                    seriesId = safePlayerRequest.seriesId,
                    seasonNumber = safePlayerRequest.seasonNumber,
                    episodeNumber = safePlayerRequest.episodeNumber,
                    episodeId = safePlayerRequest.episodeId,
                    onBack = {
                        val route = safePlayerRequest.returnRoute
                        if (navController.popBackStack()) {
                            // Popped back successfully to the previous screen (e.g. MovieDetailScreen)
                            Unit
                        } else if (!route.isNullOrBlank()) {
                            // Nothing left to pop — navigate to the return route or home as a last resort
                            navController.navigate(route) {
                                popUpTo(Routes.PLAYER) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.PLAYER) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigate = { route ->
                        navController.navigateIfResumed(route) {
                            launchSingleTop = true
                            if (route == Routes.MULTI_VIEW) {
                                popUpTo(Routes.PLAYER) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.MOVIE_DETAIL,
            arguments = listOf(
                navArgument("movieId") { type = NavType.LongType },
                navArgument("returnRoute") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val moviePresentationHint = backStackEntry.savedStateHandle.get<MovieDetailPresentationHint>(MOVIE_DETAIL_PRESENTATION_HINT_KEY)
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<MovieDetailPresentationHint>(MOVIE_DETAIL_PRESENTATION_HINT_KEY)?.also {
                    backStackEntry.savedStateHandle[MOVIE_DETAIL_PRESENTATION_HINT_KEY] = it
                }
            val returnRoute = backStackEntry.arguments?.getString("returnRoute").orEmpty().takeIf { it.isNotBlank() }
            val movieId = backStackEntry.arguments?.getLong("movieId") ?: -1L
            val moviePlayMode = mainActivity.preferencesRepository.playerExternalPlaybackMode
                .collectAsStateWithLifecycle(ExternalPlaybackMode.INTERNAL_PLAYER).value
            tv.darshini.app.ui.screens.movies.MovieDetailScreen(
                onPlay = { movie ->
                    Log.d("AppNavigation", "MovieDetail onPlay: mode = $moviePlayMode, movie = ${movie.name}")
                    when (moviePlayMode) {
                        ExternalPlaybackMode.INTERNAL_PLAYER -> {
                            navController.navigateToPlayer(
                                Routes.moviePlayer(movie).copy(
                                    returnRoute = Routes.movieDetail(
                                        movieId = movie.id.takeIf { it > 0L } ?: movieId,
                                        returnRoute = returnRoute
                                    )
                                )
                            )
                        }
                        ExternalPlaybackMode.EXTERNAL_PLAYER, ExternalPlaybackMode.ASK_EVERY_TIME -> {
                            Log.d("AppNavigation", "Launching EXTERNAL player for movie with streamUrl")
                            launchExternalPlayerForChannel(moviePlayMode, movieId = movie.id, streamUrl = movie.streamUrl)
                        }
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        if (!returnRoute.isNullOrBlank()) {
                            navController.navigate(returnRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.SERIES_DETAIL,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.LongType },
                navArgument("returnRoute") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val seriesPresentationHint = backStackEntry.savedStateHandle.get<SeriesDetailPresentationHint>(SERIES_DETAIL_PRESENTATION_HINT_KEY)
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<SeriesDetailPresentationHint>(SERIES_DETAIL_PRESENTATION_HINT_KEY)?.also {
                    backStackEntry.savedStateHandle[SERIES_DETAIL_PRESENTATION_HINT_KEY] = it
                }
            val returnRoute = backStackEntry.arguments?.getString("returnRoute").orEmpty().takeIf { it.isNotBlank() }
            val seriesId = backStackEntry.arguments?.getLong("seriesId") ?: -1L
            tv.darshini.app.ui.screens.series.SeriesDetailScreen(
                onEpisodeClick = { episode ->
                    // Episodes use xtream:// URLs that need SeriesRepository resolution
                    // (not yet available), so always use internal player for now
                    navController.navigateToPlayer(
                        Routes.episodePlayer(episode).copy(
                            returnRoute = Routes.seriesDetail(
                                seriesId = episode.seriesId.takeIf { it > 0L } ?: seriesId,
                                returnRoute = returnRoute
                            )
                        )
                    )
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        if (!returnRoute.isNullOrBlank()) {
                            navController.navigate(returnRoute) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.MULTI_VIEW) {
            MultiViewScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
