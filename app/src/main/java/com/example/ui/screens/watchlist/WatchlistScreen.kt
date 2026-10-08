package com.example.ui.screens.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MovieCard
import com.example.ui.components.SeriesCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WatchlistScreen(
    currentUser: UserEntity?,
    userDataRepository: UserDataRepository,
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit
) {
    val watchlistItems by if (currentUser != null) {
        userDataRepository.getWatchlistForUser(currentUser.id).collectAsState(initial = emptyList())
    } else {
        remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    }

    val allMovies by movieRepository.publishedMovies.collectAsState(initial = emptyList())
    val allSeries by seriesRepository.publishedSeries.collectAsState(initial = emptyList())

    val savedMovies = remember(watchlistItems, allMovies) {
        val movieIds = watchlistItems.filter { it.contentType == "MOVIE" }.map { it.contentId }
        allMovies.filter { movieIds.contains(it.id) }
    }

    val savedSeries = remember(watchlistItems, allSeries) {
        val seriesIds = watchlistItems.filter { it.contentType == "SERIES" }.map { it.contentId }
        allSeries.filter { seriesIds.contains(it.id) }
    }

    val isEmpty = savedMovies.isEmpty() && savedSeries.isEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "My List",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Movies and series you've saved to watch later",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        if (currentUser == null) {
            EmptyStateView(
                icon = Icons.Default.BookmarkBorder,
                title = "Sign In Required",
                message = "Please sign in to keep your personal watchlist synced.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else if (isEmpty) {
            EmptyStateView(
                icon = Icons.Default.BookmarkBorder,
                title = "Your Watchlist is Empty",
                message = "Explore movies and web series and tap '+ My List' to bookmark your favorites here.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(savedMovies, key = { "m_${it.id}" }) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie.id) }, modifier = Modifier.fillMaxWidth())
                }
                items(savedSeries, key = { "s_${it.id}" }) { series ->
                    SeriesCard(series = series, onClick = { onSeriesClick(series.id) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
