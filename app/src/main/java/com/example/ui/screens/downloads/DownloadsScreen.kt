package com.example.ui.screens.downloads

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.MediaStorageManager
import com.example.data.repository.UserDataRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.resolveImageModel
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
fun DownloadsScreen(
    currentUser: UserEntity?,
    userDataRepository: UserDataRepository,
    onPlayOffline: (DownloadEntity) -> Unit
) {
    val downloads by if (currentUser != null) {
        userDataRepository.getDownloadsFlow(currentUser.id).collectAsState(initial = emptyList())
    } else {
        remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    }

    val scope = rememberCoroutineScope()

    val downloadingItems = downloads.filter { it.downloadStatus == "DOWNLOADING" }
    val completedItems = downloads.filter { it.downloadStatus == "COMPLETED" }
    val failedItems = downloads.filter { it.downloadStatus == "FAILED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = "Downloads",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Watch your downloaded movies and episodes offline anywhere",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        if (currentUser == null) {
            EmptyStateView(
                icon = Icons.Default.FileDownload,
                title = "Sign In Required",
                message = "Sign in to manage and play your offline downloads.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else if (downloads.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FileDownload,
                title = "No Downloads Available",
                message = "Download movies and episodes enabled by the admin to watch offline without internet access.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Downloading Queue
                if (downloadingItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloading (${downloadingItems.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandRed
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(downloadingItems, key = { it.id }) { item ->
                        DownloadingCard(
                            item = item,
                            onCancel = { scope.launch { userDataRepository.deleteDownload(item.id) } }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Completed Downloads
                if (completedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloaded Content (${completedItems.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(completedItems, key = { it.id }) { item ->
                        CompletedDownloadCard(
                            item = item,
                            onPlay = { onPlayOffline(item) },
                            onDelete = { scope.launch { userDataRepository.deleteDownload(item.id) } }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Failed items
                if (failedItems.isNotEmpty()) {
                    item {
                        Text(
                            text = "Failed Downloads",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = StatusError
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(failedItems, key = { it.id }) { item ->
                        FailedDownloadCard(
                            item = item,
                            onDelete = { scope.launch { userDataRepository.deleteDownload(item.id) } }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadingCard(item: DownloadEntity, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(DarkSurface, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { item.progressPercent / 100f },
                        color = BrandRed,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Downloading... ${item.progressPercent}%",
                        color = BrandRed,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = TextMuted)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { item.progressPercent / 100f },
                color = BrandRed,
                trackColor = DarkBorder,
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
        }
    }
}

@Composable
fun CompletedDownloadCard(
    item: DownloadEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(46.dp)
                    .background(DarkSurface, RoundedCornerShape(4.dp))
            ) {
                val thumbModel = resolveImageModel(item.thumbnailUri)
                if (thumbModel != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(thumbModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.subtitleInfo.isNotEmpty()) {
                    Text(text = item.subtitleInfo, color = TextMuted, fontSize = 11.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DownloadDone, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ready offline • ${MediaStorageManager.formatFileSize(item.fileSizeBytes)}",
                        color = StatusSuccess,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_download_button")) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
            }
        }
    }
}

@Composable
fun FailedDownloadCard(item: DownloadEntity, onDelete: () -> Unit) {
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
                Text(text = item.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Download encountered an error", color = StatusError, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted)
            }
        }
    }
}
