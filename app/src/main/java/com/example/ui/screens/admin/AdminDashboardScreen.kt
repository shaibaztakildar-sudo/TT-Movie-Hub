package com.example.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.EmptyStateView
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

@Composable
fun AdminDashboardScreen(
    currentUser: UserEntity?,
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    userDataRepository: UserDataRepository,
    onNavigateToAddMovie: (String?) -> Unit,
    onNavigateToAddSeries: (String?) -> Unit,
    onNavigateToManageEpisodes: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onBack: () -> Unit
) {
    // SECURITY CHECK: Role-Based Authorization
    if (currentUser?.role != "ADMIN") {
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
                    text = "Access Denied",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You do not have Administrator privileges to access the Admin Panel. This incident has been restricted.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Return to App")
                }
            }
        }
        return
    }

    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Movies, 2: Series

    var totalMovies by remember { mutableIntStateOf(0) }
    var totalSeries by remember { mutableIntStateOf(0) }
    var totalEpisodes by remember { mutableIntStateOf(0) }
    var totalDownloads by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        totalMovies = movieRepository.getTotalMoviesCount()
        totalSeries = seriesRepository.getTotalSeriesCount()
        totalEpisodes = seriesRepository.getTotalEpisodesCount()
        totalDownloads = userDataRepository.getTotalDownloadsCount()
    }

    val allMovies by movieRepository.allMoviesForAdmin.collectAsState(initial = emptyList())
    val allSeries by seriesRepository.allSeriesForAdmin.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Admin Control Hub",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Logged in as ${currentUser.name} (Admin)",
                    style = MaterialTheme.typography.bodySmall.copy(color = BrandGold)
                )
            }
            IconButton(onClick = onNavigateToCategories, modifier = Modifier.testTag("manage_categories_button")) {
                Icon(Icons.Default.Category, contentDescription = "Categories", tint = TextPrimary)
            }
        }

        // Tabs
        val tabs = listOf("Overview", "Movies", "Web Series")
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = BrandRed,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrandRed
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) BrandRed else TextSecondary
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Overview & Stats
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Platform Statistics",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    // Stat Cards Grid
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Total Movies",
                                value = "$totalMovies",
                                icon = Icons.Default.Movie,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Web Series",
                                value = "$totalSeries",
                                icon = Icons.Default.Tv,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Episodes",
                                value = "$totalEpisodes",
                                icon = Icons.Default.Movie,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Downloads",
                                value = "$totalDownloads",
                                icon = Icons.Default.FileDownload,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Actions
                    item {
                        Text(
                            text = "Content Creation Actions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onNavigateToAddMovie(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_add_movie_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Movie")
                            }

                            Button(
                                onClick = { onNavigateToAddSeries(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("admin_add_series_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Series")
                            }
                        }
                    }
                }
            }

            1 -> {
                // Movies Management
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
                                text = "All Movies (${allMovies.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Button(
                                onClick = { onNavigateToAddMovie(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("movies_tab_add_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Movie")
                            }
                        }
                    }

                    if (allMovies.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Movie,
                                title = "No Movies Created Yet",
                                message = "Tap '+ Add Movie' above to publish your first movie from gallery or URL.",
                                modifier = Modifier.padding(top = 20.dp)
                            )
                        }
                    } else {
                        items(allMovies, key = { it.id }) { movie ->
                            AdminMovieItem(
                                movie = movie,
                                onEdit = { onNavigateToAddMovie(movie.id) },
                                onTogglePublish = { scope.launch { movieRepository.togglePublish(movie) } },
                                onDelete = { scope.launch { movieRepository.deleteMovie(movie.id) } }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Series Management
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
                                text = "All Web Series (${allSeries.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Button(
                                onClick = { onNavigateToAddSeries(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.testTag("series_tab_add_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Series")
                            }
                        }
                    }

                    if (allSeries.isEmpty()) {
                        item {
                            EmptyStateView(
                                icon = Icons.Default.Tv,
                                title = "No Web Series Added",
                                message = "Tap '+ Add Series' to create a web series, add seasons, and upload episodes.",
                                modifier = Modifier.padding(top = 20.dp)
                            )
                        }
                    } else {
                        items(allSeries, key = { it.id }) { series ->
                            AdminSeriesItem(
                                series = series,
                                onEdit = { onNavigateToAddSeries(series.id) },
                                onManageEpisodes = { onNavigateToManageEpisodes(series.id) },
                                onTogglePublish = { scope.launch { seriesRepository.togglePublish(series) } },
                                onDelete = { scope.launch { seriesRepository.deleteSeries(series.id) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black, color = TextPrimary))
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        }
    }
}

@Composable
fun AdminMovieItem(
    movie: MovieEntity,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = movie.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
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
                            text = if (movie.isPublished) "PUBLISHED" else "DRAFT",
                            color = if (movie.isPublished) StatusSuccess else StatusError,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${movie.genre} • ${movie.releaseYear} • ${movie.durationMinutes}m",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onTogglePublish) {
                Icon(
                    imageVector = if (movie.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Publish",
                    tint = if (movie.isPublished) StatusSuccess else TextMuted
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
            }
        }
    }
}

@Composable
fun AdminSeriesItem(
    series: SeriesEntity,
    onEdit: () -> Unit,
    onManageEpisodes: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = series.title,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
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
                                text = if (series.isPublished) "PUBLISHED" else "DRAFT",
                                color = if (series.isPublished) StatusSuccess else StatusError,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "${series.genre} • ${series.releaseYear}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onTogglePublish) {
                    Icon(
                        imageVector = if (series.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle",
                        tint = if (series.isPublished) StatusSuccess else TextMuted
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onManageEpisodes,
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(36.dp)
            ) {
                Text("Manage Seasons & Episodes", fontSize = 12.sp, color = BrandGold)
            }
        }
    }
}
