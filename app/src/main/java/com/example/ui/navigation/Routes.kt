package com.example.ui.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object SignUp : Screen("sign_up")
    object ForgotPassword : Screen("forgot_password")

    // Main tabs
    object Home : Screen("home")
    object Movies : Screen("movies")
    object Series : Screen("series")
    object Search : Screen("search")
    object MyList : Screen("my_list")
    object Downloads : Screen("downloads")
    object Profile : Screen("profile")

    // Content details
    object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: String) = "movie_detail/$movieId"
    }
    object SeriesDetail : Screen("series_detail/{seriesId}") {
        fun createRoute(seriesId: String) = "series_detail/$seriesId"
    }

    // Full video player
    object Player : Screen("player?contentId={contentId}&contentType={contentType}&videoUri={videoUri}&title={title}&subtitle={subtitle}&thumb={thumb}&startPos={startPos}&seriesId={seriesId}") {
        fun createRoute(
            contentId: String,
            contentType: String,
            videoUri: String,
            title: String,
            subtitle: String = "",
            thumb: String = "",
            startPos: Long = 0L,
            seriesId: String = ""
        ): String {
            val encodedVideoUri = java.net.URLEncoder.encode(videoUri, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encodedSubtitle = java.net.URLEncoder.encode(subtitle, "UTF-8")
            val encodedThumb = java.net.URLEncoder.encode(thumb, "UTF-8")
            return "player?contentId=$contentId&contentType=$contentType&videoUri=$encodedVideoUri&title=$encodedTitle&subtitle=$encodedSubtitle&thumb=$encodedThumb&startPos=$startPos&seriesId=$seriesId"
        }
    }

    // Dedicated Admin Routes
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminMovies : Screen("admin_movies")
    object AdminSeries : Screen("admin_series")
    object AdminContentLibrary : Screen("admin_content_library")
    object AdminUsers : Screen("admin_users")
    object AdminCategories : Screen("admin_categories")
    object AdminSettings : Screen("admin_settings")
    object AdminProfile : Screen("admin_profile")

    object AdminAddEditMovie : Screen("admin_movie_form?movieId={movieId}") {
        fun createRoute(movieId: String? = null) = if (movieId != null) "admin_movie_form?movieId=$movieId" else "admin_movie_form"
    }
    object AdminAddEditSeries : Screen("admin_series_form?seriesId={seriesId}") {
        fun createRoute(seriesId: String? = null) = if (seriesId != null) "admin_series_form?seriesId=$seriesId" else "admin_series_form"
    }
    object AdminManageEpisodes : Screen("admin_manage_episodes/{seriesId}") {
        fun createRoute(seriesId: String) = "admin_manage_episodes/$seriesId"
    }
}
