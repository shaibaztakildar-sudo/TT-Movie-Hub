package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.SeriesEntity
import com.example.ui.theme.BrandGold
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun MovieCard(
    movie: MovieEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 140,
    height: Int = 210
) {
    Card(
        modifier = modifier
            .width(width.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp)
                    .background(DarkSurface)
            ) {
                val imageModel = resolveImageModel(movie.posterUri)
                if (imageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Gradient bottom scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )

                // Rating / Year Badge
                if (movie.rating > 0f) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", movie.rating),
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Title & genre
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${movie.releaseYear}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${movie.durationMinutes}m",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SeriesCard(
    series: SeriesEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Int = 140,
    height: Int = 210
) {
    Card(
        modifier = modifier
            .width(width.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height.dp)
                    .background(DarkSurface)
            ) {
                val imageModel = resolveImageModel(series.posterUri)
                if (imageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = series.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Series tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(BrandRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SERIES",
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${series.genre} • ${series.releaseYear}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ContinueWatchingCard(
    history: PlaybackHistoryEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (history.durationMs > 0) {
        (history.positionMs.toFloat() / history.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
                    .background(DarkSurface)
            ) {
                val imageModel = resolveImageModel(history.thumbnailUri)
                if (imageModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = history.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Play icon overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Remove button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp),
                    color = BrandRed,
                    trackColor = DarkBorder,
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = history.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (history.subtitleInfo.isNotEmpty()) {
                    Text(
                        text = history.subtitleInfo,
                        color = BrandRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                val posSec = history.positionMs / 1000
                val durSec = history.durationMs / 1000
                Text(
                    text = "${formatTime(posSec)} / ${formatTime(durSec)}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

fun resolveImageModel(uriOrPath: String?): Any? {
    if (uriOrPath.isNullOrEmpty()) return null
    return if (uriOrPath.startsWith("/") || uriOrPath.startsWith("file://")) {
        val path = if (uriOrPath.startsWith("file://")) uriOrPath.substring(7) else uriOrPath
        val file = File(path)
        if (file.exists()) file else uriOrPath
    } else {
        uriOrPath
    }
}

fun formatTime(totalSeconds: Long): String {
    val hrs = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hrs > 0) {
        String.format("%d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
