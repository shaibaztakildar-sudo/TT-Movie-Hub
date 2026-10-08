package com.example.ui.screens.admin

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.MediaStorageManager
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.resolveImageModel
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminNavSection(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    MOVIES("Movies", Icons.Default.Movie),
    SERIES("Web Series", Icons.Default.Tv),
    LIBRARY("Content Library", Icons.Default.Folder),
    USERS("Users", Icons.Default.People),
    CATEGORIES("Categories", Icons.Default.Category),
    SETTINGS("Settings", Icons.Default.Settings),
    PROFILE("Admin Profile", Icons.Default.Person),
    LOGOUT("Logout", Icons.Default.ExitToApp)
}

@Composable
fun AdminPanelScreen(
    authRepository: AuthRepository,
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    userDataRepository: UserDataRepository,
    onNavigateToAddMovie: (String?) -> Unit,
    onNavigateToAddSeries: (String?) -> Unit,
    onNavigateToManageEpisodes: (String) -> Unit,
    onLogoutAdmin: () -> Unit,
    onBackToUserApp: () -> Unit
) {
    val currentAdmin by authRepository.currentAdmin.collectAsState()
    val currentUser by authRepository.currentUser.collectAsState()

    // Enforce strict security: must have role == ADMIN
    val activeAdmin = currentAdmin ?: if (currentUser?.role == "ADMIN") currentUser else null
    if (activeAdmin == null || activeAdmin.role != "ADMIN" || !activeAdmin.isActive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .statusBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = StatusError, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Access Blocked",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Normal users are not authorized to view the Admin Panel. Please sign in with administrator credentials.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBackToUserApp,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Return to Application")
                }
            }
        }
        return
    }

    var selectedSection by remember { mutableStateOf(AdminNavSection.DASHBOARD) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Admin Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(BrandGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = BrandGold, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TT Movie Hub • Admin Panel",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
                Text(
                    text = "Admin: ${activeAdmin.name}",
                    color = BrandGold,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onBackToUserApp, modifier = Modifier.testTag("admin_exit_to_app_btn")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to User App", tint = TextPrimary)
            }
        }

        // Horizontal Navigation Bar across all sections
        ScrollableTabRow(
            selectedTabIndex = selectedSection.ordinal,
            containerColor = DarkSurfaceElevated,
            contentColor = BrandRed,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection.ordinal]),
                    color = BrandRed
                )
            }
        ) {
            AdminNavSection.values().forEach { section ->
                val isSelected = selectedSection == section
                Tab(
                    selected = isSelected,
                    onClick = {
                        if (section == AdminNavSection.LOGOUT) {
                            showLogoutDialog = true
                        } else {
                            selectedSection = section
                        }
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) BrandRed else TextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = section.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) BrandRed else TextSecondary
                            )
                        }
                    },
                    modifier = Modifier.testTag("admin_nav_${section.name.lowercase()}")
                )
            }
        }

        // Main Section Container
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedSection) {
                AdminNavSection.DASHBOARD -> {
                    AdminDashboardTab(
                        movieRepository = movieRepository,
                        seriesRepository = seriesRepository,
                        userDataRepository = userDataRepository,
                        authRepository = authRepository,
                        onNavigateToAddMovie = onNavigateToAddMovie,
                        onNavigateToAddSeries = onNavigateToAddSeries
                    )
                }
                AdminNavSection.MOVIES -> {
                    AdminMoviesTab(
                        movieRepository = movieRepository,
                        onNavigateToAddMovie = onNavigateToAddMovie
                    )
                }
                AdminNavSection.SERIES -> {
                    AdminSeriesTab(
                        seriesRepository = seriesRepository,
                        onNavigateToAddSeries = onNavigateToAddSeries,
                        onNavigateToManageEpisodes = onNavigateToManageEpisodes
                    )
                }
                AdminNavSection.LIBRARY -> {
                    AdminContentLibraryTab(
                        movieRepository = movieRepository,
                        seriesRepository = seriesRepository,
                        onNavigateToAddMovie = onNavigateToAddMovie,
                        onNavigateToAddSeries = onNavigateToAddSeries,
                        onNavigateToManageEpisodes = onNavigateToManageEpisodes
                    )
                }
                AdminNavSection.USERS -> {
                    AdminUsersTab(
                        authRepository = authRepository
                    )
                }
                AdminNavSection.CATEGORIES -> {
                    AdminCategoriesView(
                        userDataRepository = userDataRepository
                    )
                }
                AdminNavSection.SETTINGS -> {
                    AdminSettingsTab(
                        authRepository = authRepository
                    )
                }
                AdminNavSection.PROFILE -> {
                    AdminProfileTab(
                        admin = activeAdmin,
                        authRepository = authRepository,
                        onLogoutAdmin = onLogoutAdmin
                    )
                }
                AdminNavSection.LOGOUT -> {
                    // Handled via dialog
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout Administrator", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to end your administrator session and return to the main app?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            authRepository.logoutAdmin()
                            onLogoutAdmin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}

@Composable
fun AdminDashboardTab(
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    userDataRepository: UserDataRepository,
    authRepository: AuthRepository,
    onNavigateToAddMovie: (String?) -> Unit,
    onNavigateToAddSeries: (String?) -> Unit
) {
    var totalMovies by remember { mutableIntStateOf(0) }
    var publishedMovies by remember { mutableIntStateOf(0) }
    var unpublishedMovies by remember { mutableIntStateOf(0) }

    var totalSeries by remember { mutableIntStateOf(0) }
    var publishedSeries by remember { mutableIntStateOf(0) }
    var unpublishedSeries by remember { mutableIntStateOf(0) }

    var totalSeasons by remember { mutableIntStateOf(0) }
    var totalEpisodes by remember { mutableIntStateOf(0) }
    var totalUsers by remember { mutableIntStateOf(0) }

    var recentMovies by remember { mutableStateOf<List<MovieEntity>>(emptyList()) }
    var recentSeries by remember { mutableStateOf<List<SeriesEntity>>(emptyList()) }

    val allUsers by authRepository.allUsersFlow.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        totalMovies = movieRepository.getTotalMoviesCount()
        publishedMovies = movieRepository.getPublishedCount()
        unpublishedMovies = movieRepository.getUnpublishedCount()

        totalSeries = seriesRepository.getTotalSeriesCount()
        publishedSeries = seriesRepository.getPublishedSeriesCount()
        unpublishedSeries = seriesRepository.getUnpublishedSeriesCount()

        totalSeasons = seriesRepository.getTotalSeasonsCount()
        totalEpisodes = seriesRepository.getTotalEpisodesCount()

        recentMovies = movieRepository.getRecentMovies(5)
        recentSeries = seriesRepository.getRecentSeries(5)
    }

    LaunchedEffect(allUsers) {
        totalUsers = allUsers.size
    }

    val totalPublished = publishedMovies + publishedSeries
    val totalUnpublished = unpublishedMovies + unpublishedSeries

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Overview & Live Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        // Row 1: Movies & Series
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    title = "Total Movies",
                    value = "$totalMovies",
                    subtitle = "$publishedMovies Published • $unpublishedMovies Draft",
                    icon = Icons.Default.Movie,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Web Series",
                    value = "$totalSeries",
                    subtitle = "$publishedSeries Published • $unpublishedSeries Draft",
                    icon = Icons.Default.Tv,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 2: Seasons, Episodes, Users
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminMetricCard(
                    title = "Seasons",
                    value = "$totalSeasons",
                    subtitle = "Across all series",
                    icon = Icons.Default.Tv,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Episodes",
                    value = "$totalEpisodes",
                    subtitle = "Uploaded media",
                    icon = Icons.Default.Movie,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Users",
                    value = "$totalUsers",
                    subtitle = "Registered accounts",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Row 3: Published vs Unpublished
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StatusSuccess.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "$totalPublished", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 20.sp)
                            Text(text = "Published Content", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(BrandGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = BrandGold, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "$totalUnpublished", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 20.sp)
                            Text(text = "Unpublished Content", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Quick Creator Actions
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onNavigateToAddMovie(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("quick_add_movie_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Movie")
                }
                Button(
                    onClick = { onNavigateToAddSeries(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("quick_add_series_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add Series")
                }
            }
        }

        // Recently Added Content
        item {
            Text(
                text = "Recently Added Movies",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        if (recentMovies.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(
                        text = "No movies added yet. Movies: 0",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentMovies, key = { it.id }) { m ->
                AdminMiniItem(title = m.title, subtitle = "${m.genre} • ${m.releaseYear}", isPublished = m.isPublished)
            }
        }

        item {
            Text(
                text = "Recently Added Web Series",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        if (recentSeries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(
                        text = "No web series added yet. Series: 0",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(recentSeries, key = { it.id }) { s ->
                AdminMiniItem(title = s.title, subtitle = "${s.genre} • ${s.releaseYear}", isPublished = s.isPublished)
            }
        }

        // Recent User Activity
        item {
            Text(
                text = "Recent User Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val normalUsers = allUsers.filter { it.role != "ADMIN" }
        if (normalUsers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Text(
                        text = "No user activity yet. Registered users: 0",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            val recentUsers = normalUsers.sortedByDescending { it.createdAt }.take(5)
            items(recentUsers, key = { it.id }) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(BrandRed.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(u.name.take(1).uppercase(), color = BrandRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${u.name} registered account", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            val joinDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(u.createdAt))
                            Text(text = "${u.email} • $joinDate", color = TextMuted, fontSize = 10.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(if (u.isActive) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (u.isActive) "ACTIVE" else "DISABLED",
                                color = if (u.isActive) StatusSuccess else StatusError,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, color = TextPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp), maxLines = 1)
        }
    }
}

@Composable
fun AdminMiniItem(title: String, subtitle: String, isPublished: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        if (isPublished) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isPublished) "PUBLISHED" else "UNPUBLISHED",
                    color = if (isPublished) StatusSuccess else StatusError,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminMoviesTab(
    movieRepository: MovieRepository,
    onNavigateToAddMovie: (String?) -> Unit
) {
    val movies by movieRepository.allMoviesForAdmin.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Movies Management (${movies.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Button(
                    onClick = { onNavigateToAddMovie(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("admin_movies_tab_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Movie")
                }
            }
        }

        if (movies.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Movie,
                    title = "No Movies Found",
                    message = "Tap '+ Add Movie' to upload video, poster, trailer and details.",
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        } else {
            items(movies, key = { it.id }) { movie ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = movie.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (movie.isPublished) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (movie.isPublished) "PUBLISHED" else "UNPUBLISHED",
                                        color = if (movie.isPublished) StatusSuccess else StatusError,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${movie.genre} • ${movie.releaseYear} • ${movie.durationMinutes}m ${if (movie.isDownloadable) "• [Download ON]" else "• [Download OFF]"}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Actions: [Publish/Unpublish] [Edit] [Delete]
                        IconButton(onClick = { scope.launch { movieRepository.togglePublish(movie) } }) {
                            Icon(
                                imageVector = if (movie.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Publish",
                                tint = if (movie.isPublished) StatusSuccess else TextMuted
                            )
                        }
                        IconButton(onClick = { onNavigateToAddMovie(movie.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                        }
                        IconButton(onClick = { scope.launch { movieRepository.deleteMovie(movie.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSeriesTab(
    seriesRepository: SeriesRepository,
    onNavigateToAddSeries: (String?) -> Unit,
    onNavigateToManageEpisodes: (String) -> Unit
) {
    val seriesList by seriesRepository.allSeriesForAdmin.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Web Series Management (${seriesList.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Button(
                    onClick = { onNavigateToAddSeries(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("admin_series_tab_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Series")
                }
            }
        }

        if (seriesList.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Tv,
                    title = "No Web Series Found",
                    message = "Tap '+ Add Series' to create a new show and organize seasons and episodes.",
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        } else {
            items(seriesList, key = { it.id }) { series ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = series.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (series.isPublished) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (series.isPublished) "PUBLISHED" else "UNPUBLISHED",
                                            color = if (series.isPublished) StatusSuccess else StatusError,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "${series.genre} • ${series.releaseYear}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(onClick = { scope.launch { seriesRepository.togglePublish(series) } }) {
                                Icon(
                                    imageVector = if (series.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle",
                                    tint = if (series.isPublished) StatusSuccess else TextMuted
                                )
                            }
                            IconButton(onClick = { onNavigateToAddSeries(series.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                            }
                            IconButton(onClick = { scope.launch { seriesRepository.deleteSeries(series.id) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onNavigateToManageEpisodes(series.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Manage Seasons & Episodes", fontSize = 12.sp, color = BrandGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminContentLibraryTab(
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    onNavigateToAddMovie: (String?) -> Unit,
    onNavigateToAddSeries: (String?) -> Unit,
    onNavigateToManageEpisodes: (String) -> Unit
) {
    var libTab by remember { mutableIntStateOf(0) } // 0: Movies, 1: Web Series, 2: Episodes
    val movies by movieRepository.allMoviesForAdmin.collectAsState(initial = emptyList())
    val seriesList by seriesRepository.allSeriesForAdmin.collectAsState(initial = emptyList())
    val episodes by seriesRepository.allEpisodesFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = libTab,
            containerColor = DarkSurfaceElevated,
            contentColor = BrandRed
        ) {
            Tab(
                selected = libTab == 0,
                onClick = { libTab = 0 },
                text = { Text("Movies (${movies.size})", color = if (libTab == 0) BrandRed else TextSecondary) }
            )
            Tab(
                selected = libTab == 1,
                onClick = { libTab = 1 },
                text = { Text("Series (${seriesList.size})", color = if (libTab == 1) BrandRed else TextSecondary) }
            )
            Tab(
                selected = libTab == 2,
                onClick = { libTab = 2 },
                text = { Text("Episodes (${episodes.size})", color = if (libTab == 2) BrandRed else TextSecondary) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (libTab) {
            0 -> {
                if (movies.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Movie,
                        title = "No Content Added Yet",
                        message = "Your movie library is currently empty. Add movies using the Add Movie button.",
                        actionButtonText = "+ Add Movie",
                        onActionClick = { onNavigateToAddMovie(null) }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(movies, key = { it.id }) { m ->
                            LibraryItemCard(
                                title = m.title,
                                info = "${m.genre} • ${m.durationMinutes}m",
                                isPublished = m.isPublished,
                                onTogglePublish = { scope.launch { movieRepository.togglePublish(m) } },
                                onEdit = { onNavigateToAddMovie(m.id) },
                                onDelete = { scope.launch { movieRepository.deleteMovie(m.id) } }
                            )
                        }
                    }
                }
            }
            1 -> {
                if (seriesList.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Tv,
                        title = "No Content Added Yet",
                        message = "Your web series library is currently empty. Add your first series now.",
                        actionButtonText = "+ Add Series",
                        onActionClick = { onNavigateToAddSeries(null) }
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(seriesList, key = { it.id }) { s ->
                            LibraryItemCard(
                                title = s.title,
                                info = "${s.genre} • ${s.releaseYear}",
                                isPublished = s.isPublished,
                                onTogglePublish = { scope.launch { seriesRepository.togglePublish(s) } },
                                onEdit = { onNavigateToAddSeries(s.id) },
                                onDelete = { scope.launch { seriesRepository.deleteSeries(s.id) } }
                            )
                        }
                    }
                }
            }
            2 -> {
                if (episodes.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Default.Movie,
                        title = "No Content Added Yet",
                        message = "No episodes have been uploaded. Select a series to create seasons and episodes."
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(episodes, key = { it.id }) { ep ->
                            LibraryItemCard(
                                title = "Episode ${ep.episodeNumber}: ${ep.title}",
                                info = "${ep.durationMinutes} mins • Season ${ep.seasonNumber}",
                                isPublished = ep.isPublished,
                                onTogglePublish = { scope.launch { seriesRepository.toggleEpisodePublish(ep) } },
                                onEdit = { onNavigateToManageEpisodes(ep.seriesId) },
                                onDelete = { scope.launch { seriesRepository.deleteEpisode(ep.id) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryItemCard(
    title: String,
    info: String,
    isPublished: Boolean,
    onTogglePublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(text = info, color = TextMuted, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        if (isPublished) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isPublished) "PUBLISHED" else "UNPUBLISHED",
                    color = if (isPublished) StatusSuccess else StatusError,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onTogglePublish) {
                Icon(
                    imageVector = if (isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = if (isPublished) StatusSuccess else TextMuted
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = TextPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError)
            }
        }
    }
}

@Composable
fun AdminUsersTab(
    authRepository: AuthRepository
) {
    val users by authRepository.allUsersFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Registered Users (${users.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )
        Text(
            text = "All accounts registered in the database. Manage access permissions.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (users.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = "No Users Registered",
                message = "When users sign up, their profiles will appear here."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users, key = { it.id }) { u ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (u.role == "ADMIN") BrandGold else BrandRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = u.name.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = u.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(if (u.role == "ADMIN") BrandGold else DarkBorder, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = u.role, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (u.role == "ADMIN") DarkBackground else TextPrimary)
                                    }
                                }
                                Text(text = u.email, color = TextMuted, fontSize = 11.sp)
                                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(u.createdAt))
                                Text(text = "Joined: $dateStr • Status: ${if (u.isActive) "ACTIVE" else "DISABLED"}", color = if (u.isActive) StatusSuccess else StatusError, fontSize = 10.sp)
                            }

                            // Toggle Account Active / Disable status
                            if (u.role != "ADMIN") {
                                OutlinedButton(
                                    onClick = { scope.launch { authRepository.setUserStatus(u.id, !u.isActive) } },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (u.isActive) "Disable" else "Enable",
                                        fontSize = 11.sp,
                                        color = if (u.isActive) StatusError else StatusSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    authRepository: AuthRepository
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "System Settings & Configuration",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Storage & Engine Status", fontWeight = FontWeight.Bold, color = BrandGold)
                Text(text = "• Database Engine: SQLite / Room Persistent Database (v2)", color = TextSecondary, fontSize = 12.sp)
                Text(text = "• Media Storage: App Internal Media Directory (/files/media)", color = TextSecondary, fontSize = 12.sp)
                Text(text = "• Offline Storage: Dedicated Downloads Directory (/files/downloads)", color = TextSecondary, fontSize = 12.sp)
                Text(text = "• Video Core: AndroidX Media3 ExoPlayer (Hardware Accelerated)", color = TextSecondary, fontSize = 12.sp)
                Text(text = "• Security: Role-Based Access Control (RBAC)", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Content Distribution Rules", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "1. Only content marked 'Published' is visible in public user views.", color = TextMuted, fontSize = 12.sp)
                Text(text = "2. Offline downloads require 'Allow Download = ON' set by admin.", color = TextMuted, fontSize = 12.sp)
                Text(text = "3. Only authorized admin accounts can invoke content CRUD operations.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AdminProfileTab(
    admin: UserEntity,
    authRepository: AuthRepository,
    onLogoutAdmin: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Administrator Profile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).background(BrandGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = admin.name.take(1).uppercase(), color = DarkBackground, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(text = admin.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                        Text(text = admin.email, color = TextSecondary, fontSize = 12.sp)
                        if (admin.phone.isNotEmpty()) {
                            Text(text = "Mobile: ${admin.phone}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Divider(color = DarkBorder)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Privilege Level", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "SUPER_ADMIN", fontWeight = FontWeight.Bold, color = BrandGold, fontSize = 12.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Authentication Status", color = TextSecondary, fontSize = 12.sp)
                    Text(text = "ACTIVE SECURE SESSION", fontWeight = FontWeight.Bold, color = StatusSuccess, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                authRepository.logoutAdmin()
                onLogoutAdmin()
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_logout_btn")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = StatusError)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Administrator", color = StatusError, fontWeight = FontWeight.Bold)
        }
    }
}
