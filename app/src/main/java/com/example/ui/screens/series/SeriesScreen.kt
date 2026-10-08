package com.example.ui.screens.series

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
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.SeriesRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SeriesCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SeriesScreen(
    seriesRepository: SeriesRepository,
    onSeriesClick: (String) -> Unit
) {
    val seriesList by seriesRepository.publishedSeries.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = "Web Series",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Binge-worthy shows, original series & multi-season dramas",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        if (seriesList.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Tv,
                title = "No Web Series Added Yet",
                message = "Web series and seasons will appear here as soon as the admin creates them.",
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
                items(seriesList, key = { it.id }) { series ->
                    SeriesCard(
                        series = series,
                        onClick = { onSeriesClick(series.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
