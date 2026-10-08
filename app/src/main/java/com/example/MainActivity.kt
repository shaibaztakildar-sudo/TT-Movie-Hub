package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.di.AppContainer
import com.example.ui.components.TTMovieHubBottomBar
import com.example.ui.components.TTMovieHubTopBar
import com.example.ui.navigation.Screen
import com.example.ui.player.VideoPlayerScreen
import com.example.ui.screens.admin.AdminAddEditMovieScreen
import com.example.ui.screens.admin.AdminAddEditSeriesScreen
import com.example.ui.screens.admin.AdminCategoriesScreen
import com.example.ui.screens.admin.AdminLoginScreen
import com.example.ui.screens.admin.AdminManageEpisodesScreen
import com.example.ui.screens.admin.AdminPanelScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.movies.MovieDetailScreen
import com.example.ui.screens.movies.MoviesScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.series.SeriesDetailScreen
import com.example.ui.screens.series.SeriesScreen
import com.example.ui.screens.watchlist.WatchlistScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = AppContainer.getInstance(applicationContext)

        setContent {
            MyApplicationTheme {
                MainApp(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun MainApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val currentUser by appContainer.authRepository.currentUser.collectAsState()
    val currentAdmin by appContainer.authRepository.currentAdmin.collectAsState()

    LaunchedEffect(Unit) {
        appContainer.authRepository.initializeSession()
    }

    val isPlayerRoute = currentRoute.startsWith("player")
    val isAuthRoute = currentRoute in listOf(Screen.Login.route, Screen.SignUp.route, Screen.ForgotPassword.route)
    val isAdminRoute = currentRoute.startsWith("admin_")

    val showTopBar = !isPlayerRoute && !isAuthRoute && !isAdminRoute && currentRoute != Screen.Profile.route && !currentRoute.startsWith("movie_detail") && !currentRoute.startsWith("series_detail")
    val showBottomBar = !isPlayerRoute && !isAuthRoute && !isAdminRoute

    Scaffold(
        topBar = {
            if (showTopBar) {
                TTMovieHubTopBar(
                    currentUser = currentUser,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onAdminClick = {
                        if (currentUser?.role == "ADMIN" || currentAdmin != null) {
                            navController.navigate(Screen.AdminDashboard.route)
                        } else {
                            navController.navigate(Screen.AdminLogin.route)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                TTMovieHubBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = DarkBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(
                    top = if (isPlayerRoute) 0.dp else innerPadding.calculateTopPadding(),
                    bottom = if (isPlayerRoute) 0.dp else innerPadding.calculateBottomPadding()
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                // Auth Routes
                composable(Screen.Login.route) {
                    LoginScreen(
                        authRepository = appContainer.authRepository,
                        onLoginSuccess = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                        onNavigateToAdminLogin = { navController.navigate(Screen.AdminLogin.route) }
                    )
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        authRepository = appContainer.authRepository,
                        onSignUpSuccess = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        authRepository = appContainer.authRepository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Dedicated Admin Login Route
                composable(Screen.AdminLogin.route) {
                    AdminLoginScreen(
                        authRepository = appContainer.authRepository,
                        onAdminLoginSuccess = {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.AdminLogin.route) { inclusive = true }
                            }
                        },
                        onBackToUserApp = { navController.popBackStack() }
                    )
                }

                // Home
                composable(Screen.Home.route) {
                    HomeScreen(
                        currentUser = currentUser,
                        movieRepository = appContainer.movieRepository,
                        seriesRepository = appContainer.seriesRepository,
                        userDataRepository = appContainer.userDataRepository,
                        onMovieClick = { movieId ->
                            navController.navigate(Screen.MovieDetail.createRoute(movieId))
                        },
                        onSeriesClick = { seriesId ->
                            navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                        },
                        onPlayMovie = { movie ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = movie.id,
                                    contentType = "MOVIE",
                                    videoUri = movie.videoUri,
                                    title = movie.title,
                                    subtitle = "${movie.durationMinutes}m • ${movie.genre}",
                                    thumb = movie.posterUri
                                )
                            )
                        },
                        onContinueWatchingPlay = { contentId, contentType, videoUri, title, subtitle, thumb, startPos, seriesId ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = contentId,
                                    contentType = contentType,
                                    videoUri = videoUri,
                                    title = title,
                                    subtitle = subtitle,
                                    thumb = thumb,
                                    startPos = startPos,
                                    seriesId = seriesId
                                )
                            )
                        },
                        onNavigateToAdmin = {
                            if (currentUser?.role == "ADMIN" || currentAdmin != null) {
                                navController.navigate(Screen.AdminDashboard.route)
                            } else {
                                navController.navigate(Screen.AdminLogin.route)
                            }
                        }
                    )
                }

                // Movies List
                composable(Screen.Movies.route) {
                    MoviesScreen(
                        movieRepository = appContainer.movieRepository,
                        onMovieClick = { movieId ->
                            navController.navigate(Screen.MovieDetail.createRoute(movieId))
                        }
                    )
                }

                // Series List
                composable(Screen.Series.route) {
                    SeriesScreen(
                        seriesRepository = appContainer.seriesRepository,
                        onSeriesClick = { seriesId ->
                            navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                        }
                    )
                }

                // Search
                composable(Screen.Search.route) {
                    SearchScreen(
                        movieRepository = appContainer.movieRepository,
                        seriesRepository = appContainer.seriesRepository,
                        onMovieClick = { movieId ->
                            navController.navigate(Screen.MovieDetail.createRoute(movieId))
                        },
                        onSeriesClick = { seriesId ->
                            navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                        }
                    )
                }

                // My List / Watchlist
                composable(Screen.MyList.route) {
                    WatchlistScreen(
                        currentUser = currentUser,
                        userDataRepository = appContainer.userDataRepository,
                        movieRepository = appContainer.movieRepository,
                        seriesRepository = appContainer.seriesRepository,
                        onMovieClick = { movieId ->
                            navController.navigate(Screen.MovieDetail.createRoute(movieId))
                        },
                        onSeriesClick = { seriesId ->
                            navController.navigate(Screen.SeriesDetail.createRoute(seriesId))
                        }
                    )
                }

                // Downloads
                composable(Screen.Downloads.route) {
                    DownloadsScreen(
                        currentUser = currentUser,
                        userDataRepository = appContainer.userDataRepository,
                        onPlayOffline = { download ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = download.contentId,
                                    contentType = download.contentType,
                                    videoUri = download.localFilePath,
                                    title = download.title,
                                    subtitle = download.subtitleInfo,
                                    thumb = download.thumbnailUri
                                )
                            )
                        }
                    )
                }

                // Profile
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        currentUser = currentUser,
                        authRepository = appContainer.authRepository,
                        onNavigateToMyList = { navController.navigate(Screen.MyList.route) },
                        onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) },
                        onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) },
                        onNavigateToAdminLogin = { navController.navigate(Screen.AdminLogin.route) },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onLogout = { navController.navigate(Screen.Home.route) }
                    )
                }

                // Movie Detail
                composable(
                    route = Screen.MovieDetail.route,
                    arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                ) { backStack ->
                    val movieId = backStack.arguments?.getString("movieId") ?: ""
                    MovieDetailScreen(
                        movieId = movieId,
                        currentUser = currentUser,
                        movieRepository = appContainer.movieRepository,
                        userDataRepository = appContainer.userDataRepository,
                        onPlayMovie = { movie ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = movie.id,
                                    contentType = "MOVIE",
                                    videoUri = movie.videoUri,
                                    title = movie.title,
                                    subtitle = "${movie.durationMinutes}m • ${movie.genre}",
                                    thumb = movie.posterUri
                                )
                            )
                        },
                        onPlayTrailer = { movie ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = "${movie.id}_trailer",
                                    contentType = "MOVIE",
                                    videoUri = movie.trailerUri,
                                    title = "${movie.title} - Official Trailer",
                                    subtitle = "Trailer",
                                    thumb = movie.bannerUri.ifEmpty { movie.posterUri }
                                )
                            )
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Series Detail
                composable(
                    route = Screen.SeriesDetail.route,
                    arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
                ) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId") ?: ""
                    SeriesDetailScreen(
                        seriesId = seriesId,
                        currentUser = currentUser,
                        seriesRepository = appContainer.seriesRepository,
                        userDataRepository = appContainer.userDataRepository,
                        onPlayEpisode = { ep, s ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = ep.id,
                                    contentType = "EPISODE",
                                    videoUri = ep.videoUri,
                                    title = "${s.title}: ${ep.title}",
                                    subtitle = "Season ${ep.seasonNumber} Episode ${ep.episodeNumber}",
                                    thumb = ep.thumbnailUri,
                                    seriesId = s.id
                                )
                            )
                        },
                        onPlayTrailer = { trailerUri, title ->
                            navController.navigate(
                                Screen.Player.createRoute(
                                    contentId = "${seriesId}_trailer",
                                    contentType = "SERIES",
                                    videoUri = trailerUri,
                                    title = title,
                                    subtitle = "Official Trailer"
                                )
                            )
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                // Full Video Player
                composable(
                    route = Screen.Player.route,
                    arguments = listOf(
                        navArgument("contentId") { type = NavType.StringType; defaultValue = "" },
                        navArgument("contentType") { type = NavType.StringType; defaultValue = "MOVIE" },
                        navArgument("videoUri") { type = NavType.StringType; defaultValue = "" },
                        navArgument("title") { type = NavType.StringType; defaultValue = "" },
                        navArgument("subtitle") { type = NavType.StringType; defaultValue = "" },
                        navArgument("thumb") { type = NavType.StringType; defaultValue = "" },
                        navArgument("startPos") { type = NavType.LongType; defaultValue = 0L },
                        navArgument("seriesId") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backStack ->
                    val rawVideoUri = backStack.arguments?.getString("videoUri") ?: ""
                    val rawTitle = backStack.arguments?.getString("title") ?: ""
                    val rawSubtitle = backStack.arguments?.getString("subtitle") ?: ""
                    val rawThumb = backStack.arguments?.getString("thumb") ?: ""

                    val videoUri = java.net.URLDecoder.decode(rawVideoUri, "UTF-8")
                    val title = java.net.URLDecoder.decode(rawTitle, "UTF-8")
                    val subtitle = java.net.URLDecoder.decode(rawSubtitle, "UTF-8")
                    val thumb = java.net.URLDecoder.decode(rawThumb, "UTF-8")

                    val contentId = backStack.arguments?.getString("contentId") ?: ""
                    val contentType = backStack.arguments?.getString("contentType") ?: "MOVIE"
                    val startPos = backStack.arguments?.getLong("startPos") ?: 0L
                    val seriesId = backStack.arguments?.getString("seriesId") ?: ""

                    VideoPlayerScreen(
                        contentId = contentId,
                        contentType = contentType,
                        videoUri = videoUri,
                        title = title,
                        subtitleInfo = subtitle,
                        thumbnailUri = thumb,
                        startPositionMs = startPos,
                        seriesId = seriesId.ifEmpty { null },
                        currentUserId = currentUser?.id,
                        userDataRepository = appContainer.userDataRepository,
                        onBack = { navController.popBackStack() },
                        onNextEpisode = null
                    )
                }

                // Dedicated Protected Admin Panel Shell (Includes Dashboard, Movies, Series, Library, Users, Categories, Settings, Profile)
                composable(Screen.AdminDashboard.route) {
                    AdminPanelScreen(
                        authRepository = appContainer.authRepository,
                        movieRepository = appContainer.movieRepository,
                        seriesRepository = appContainer.seriesRepository,
                        userDataRepository = appContainer.userDataRepository,
                        onNavigateToAddMovie = { movieId ->
                            navController.navigate(Screen.AdminAddEditMovie.createRoute(movieId))
                        },
                        onNavigateToAddSeries = { seriesId ->
                            navController.navigate(Screen.AdminAddEditSeries.createRoute(seriesId))
                        },
                        onNavigateToManageEpisodes = { sId ->
                            navController.navigate(Screen.AdminManageEpisodes.createRoute(sId))
                        },
                        onLogoutAdmin = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        },
                        onBackToUserApp = { navController.navigate(Screen.Home.route) }
                    )
                }

                // Admin Add/Edit Movie
                composable(
                    route = Screen.AdminAddEditMovie.route,
                    arguments = listOf(navArgument("movieId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStack ->
                    val movieId = backStack.arguments?.getString("movieId")
                    AdminAddEditMovieScreen(
                        movieId = movieId,
                        movieRepository = appContainer.movieRepository,
                        userDataRepository = appContainer.userDataRepository,
                        mediaStorageManager = appContainer.mediaStorageManager,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin Add/Edit Series
                composable(
                    route = Screen.AdminAddEditSeries.route,
                    arguments = listOf(navArgument("seriesId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId")
                    AdminAddEditSeriesScreen(
                        seriesId = seriesId,
                        seriesRepository = appContainer.seriesRepository,
                        mediaStorageManager = appContainer.mediaStorageManager,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin Manage Episodes
                composable(
                    route = Screen.AdminManageEpisodes.route,
                    arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
                ) { backStack ->
                    val seriesId = backStack.arguments?.getString("seriesId") ?: ""
                    AdminManageEpisodesScreen(
                        seriesId = seriesId,
                        seriesRepository = appContainer.seriesRepository,
                        mediaStorageManager = appContainer.mediaStorageManager,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Admin Categories
                composable(Screen.AdminCategories.route) {
                    AdminCategoriesScreen(
                        userDataRepository = appContainer.userDataRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
