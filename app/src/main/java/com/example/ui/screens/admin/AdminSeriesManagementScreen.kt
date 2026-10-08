package com.example.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.SeasonEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.repository.MediaStorageManager
import com.example.data.repository.SeriesRepository
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
import java.util.UUID

@Composable
fun AdminAddEditSeriesScreen(
    seriesId: String?,
    seriesRepository: SeriesRepository,
    mediaStorageManager: MediaStorageManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Drama") }
    var language by remember { mutableStateOf("Hindi") }
    var releaseYear by remember { mutableStateOf("2026") }
    var cast by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var ageRating by remember { mutableStateOf("U/A 16+") }
    var posterUri by remember { mutableStateOf("") }
    var bannerUri by remember { mutableStateOf("") }
    var trailerUri by remember { mutableStateOf("") }
    var isPublished by remember { mutableStateOf(true) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Upload states with retry support
    var isPosterUploading by remember { mutableStateOf(false) }
    var posterUploadProgress by remember { mutableFloatStateOf(0f) }
    var posterError by remember { mutableStateOf<String?>(null) }
    var lastPosterUri by remember { mutableStateOf<Uri?>(null) }

    var isBannerUploading by remember { mutableStateOf(false) }
    var bannerUploadProgress by remember { mutableFloatStateOf(0f) }
    var bannerError by remember { mutableStateOf<String?>(null) }
    var lastBannerUri by remember { mutableStateOf<Uri?>(null) }

    var isTrailerUploading by remember { mutableStateOf(false) }
    var trailerUploadProgress by remember { mutableFloatStateOf(0f) }
    var trailerError by remember { mutableStateOf<String?>(null) }
    var lastTrailerUri by remember { mutableStateOf<Uri?>(null) }

    val uploadPoster: (Uri) -> Unit = { uri ->
        lastPosterUri = uri
        isPosterUploading = true
        posterError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "series_posters", "poster") {
                posterUploadProgress = it
            }
            isPosterUploading = false
            if (res.isSuccess) {
                posterUri = res.getOrThrow()
            } else {
                posterError = res.exceptionOrNull()?.message ?: "Upload failed"
            }
        }
    }

    val uploadBanner: (Uri) -> Unit = { uri ->
        lastBannerUri = uri
        isBannerUploading = true
        bannerError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "series_banners", "banner") {
                bannerUploadProgress = it
            }
            isBannerUploading = false
            if (res.isSuccess) {
                bannerUri = res.getOrThrow()
            } else {
                bannerError = res.exceptionOrNull()?.message ?: "Upload failed"
            }
        }
    }

    val uploadTrailer: (Uri) -> Unit = { uri ->
        lastTrailerUri = uri
        isTrailerUploading = true
        trailerError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "series_trailers", "trailer") {
                trailerUploadProgress = it
            }
            isTrailerUploading = false
            if (res.isSuccess) {
                trailerUri = res.getOrThrow()
            } else {
                trailerError = res.exceptionOrNull()?.message ?: "Upload failed"
            }
        }
    }

    val posterPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) uploadPoster(uri) }

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) uploadBanner(uri) }

    val trailerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) uploadTrailer(uri) }

    LaunchedEffect(seriesId) {
        if (seriesId != null) {
            val existing = seriesRepository.getSeriesById(seriesId)
            if (existing != null) {
                title = existing.title
                description = existing.description
                genre = existing.genre
                language = existing.language
                releaseYear = existing.releaseYear.toString()
                cast = existing.cast
                director = existing.director
                ageRating = existing.ageRating
                posterUri = existing.posterUri
                bannerUri = existing.bannerUri
                trailerUri = existing.trailerUri
                isPublished = existing.isPublished
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (seriesId != null) "Edit Web Series" else "Add Web Series",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                Text(text = errorMessage!!, color = StatusError, fontSize = 13.sp)
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; errorMessage = null },
                label = { Text("Series Title *") },
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("series_title_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Synopsis") },
                maxLines = 4,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = releaseYear,
                    onValueChange = { releaseYear = it },
                    label = { Text("Release Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ageRating,
                    onValueChange = { ageRating = it },
                    label = { Text("Age Rating") },
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = director,
                onValueChange = { director = it },
                label = { Text("Director / Creator") },
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = cast,
                onValueChange = { cast = it },
                label = { Text("Star Cast") },
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            UploadPickerCard(
                label = "Series Poster (Portrait)",
                currentPath = posterUri,
                isUploading = isPosterUploading,
                progress = posterUploadProgress,
                error = posterError,
                onRetry = { lastPosterUri?.let { uploadPoster(it) } },
                mediaType = "image",
                onPick = { posterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onManualInput = { posterUri = it; posterError = null }
            )

            UploadPickerCard(
                label = "Series Banner Cover (Landscape)",
                currentPath = bannerUri,
                isUploading = isBannerUploading,
                progress = bannerUploadProgress,
                error = bannerError,
                onRetry = { lastBannerUri?.let { uploadBanner(it) } },
                mediaType = "image",
                onPick = { bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onManualInput = { bannerUri = it; bannerError = null }
            )

            UploadPickerCard(
                label = "Series Trailer Video (Optional)",
                currentPath = trailerUri,
                isUploading = isTrailerUploading,
                progress = trailerUploadProgress,
                error = trailerError,
                onRetry = { lastTrailerUri?.let { uploadTrailer(it) } },
                mediaType = "video",
                onPick = { trailerPicker.launch("video/*") },
                onManualInput = { trailerUri = it; trailerError = null }
            )

            AdminSwitchRow("Published", "Visible to public users in the app", isPublished) { isPublished = it }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Save, Publish, Unpublish, Delete
            val onSaveSeriesAction: (Boolean) -> Unit = { shouldPublish ->
                if (title.isBlank()) {
                    errorMessage = "Please enter series title"
                } else {
                    isSaving = true
                    errorMessage = null
                    scope.launch {
                        val s = SeriesEntity(
                            id = seriesId ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            posterUri = posterUri.trim(),
                            bannerUri = bannerUri.trim(),
                            description = description.trim(),
                            genre = genre.trim().ifEmpty { "Drama" },
                            language = language.trim().ifEmpty { "Hindi" },
                            releaseYear = releaseYear.toIntOrNull() ?: 2026,
                            cast = cast.trim(),
                            director = director.trim(),
                            ageRating = ageRating.trim().ifEmpty { "U/A 16+" },
                            trailerUri = trailerUri.trim(),
                            isPublished = shouldPublish
                        )
                        seriesRepository.saveSeries(s)
                        isSaving = false
                        onBack()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSaveSeriesAction(isPublished) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("save_series_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (seriesId != null) "Save" else "Create", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Button(
                    onClick = { onSaveSeriesAction(true) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }

                Button(
                    onClick = { onSaveSeriesAction(false) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Unpublish", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
            }

            if (seriesId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("delete_series_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Series & All Episodes", color = StatusError, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showDeleteConfirmDialog && seriesId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Web Series", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete \"$title\" and all associated seasons and episodes?", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                seriesRepository.deleteSeries(seriesId)
                                showDeleteConfirmDialog = false
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                    ) {
                        Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}

@Composable
fun AdminManageEpisodesScreen(
    seriesId: String,
    seriesRepository: SeriesRepository,
    mediaStorageManager: MediaStorageManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
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

    var showAddSeasonDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    var editingEpisode by remember { mutableStateOf<EpisodeEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = series?.title ?: "Web Series",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Text(text = "Manage Seasons & Episodes", style = MaterialTheme.typography.bodySmall.copy(color = BrandGold))
            }
            Button(
                onClick = { showAddSeasonDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("add_season_btn")
            ) {
                Text("+ Season", fontSize = 12.sp)
            }
        }

        // Seasons Selection Row
        if (seasons.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No seasons created yet.", color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val s1 = SeasonEntity(
                                    id = UUID.randomUUID().toString(),
                                    seriesId = seriesId,
                                    seasonNumber = 1,
                                    title = "Season 1"
                                )
                                seriesRepository.saveSeason(s1)
                                selectedSeasonId = s1.id
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text("Create Season 1")
                    }
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(seasons, key = { it.id }) { season ->
                    val isSelected = selectedSeasonId == season.id
                    Button(
                        onClick = { selectedSeasonId = season.id },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) BrandRed else DarkSurfaceElevated,
                            contentColor = if (isSelected) Color.White else TextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(season.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Season Action Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val curSeason = seasons.find { it.id == selectedSeasonId }
                Text(
                    text = "${curSeason?.title ?: "Season"} • ${episodes.size} Episodes",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Button(
                    onClick = {
                        editingEpisode = null
                        showEpisodeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("add_episode_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BrandGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Episode", color = BrandGold, fontSize = 12.sp)
                }
            }
        }

        // Episodes List
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(episodes, key = { it.id }) { ep ->
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
                                    text = "Ep ${ep.episodeNumber}: ${ep.title}",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (ep.isPublished) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (ep.isPublished) "PUBLISHED" else "DRAFT",
                                        color = if (ep.isPublished) StatusSuccess else StatusError,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${ep.durationMinutes} mins • ${if (ep.isDownloadable) "Downloadable" else "Stream only"}${if (!ep.subtitleUri.isNullOrEmpty()) " • Subtitled" else ""}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Toggle Publish
                        IconButton(onClick = { scope.launch { seriesRepository.toggleEpisodePublish(ep) } }) {
                            Icon(
                                imageVector = if (ep.isPublished) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle",
                                tint = if (ep.isPublished) StatusSuccess else TextMuted
                            )
                        }
                        // Edit Episode
                        IconButton(onClick = {
                            editingEpisode = ep
                            showEpisodeDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextPrimary)
                        }
                        // Delete Episode
                        IconButton(onClick = { scope.launch { seriesRepository.deleteEpisode(ep.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                        }
                    }
                }
            }
        }

        // Add Season Dialog
        if (showAddSeasonDialog) {
            var seasonNumberText by remember { mutableStateOf((seasons.size + 1).toString()) }
            var seasonTitleText by remember { mutableStateOf("Season ${seasons.size + 1}") }

            AlertDialog(
                onDismissRequest = { showAddSeasonDialog = false },
                title = { Text("Add New Season", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = seasonTitleText,
                            onValueChange = { seasonTitleText = it },
                            label = { Text("Season Title (e.g. Season 2)") },
                            colors = adminTextFieldColors()
                        )
                        OutlinedTextField(
                            value = seasonNumberText,
                            onValueChange = { seasonNumberText = it },
                            label = { Text("Season Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = adminTextFieldColors()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val num = seasonNumberText.toIntOrNull() ?: (seasons.size + 1)
                            val season = SeasonEntity(
                                id = UUID.randomUUID().toString(),
                                seriesId = seriesId,
                                seasonNumber = num,
                                title = seasonTitleText.trim().ifEmpty { "Season $num" }
                            )
                            scope.launch {
                                seriesRepository.saveSeason(season)
                                selectedSeasonId = season.id
                                showAddSeasonDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text("Add Season")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddSeasonDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }

        // Add / Edit Episode Dialog with Gallery Uploads & Full Fields
        if (showEpisodeDialog && selectedSeasonId != null) {
            val curSeason = seasons.find { it.id == selectedSeasonId }
            var epTitle by remember { mutableStateOf(editingEpisode?.title ?: "") }
            var epNumber by remember { mutableStateOf(editingEpisode?.episodeNumber?.toString() ?: (episodes.size + 1).toString()) }
            var epDesc by remember { mutableStateOf(editingEpisode?.description ?: "") }
            var epDuration by remember { mutableStateOf(editingEpisode?.durationMinutes?.toString() ?: "45") }
            var epThumbUri by remember { mutableStateOf(editingEpisode?.thumbnailUri ?: "") }
            var epVideoUri by remember { mutableStateOf(editingEpisode?.videoUri ?: "") }
            var epSubtitleUri by remember { mutableStateOf(editingEpisode?.subtitleUri ?: "") }
            var epDownloadable by remember { mutableStateOf(editingEpisode?.isDownloadable ?: true) }
            var epPublished by remember { mutableStateOf(editingEpisode?.isPublished ?: true) }

            // Upload states for Episode
            var isEpThumbUploading by remember { mutableStateOf(false) }
            var epThumbProgress by remember { mutableFloatStateOf(0f) }
            var epThumbError by remember { mutableStateOf<String?>(null) }
            var lastEpThumbUri by remember { mutableStateOf<Uri?>(null) }

            var isEpVidUploading by remember { mutableStateOf(false) }
            var epVidProgress by remember { mutableFloatStateOf(0f) }
            var epVidError by remember { mutableStateOf<String?>(null) }
            var lastEpVidUri by remember { mutableStateOf<Uri?>(null) }

            var isEpSubUploading by remember { mutableStateOf(false) }
            var epSubProgress by remember { mutableFloatStateOf(0f) }
            var epSubError by remember { mutableStateOf<String?>(null) }
            var lastEpSubUri by remember { mutableStateOf<Uri?>(null) }

            val uploadEpThumb: (Uri) -> Unit = { uri ->
                lastEpThumbUri = uri
                isEpThumbUploading = true
                epThumbError = null
                scope.launch {
                    val res = mediaStorageManager.saveMediaFromUri(uri, "episodes", "ep_thumb") {
                        epThumbProgress = it
                    }
                    isEpThumbUploading = false
                    if (res.isSuccess) {
                        epThumbUri = res.getOrThrow()
                    } else {
                        epThumbError = res.exceptionOrNull()?.message ?: "Upload failed"
                    }
                }
            }

            val uploadEpVid: (Uri) -> Unit = { uri ->
                lastEpVidUri = uri
                isEpVidUploading = true
                epVidError = null
                scope.launch {
                    val res = mediaStorageManager.saveMediaFromUri(uri, "episodes", "ep_vid") {
                        epVidProgress = it
                    }
                    isEpVidUploading = false
                    if (res.isSuccess) {
                        epVideoUri = res.getOrThrow()
                    } else {
                        epVidError = res.exceptionOrNull()?.message ?: "Upload failed"
                    }
                }
            }

            val uploadEpSub: (Uri) -> Unit = { uri ->
                lastEpSubUri = uri
                isEpSubUploading = true
                epSubError = null
                scope.launch {
                    val res = mediaStorageManager.saveMediaFromUri(uri, "episodes", "ep_sub") {
                        epSubProgress = it
                    }
                    isEpSubUploading = false
                    if (res.isSuccess) {
                        epSubtitleUri = res.getOrThrow()
                    } else {
                        epSubError = res.exceptionOrNull()?.message ?: "Upload failed"
                    }
                }
            }

            val epThumbPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri -> if (uri != null) uploadEpThumb(uri) }

            val epVidPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri -> if (uri != null) uploadEpVid(uri) }

            val epSubPicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri -> if (uri != null) uploadEpSub(uri) }

            AlertDialog(
                onDismissRequest = { showEpisodeDialog = false },
                title = { Text(if (editingEpisode != null) "Edit Episode" else "Add Episode to ${curSeason?.title}", color = TextPrimary) },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = epTitle,
                            onValueChange = { epTitle = it },
                            label = { Text("Episode Title *") },
                            colors = adminTextFieldColors()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = epNumber,
                                onValueChange = { epNumber = it },
                                label = { Text("Episode Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = adminTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = epDuration,
                                onValueChange = { epDuration = it },
                                label = { Text("Duration (mins)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = adminTextFieldColors(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = epDesc,
                            onValueChange = { epDesc = it },
                            label = { Text("Description") },
                            colors = adminTextFieldColors()
                        )

                        UploadPickerCard(
                            label = "Episode Thumbnail",
                            currentPath = epThumbUri,
                            isUploading = isEpThumbUploading,
                            progress = epThumbProgress,
                            error = epThumbError,
                            onRetry = { lastEpThumbUri?.let { uploadEpThumb(it) } },
                            mediaType = "image",
                            onPick = { epThumbPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onManualInput = { epThumbUri = it; epThumbError = null }
                        )

                        UploadPickerCard(
                            label = "Episode Video File *",
                            currentPath = epVideoUri,
                            isUploading = isEpVidUploading,
                            progress = epVidProgress,
                            error = epVidError,
                            onRetry = { lastEpVidUri?.let { uploadEpVid(it) } },
                            mediaType = "video",
                            onPick = { epVidPicker.launch("video/*") },
                            onManualInput = { epVideoUri = it; epVidError = null }
                        )

                        UploadPickerCard(
                            label = "Episode Subtitle / Captions (Optional)",
                            currentPath = epSubtitleUri,
                            isUploading = isEpSubUploading,
                            progress = epSubProgress,
                            error = epSubError,
                            onRetry = { lastEpSubUri?.let { uploadEpSub(it) } },
                            mediaType = "subtitle",
                            onPick = { epSubPicker.launch("*/*") },
                            onManualInput = { epSubtitleUri = it; epSubError = null }
                        )

                        AdminSwitchRow("Download Available", "Allow offline download for this episode", epDownloadable) { epDownloadable = it }
                        AdminSwitchRow("Published", "Visible to users in the series catalog", epPublished) { epPublished = it }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (epTitle.isNotBlank() && epVideoUri.isNotBlank()) {
                                val ep = EpisodeEntity(
                                    id = editingEpisode?.id ?: UUID.randomUUID().toString(),
                                    seriesId = seriesId,
                                    seasonId = selectedSeasonId!!,
                                    seasonNumber = curSeason?.seasonNumber ?: 1,
                                    episodeNumber = epNumber.toIntOrNull() ?: (episodes.size + 1),
                                    title = epTitle.trim(),
                                    description = epDesc.trim(),
                                    thumbnailUri = epThumbUri.trim(),
                                    videoUri = epVideoUri.trim(),
                                    subtitleUri = epSubtitleUri.trim().ifEmpty { null },
                                    durationMinutes = epDuration.toIntOrNull() ?: 45,
                                    isDownloadable = epDownloadable,
                                    isPublished = epPublished
                                )
                                scope.launch {
                                    seriesRepository.saveEpisode(ep)
                                    showEpisodeDialog = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                    ) {
                        Text(if (editingEpisode != null) "Update Episode" else "Save Episode")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showEpisodeDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }
    }
}
