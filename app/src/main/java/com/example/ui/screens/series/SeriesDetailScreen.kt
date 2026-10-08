package com.example.ui.screens.series

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.local.entity.UserEntity
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
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    currentUser: UserEntity?,
    seriesRepository: SeriesRepository,
    userDataRepository: UserDataRepository,
    onPlayEpisode: (EpisodeEntity, SeriesEntity) -> Unit,
    onPlayTrailer: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val series by seriesRepository.getSeriesByIdFlow(seriesId).collectAsState(initial = null)
    val seasons by seriesRepository.getSeasonsForSeriesFlow(seriesId).collectAsState(initial = emptyList())
    var selectedSeasonId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(seasons) {
        if (selectedSeasonId == null && seasons.isNotEmpty()) {
            selectedSeasonId = seasons.first().id
        }
    }

    val episodes by if (selectedSeasonId != null) {
        seriesRepository.getEpisodesForSeasonFlow(selectedSeasonId!!).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isInWatchlist by if (currentUser != null) {
        userDataRepository.isInWatchlistFlow(currentUser.id, seriesId).collectAsState(initial = false)
    } else {
        remember { mutableStateOf(false) }
    }

    if (series == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            EmptyStateView(
                icon = Icons.Default.Tv,
                title = "Series Not Found",
                message = "The web series you are looking for may have been removed.",
                actionButtonText = "Go Back",
                onActionClick = onBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    val currentSeries = series!!

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // Backdrop Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    val bannerModel = resolveImageModel(currentSeries.bannerUri.ifEmpty { currentSeries.posterUri })
                    if (bannerModel != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(bannerModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = currentSeries.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.6f),
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .testTag("series_detail_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                }
            }

            // Series Info Area
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = currentSeries.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${currentSeries.genre} • ${currentSeries.language} • ${currentSeries.releaseYear}", color = TextSecondary, fontSize = 13.sp)
                        Text(text = " • ", color = TextMuted)
                        Text(text = "${seasons.size} Season${if (seasons.size == 1) "" else "s"}", color = BrandGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (currentUser != null) {
                                    scope.launch {
                                        val added = userDataRepository.toggleWatchlist(currentUser.id, currentSeries.id, "SERIES")
                                        snackbarHostState.showSnackbar(if (added) "Added to My List" else "Removed from My List")
                                    }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Sign in to save to My List") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("series_watchlist_button")
                        ) {
                            Icon(
                                imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = if (isInWatchlist) BrandRed else TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isInWatchlist) "In My List" else "+ My List", color = TextPrimary)
                        }

                        if (currentSeries.trailerUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onPlayTrailer(currentSeries.trailerUri, "${currentSeries.title} Trailer") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trailer", color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = currentSeries.description, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp))
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Seasons Selector Chips
            if (seasons.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        Text(
                            text = "Seasons",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(seasons) { season ->
                                val isSelected = selectedSeasonId == season.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSeasonId = season.id },
                                    label = { Text(season.title) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandRed,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = DarkBorder,
                                        selectedBorderColor = BrandRed
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Episodes List
            if (episodes.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Tv,
                        title = "No Episodes Yet",
                        message = "Episodes for this season will appear when uploaded by the admin.",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            } else {
                items(episodes, key = { it.id }) { episode ->
                    EpisodeListItem(
                        episode = episode,
                        currentUser = currentUser,
                        userDataRepository = userDataRepository,
                        onPlay = { onPlayEpisode(episode, currentSeries) },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun EpisodeListItem(
    episode: EpisodeEntity,
    currentUser: UserEntity?,
    userDataRepository: UserDataRepository,
    onPlay: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, episode.id) {
        if (currentUser != null) {
            userDataRepository.getDownloadsFlow(currentUser.id).collect { list ->
                val dl = list.find { it.contentId == episode.id }
                if (dl != null) {
                    if (dl.downloadStatus == "COMPLETED") {
                        isDownloaded = true
                        isDownloading = false
                    } else if (dl.downloadStatus == "DOWNLOADING") {
                        isDownloading = true
                        downloadProgress = dl.progressPercent
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode Thumbnail with Play icon
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(65.dp)
                    .background(DarkSurface, RoundedCornerShape(6.dp))
            ) {
                val thumbModel = resolveImageModel(episode.thumbnailUri)
                if (thumbModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Episode info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "E${episode.episodeNumber}. ${episode.title}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${episode.durationMinutes} min",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                if (episode.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = episode.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Download icon button
            if (episode.isDownloadable) {
                IconButton(
                    onClick = {
                        if (currentUser != null) {
                            if (!isDownloaded && !isDownloading) {
                                isDownloading = true
                                scope.launch {
                                    snackbarHostState.showSnackbar("Downloading ${episode.title}...")
                                    val result = userDataRepository.startDownload(
                                        userId = currentUser.id,
                                        contentId = episode.id,
                                        contentType = "EPISODE",
                                        title = episode.title,
                                        subtitleInfo = "Episode ${episode.episodeNumber}",
                                        thumbnailUri = episode.thumbnailUri,
                                        videoUri = episode.videoUri
                                    )
                                    isDownloading = false
                                    if (result.isSuccess) {
                                        isDownloaded = true
                                        snackbarHostState.showSnackbar("Episode downloaded!")
                                    } else {
                                        snackbarHostState.showSnackbar("Download failed")
                                    }
                                }
                            }
                        }
                    }
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandRed, strokeWidth = 2.dp)
                    } else if (isDownloaded) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = StatusSuccess)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = TextMuted)
                    }
                }
            }
        }
    }
}
