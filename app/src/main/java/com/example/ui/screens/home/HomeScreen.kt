package com.example.ui.screens.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MovieCard
import com.example.ui.components.SeriesCard
import com.example.ui.components.resolveImageModel
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    currentUser: UserEntity?,
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    userDataRepository: UserDataRepository,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit,
    onPlayMovie: (MovieEntity) -> Unit,
    onContinueWatchingPlay: (contentId: String, contentType: String, videoUri: String, title: String, subtitle: String, thumb: String, startPos: Long, seriesId: String) -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val movies by movieRepository.publishedMovies.collectAsState(initial = emptyList())
    val seriesList by seriesRepository.publishedSeries.collectAsState(initial = emptyList())
    val continueWatchingList by if (currentUser != null) {
        userDataRepository.getContinueWatchingFlow(currentUser.id).collectAsState(initial = emptyList())
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    }

    val featuredMovie = movies.firstOrNull { it.isFeatured } ?: movies.firstOrNull()
    val trendingMovies = movies.filter { it.isTrending }.ifEmpty { movies.take(8) }
    val scope = rememberCoroutineScope()

    val isEmpty = movies.isEmpty() && seriesList.isEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        if (isEmpty) {
            item {
                Spacer(modifier = Modifier.height(60.dp))
                EmptyStateView(
                    icon = Icons.Default.Movie,
                    title = "TT Movie Hub is Ready",
                    message = if (currentUser?.role == "ADMIN") {
                        "No content in the database yet. As an Admin, you can add movies, web series, trailers, and upload files from your gallery!"
                    } else {
                        "Movies and web series will appear here as soon as the admin publishes new content."
                    },
                    actionButtonText = if (currentUser?.role == "ADMIN") "Go to Admin Panel" else null,
                    onActionClick = if (currentUser?.role == "ADMIN") onNavigateToAdmin else null,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
        } else {
            // Featured Hero Banner
            if (featuredMovie != null) {
                item {
                    FeaturedHeroBanner(
                        movie = featuredMovie,
                        currentUser = currentUser,
                        userDataRepository = userDataRepository,
                        onWatchNow = { onPlayMovie(featuredMovie) },
                        onDetails = { onMovieClick(featuredMovie.id) }
                    )
                }
            }

            // Continue Watching Section
            if (continueWatchingList.isNotEmpty()) {
                item {
                    SectionHeader(title = "Continue Watching")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(continueWatchingList, key = { it.id }) { item ->
                            ContinueWatchingCard(
                                history = item,
                                onClick = {
                                    onContinueWatchingPlay(
                                        item.contentId,
                                        item.contentType,
                                        item.videoUri,
                                        item.title,
                                        item.subtitleInfo,
                                        item.thumbnailUri,
                                        item.positionMs,
                                        item.seriesId ?: ""
                                    )
                                },
                                onRemove = {
                                    if (currentUser != null) {
                                        scope.launch {
                                            userDataRepository.removeContinueWatching(currentUser.id, item.contentId)
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Trending Section
            if (trendingMovies.isNotEmpty()) {
                item {
                    SectionHeader(title = "Trending Now")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trendingMovies, key = { it.id }) { movie ->
                            MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Latest Movies Section
            if (movies.isNotEmpty()) {
                item {
                    SectionHeader(title = "Latest Movies")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(movies, key = { it.id }) { movie ->
                            MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Latest Web Series Section
            if (seriesList.isNotEmpty()) {
                item {
                    SectionHeader(title = "Popular Web Series")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(seriesList, key = { it.id }) { series ->
                            SeriesCard(series = series, onClick = { onSeriesClick(series.id) })
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturedHeroBanner(
    movie: MovieEntity,
    currentUser: UserEntity?,
    userDataRepository: UserDataRepository,
    onWatchNow: () -> Unit,
    onDetails: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isInWatchlist by if (currentUser != null) {
        userDataRepository.isInWatchlistFlow(currentUser.id, movie.id).collectAsState(initial = false)
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val bannerModel = resolveImageModel(movie.bannerUri.ifEmpty { movie.posterUri })
            if (bannerModel != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bannerModel)
                        .crossfade(true)
                        .build(),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Cinematic Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f),
                                DarkBackground
                            ),
                            startY = 60f
                        )
                    )
            )

            // Content info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                // Genre & Year chips
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = movie.genre.uppercase(),
                        color = BrandGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(text = " • ", color = TextMuted)
                    Text(text = "${movie.releaseYear}", color = TextSecondary, fontSize = 12.sp)
                    Text(text = " • ", color = TextMuted)
                    Text(text = movie.ageRating, color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (movie.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: [Watch Now] [+ My List] [Details]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onWatchNow,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_watch_now_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Watch Now", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (currentUser != null) {
                                scope.launch {
                                    userDataRepository.toggleWatchlist(currentUser.id, movie.id, "MOVIE")
                                }
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("hero_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "My List",
                            tint = if (isInWatchlist) BrandRed else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isInWatchlist) "Added" else "My List", color = TextPrimary)
                    }

                    OutlinedButton(
                        onClick = onDetails,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Details", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        if (onSeeAll != null) {
            Text(
                text = "See All",
                color = BrandRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAll)
            )
        }
    }
}
