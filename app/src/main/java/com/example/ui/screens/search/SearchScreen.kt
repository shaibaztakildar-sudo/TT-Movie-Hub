package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MovieCard
import com.example.ui.components.SeriesCard
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    movieRepository: MovieRepository,
    seriesRepository: SeriesRepository,
    onMovieClick: (String) -> Unit,
    onSeriesClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") } // "All", "Movies", "Web Series"
    var selectedGenre by remember { mutableStateOf("All Genres") }

    val movies by movieRepository.publishedMovies.collectAsState(initial = emptyList())
    val seriesList by seriesRepository.publishedSeries.collectAsState(initial = emptyList())

    val allGenres = remember(movies, seriesList) {
        val g = (movies.map { it.genre } + seriesList.map { it.genre })
            .filter { it.isNotBlank() }
            .distinct()
        listOf("All Genres") + g
    }

    val filteredMovies = remember(movies, searchQuery, selectedType, selectedGenre) {
        if (selectedType == "Web Series") return@remember emptyList()
        val q = searchQuery.trim().lowercase()
        movies.filter { movie ->
            val matchesQuery = q.isEmpty() ||
                movie.title.lowercase().contains(q) ||
                movie.genre.lowercase().contains(q) ||
                movie.language.lowercase().contains(q) ||
                movie.cast.lowercase().contains(q) ||
                movie.director.lowercase().contains(q)

            val matchesGenre = selectedGenre == "All Genres" ||
                movie.genre.equals(selectedGenre, ignoreCase = true)

            matchesQuery && matchesGenre
        }
    }

    val filteredSeries = remember(seriesList, searchQuery, selectedType, selectedGenre) {
        if (selectedType == "Movies") return@remember emptyList()
        val q = searchQuery.trim().lowercase()
        seriesList.filter { series ->
            val matchesQuery = q.isEmpty() ||
                series.title.lowercase().contains(q) ||
                series.genre.lowercase().contains(q) ||
                series.language.lowercase().contains(q) ||
                series.cast.lowercase().contains(q) ||
                series.director.lowercase().contains(q)

            val matchesGenre = selectedGenre == "All Genres" ||
                series.genre.equals(selectedGenre, ignoreCase = true)

            matchesQuery && matchesGenre
        }
    }

    val totalResultsCount = filteredMovies.size + filteredSeries.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Search Input Bar
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search movies, series, actors, directors...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandRed,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    cursorColor = BrandRed
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Type filter chips: All, Movies, Web Series
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Movies", "Web Series")) { type ->
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type) },
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

                if (allGenres.size > 1) {
                    items(allGenres) { genre ->
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
        }

        // Search Results
        if (totalResultsCount == 0) {
            EmptyStateView(
                icon = Icons.Default.Search,
                title = if (searchQuery.isEmpty()) "Search TT Movie Hub" else "No Results Found",
                message = if (searchQuery.isEmpty()) {
                    "Type a movie or series title, genre, actor, or director to discover entertainment."
                } else {
                    "No movies or web series matched '$searchQuery'. Try checking spelling or using different keywords."
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
                items(filteredMovies, key = { "m_${it.id}" }) { movie ->
                    MovieCard(movie = movie, onClick = { onMovieClick(movie.id) }, modifier = Modifier.fillMaxWidth())
                }
                items(filteredSeries, key = { "s_${it.id}" }) { series ->
                    SeriesCard(series = series, onClick = { onSeriesClick(series.id) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
