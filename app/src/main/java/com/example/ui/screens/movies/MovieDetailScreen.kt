package com.example.ui.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.resolveImageModel
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MovieDetailScreen(
    movieId: String,
    currentUser: UserEntity?,
    movieRepository: MovieRepository,
    userDataRepository: UserDataRepository,
    onPlayMovie: (MovieEntity) -> Unit,
    onPlayTrailer: (MovieEntity) -> Unit,
    onBack: () -> Unit
) {
    val movie by movieRepository.getMovieByIdFlow(movieId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isInWatchlist by if (currentUser != null) {
        userDataRepository.isInWatchlistFlow(currentUser.id, movieId).collectAsState(initial = false)
    } else {
        remember { mutableStateOf(false) }
    }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloaded by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, movieId) {
        if (currentUser != null) {
            val userDownloads = userDataRepository.getDownloadsFlow(currentUser.id)
            userDownloads.collect { list ->
                val dl = list.find { it.contentId == movieId }
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

    if (movie == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
            EmptyStateView(
                icon = Icons.Default.Movie,
                title = "Movie Not Found",
                message = "The movie you are looking for may have been removed or unpublished.",
                actionButtonText = "Go Back",
                onActionClick = onBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    val currentMovie = movie!!

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            // Backdrop Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                val bannerModel = resolveImageModel(currentMovie.bannerUri.ifEmpty { currentMovie.posterUri })
                if (bannerModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(bannerModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = currentMovie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.5f),
                                    DarkBackground
                                )
                            )
                        )
                )

                // Top Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("movie_detail_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
            }

            // Movie Details Info Card Area
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Poster Thumbnail
                    Card(
                        modifier = Modifier
                            .width(110.dp)
                            .height(160.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        val posterModel = resolveImageModel(currentMovie.posterUri)
                        if (posterModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(posterModel)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = currentMovie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Metadata info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentMovie.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (currentMovie.rating > 0f) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", currentMovie.rating),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(text = " • ", color = TextMuted)
                            }
                            Text(text = "${currentMovie.releaseYear}", color = TextSecondary, fontSize = 13.sp)
                            Text(text = " • ", color = TextMuted)
                            Text(text = "${currentMovie.durationMinutes}m", color = TextSecondary, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${currentMovie.genre} | ${currentMovie.language}",
                            color = BrandGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Rating: ${currentMovie.ageRating}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Buttons:
                // ▶ Watch Now
                Button(
                    onClick = { onPlayMovie(currentMovie) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("movie_watch_now_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Watch Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Buttons Row: [+ My List] [⬇ Download] [▷ Trailer]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Watchlist Toggle
                    OutlinedButton(
                        onClick = {
                            if (currentUser != null) {
                                scope.launch {
                                    val added = userDataRepository.toggleWatchlist(currentUser.id, currentMovie.id, "MOVIE")
                                    snackbarHostState.showSnackbar(if (added) "Added to My List" else "Removed from My List")
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Sign in to save to My List") }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("movie_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (isInWatchlist) BrandRed else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isInWatchlist) "In List" else "+ My List", color = TextPrimary, fontSize = 12.sp)
                    }

                    // Download Button
                    if (currentMovie.isDownloadable) {
                        OutlinedButton(
                            onClick = {
                                if (currentUser != null) {
                                    if (isDownloaded) {
                                        scope.launch { snackbarHostState.showSnackbar("Movie already downloaded! Check Downloads tab.") }
                                    } else if (!isDownloading) {
                                        isDownloading = true
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Starting download...")
                                            val result = userDataRepository.startDownload(
                                                userId = currentUser.id,
                                                contentId = currentMovie.id,
                                                contentType = "MOVIE",
                                                title = currentMovie.title,
                                                subtitleInfo = "${currentMovie.durationMinutes}m",
                                                thumbnailUri = currentMovie.posterUri,
                                                videoUri = currentMovie.videoUri
                                            )
                                            isDownloading = false
                                            if (result.isSuccess) {
                                                isDownloaded = true
                                                snackbarHostState.showSnackbar("Download completed successfully!")
                                            } else {
                                                snackbarHostState.showSnackbar("Download failed: ${result.exceptionOrNull()?.message}")
                                            }
                                        }
                                    }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Sign in to download content") }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("movie_download_button")
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(color = BrandRed, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$downloadProgress%", color = BrandRed, fontSize = 12.sp)
                            } else if (isDownloaded) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Saved", color = StatusSuccess, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    // Trailer Button
                    if (currentMovie.trailerUri.isNotBlank()) {
                        OutlinedButton(
                            onClick = { onPlayTrailer(currentMovie) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("movie_trailer_button")
                        ) {
                            Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = BrandGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trailer", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text(
                    text = "Storyline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentMovie.description.ifEmpty { "No description provided." },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cast & Director
                if (currentMovie.cast.isNotBlank()) {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = currentMovie.cast, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (currentMovie.director.isNotBlank()) {
                    Text(
                        text = "Director",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = currentMovie.director, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
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
