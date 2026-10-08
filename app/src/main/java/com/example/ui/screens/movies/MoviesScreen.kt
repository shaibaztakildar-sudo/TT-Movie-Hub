package com.example.ui.screens.movies

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.MovieRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MovieCard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MoviesScreen(
    movieRepository: MovieRepository,
    onMovieClick: (String) -> Unit
) {
    val movies by movieRepository.publishedMovies.collectAsState(initial = emptyList())
    var selectedGenre by remember { mutableStateOf("All") }

    val genres = remember(movies) {
        listOf("All") + movies.map { it.genre }.filter { it.isNotBlank() }.distinct()
    }

    val filteredMovies = remember(movies, selectedGenre) {
        if (selectedGenre == "All") movies else movies.filter { it.genre.equals(selectedGenre, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "Movies",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Browse blockbuster movies and cinema favorites",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // Genre filter chips
        if (genres.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(genres) { genre ->
                    val isSelected = selectedGenre == genre
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedGenre = genre },
                        label = { Text(genre) },
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
        }

        if (filteredMovies.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Movie,
                title = "No Movies Found",
                message = if (movies.isEmpty()) {
                    "No movies have been added to TT Movie Hub yet. When the admin publishes movies, they will appear right here."
                } else {
                    "No movies match the selected category '$selectedGenre'."
                },
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
                items(filteredMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
