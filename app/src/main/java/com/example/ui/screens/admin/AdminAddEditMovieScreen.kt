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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.entity.MovieEntity
import com.example.data.repository.MediaStorageManager
import com.example.data.repository.MovieRepository
import com.example.data.repository.UserDataRepository
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditMovieScreen(
    movieId: String?,
    movieRepository: MovieRepository,
    userDataRepository: UserDataRepository,
    mediaStorageManager: MediaStorageManager,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Action") }
    var language by remember { mutableStateOf("Hindi") }
    var releaseYear by remember { mutableStateOf("2026") }
    var durationMinutes by remember { mutableStateOf("120") }
    var cast by remember { mutableStateOf("") }
    var director by remember { mutableStateOf("") }
    var ageRating by remember { mutableStateOf("U/A 16+") }
    var rating by remember { mutableFloatStateOf(8.0f) }

    var posterUri by remember { mutableStateOf("") }
    var bannerUri by remember { mutableStateOf("") }
    var videoUri by remember { mutableStateOf("") }
    var trailerUri by remember { mutableStateOf("") }
    var subtitleUri by remember { mutableStateOf("") }

    var isDownloadable by remember { mutableStateOf(true) }
    var isPublished by remember { mutableStateOf(true) }
    var isFeatured by remember { mutableStateOf(false) }
    var isTrending by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Upload Progress & Error Trackers
    var posterUploadProgress by remember { mutableFloatStateOf(0f) }
    var isPosterUploading by remember { mutableStateOf(false) }
    var posterError by remember { mutableStateOf<String?>(null) }
    var lastPosterUri by remember { mutableStateOf<Uri?>(null) }

    var bannerUploadProgress by remember { mutableFloatStateOf(0f) }
    var isBannerUploading by remember { mutableStateOf(false) }
    var bannerError by remember { mutableStateOf<String?>(null) }
    var lastBannerUri by remember { mutableStateOf<Uri?>(null) }

    var videoUploadProgress by remember { mutableFloatStateOf(0f) }
    var isVideoUploading by remember { mutableStateOf(false) }
    var videoError by remember { mutableStateOf<String?>(null) }
    var lastVideoUri by remember { mutableStateOf<Uri?>(null) }

    var trailerUploadProgress by remember { mutableFloatStateOf(0f) }
    var isTrailerUploading by remember { mutableStateOf(false) }
    var trailerError by remember { mutableStateOf<String?>(null) }
    var lastTrailerUri by remember { mutableStateOf<Uri?>(null) }

    var subtitleUploadProgress by remember { mutableFloatStateOf(0f) }
    var isSubtitleUploading by remember { mutableStateOf(false) }
    var subtitleError by remember { mutableStateOf<String?>(null) }
    var lastSubtitleUri by remember { mutableStateOf<Uri?>(null) }

    // Upload Execution Functions with Retry
    val uploadPoster: (Uri) -> Unit = { uri ->
        lastPosterUri = uri
        isPosterUploading = true
        posterError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "posters", "poster") {
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
            val res = mediaStorageManager.saveMediaFromUri(uri, "banners", "banner") {
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

    val uploadVideo: (Uri) -> Unit = { uri ->
        lastVideoUri = uri
        isVideoUploading = true
        videoError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "videos", "main_video") {
                videoUploadProgress = it
            }
            isVideoUploading = false
            if (res.isSuccess) {
                videoUri = res.getOrThrow()
            } else {
                videoError = res.exceptionOrNull()?.message ?: "Upload failed"
            }
        }
    }

    val uploadTrailer: (Uri) -> Unit = { uri ->
        lastTrailerUri = uri
        isTrailerUploading = true
        trailerError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "trailers", "trailer") {
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

    val uploadSubtitle: (Uri) -> Unit = { uri ->
        lastSubtitleUri = uri
        isSubtitleUploading = true
        subtitleError = null
        scope.launch {
            val res = mediaStorageManager.saveMediaFromUri(uri, "subtitles", "subtitle") {
                subtitleUploadProgress = it
            }
            isSubtitleUploading = false
            if (res.isSuccess) {
                subtitleUri = res.getOrThrow()
            } else {
                subtitleError = res.exceptionOrNull()?.message ?: "Upload failed"
            }
        }
    }

    // Gallery / File Pickers
    val posterPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) uploadPoster(uri) }

    val bannerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> if (uri != null) uploadBanner(uri) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) uploadVideo(uri) }

    val trailerPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) uploadTrailer(uri) }

    val subtitlePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) uploadSubtitle(uri) }

    // Load existing movie for Edit
    LaunchedEffect(movieId) {
        if (movieId != null) {
            val existing = movieRepository.getMovieById(movieId)
            if (existing != null) {
                title = existing.title
                description = existing.description
                genre = existing.genre
                language = existing.language
                releaseYear = existing.releaseYear.toString()
                durationMinutes = existing.durationMinutes.toString()
                cast = existing.cast
                director = existing.director
                ageRating = existing.ageRating
                rating = existing.rating
                posterUri = existing.posterUri
                bannerUri = existing.bannerUri
                videoUri = existing.videoUri
                trailerUri = existing.trailerUri
                subtitleUri = existing.subtitleUri ?: ""
                isDownloadable = existing.isDownloadable
                isPublished = existing.isPublished
                isFeatured = existing.isFeatured
                isTrending = existing.isTrending
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (movieId != null) "Edit Movie" else "Add New Movie",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
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
                Text(
                    text = errorMessage!!,
                    color = StatusError,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Movie Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Movie Title *") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_movie_title_input")
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description / Storyline") },
                maxLines = 4,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Genre & Language Row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = language,
                    onValueChange = { language = it },
                    label = { Text("Language") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Year & Duration Row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = releaseYear,
                    onValueChange = { releaseYear = it },
                    label = { Text("Release Year") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    label = { Text("Duration (mins)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = adminTextFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Cast & Director
            OutlinedTextField(
                value = cast,
                onValueChange = { cast = it },
                label = { Text("Star Cast") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = director,
                onValueChange = { director = it },
                label = { Text("Director") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Age Rating
            OutlinedTextField(
                value = ageRating,
                onValueChange = { ageRating = it },
                label = { Text("Age Rating (e.g. U/A 16+, A, PG-13)") },
                singleLine = true,
                colors = adminTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Media Upload Section
            Text(
                text = "Media Uploads (Phone Gallery / File Picker)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrandGold
                ),
                modifier = Modifier.padding(top = 10.dp)
            )

            // 1. Poster Image Picker
            UploadPickerCard(
                label = "Poster Image (Portrait)",
                currentPath = posterUri,
                isUploading = isPosterUploading,
                progress = posterUploadProgress,
                error = posterError,
                onRetry = { lastPosterUri?.let { uploadPoster(it) } },
                mediaType = "image",
                onPick = {
                    posterPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onManualInput = { posterUri = it; posterError = null }
            )

            // 2. Banner Image Picker
            UploadPickerCard(
                label = "Banner / Cover (Landscape)",
                currentPath = bannerUri,
                isUploading = isBannerUploading,
                progress = bannerUploadProgress,
                error = bannerError,
                onRetry = { lastBannerUri?.let { uploadBanner(it) } },
                mediaType = "image",
                onPick = {
                    bannerPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onManualInput = { bannerUri = it; bannerError = null }
            )

            // 3. Main Movie Video Picker
            UploadPickerCard(
                label = "Main Movie Video File *",
                currentPath = videoUri,
                isUploading = isVideoUploading,
                progress = videoUploadProgress,
                error = videoError,
                onRetry = { lastVideoUri?.let { uploadVideo(it) } },
                mediaType = "video",
                onPick = { videoPicker.launch("video/*") },
                onManualInput = { videoUri = it; videoError = null }
            )

            // 4. Trailer Video Picker
            UploadPickerCard(
                label = "Trailer Video (Optional)",
                currentPath = trailerUri,
                isUploading = isTrailerUploading,
                progress = trailerUploadProgress,
                error = trailerError,
                onRetry = { lastTrailerUri?.let { uploadTrailer(it) } },
                mediaType = "video",
                onPick = { trailerPicker.launch("video/*") },
                onManualInput = { trailerUri = it; trailerError = null }
            )

            // 5. Subtitle File Picker
            UploadPickerCard(
                label = "Subtitle / Caption File (Optional)",
                currentPath = subtitleUri,
                isUploading = isSubtitleUploading,
                progress = subtitleUploadProgress,
                error = subtitleError,
                onRetry = { lastSubtitleUri?.let { uploadSubtitle(it) } },
                mediaType = "subtitle",
                onPick = { subtitlePicker.launch("*/*") },
                onManualInput = { subtitleUri = it; subtitleError = null }
            )

            // Switches / Flags
            AdminSwitchRow("Download Available", "Allow users to download this movie offline", isDownloadable) { isDownloadable = it }
            AdminSwitchRow("Published", "Visible to public users in the app", isPublished) { isPublished = it }
            AdminSwitchRow("Featured on Banner", "Promote on top hero home carousel", isFeatured) { isFeatured = it }
            AdminSwitchRow("Trending Now", "Show in trending section", isTrending) { isTrending = it }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Save, Publish, Unpublish, Delete
            val onSaveMovieAction: (Boolean) -> Unit = { shouldPublish ->
                if (title.isBlank()) {
                    errorMessage = "Please enter a movie title"
                } else if (videoUri.isBlank()) {
                    errorMessage = "Please provide or upload a main video file/URL"
                } else {
                    isSaving = true
                    errorMessage = null
                    scope.launch {
                        val movieEntity = MovieEntity(
                            id = movieId ?: UUID.randomUUID().toString(),
                            title = title.trim(),
                            posterUri = posterUri.trim(),
                            bannerUri = bannerUri.trim(),
                            description = description.trim(),
                            genre = genre.trim().ifEmpty { "General" },
                            language = language.trim().ifEmpty { "Hindi" },
                            releaseYear = releaseYear.toIntOrNull() ?: 2026,
                            durationMinutes = durationMinutes.toIntOrNull() ?: 120,
                            cast = cast.trim(),
                            director = director.trim(),
                            ageRating = ageRating.trim().ifEmpty { "U/A 13+" },
                            trailerUri = trailerUri.trim(),
                            videoUri = videoUri.trim(),
                            subtitleUri = subtitleUri.trim().ifEmpty { null },
                            isDownloadable = isDownloadable,
                            isPublished = shouldPublish,
                            isFeatured = isFeatured,
                            isTrending = isTrending,
                            rating = rating
                        )
                        movieRepository.saveMovie(movieEntity)
                        isSaving = false
                        onBack()
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Save Button
                Button(
                    onClick = { onSaveMovieAction(isPublished) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_save_movie_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (movieId != null) "Save" else "Create", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Publish Button
                Button(
                    onClick = { onSaveMovieAction(true) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_publish_movie_button")
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }

                // Unpublish Button
                Button(
                    onClick = { onSaveMovieAction(false) },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp).testTag("admin_unpublish_movie_button")
                ) {
                    Text("Unpublish", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                }
            }

            // Delete Movie Button (When editing existing movie)
            if (movieId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError),
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("admin_delete_movie_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Movie From Database", color = StatusError, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showDeleteConfirmDialog && movieId != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Movie", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete \"$title\" from the database? This cannot be undone.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                movieRepository.deleteMovie(movieId)
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
fun UploadPickerCard(
    label: String,
    currentPath: String,
    isUploading: Boolean,
    progress: Float,
    error: String? = null,
    onRetry: (() -> Unit)? = null,
    mediaType: String = "image", // "image", "video", "subtitle"
    onPick: () -> Unit,
    onManualInput: (String) -> Unit
) {
    var showUrlInput by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))

            // State 1: Uploading with real progress
            if (isUploading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandRed, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading file... ${(progress * 100).toInt()}%", color = BrandRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = BrandRed,
                    trackColor = DarkBorder
                )
            }
            // State 2: Upload Failure with Retry option
            else if (error != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upload failed: $error",
                            color = StatusError,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                    if (onRetry != null) {
                        OutlinedButton(
                            onClick = onRetry,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 10.sp, color = BrandGold)
                        }
                    }
                }
            }
            // State 3: Upload Success / Loaded File
            else if (currentPath.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Loaded: ${currentPath.substringAfterLast('/')}",
                        color = StatusSuccess,
                        fontSize = 12.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPick,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val iconVector = when (mediaType) {
                        "video" -> Icons.Default.VideoFile
                        "subtitle" -> Icons.Default.Subtitles
                        else -> Icons.Default.Image
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BrandGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload From Phone", fontSize = 11.sp, color = TextPrimary)
                }

                OutlinedButton(
                    onClick = { showUrlInput = !showUrlInput },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(if (showUrlInput) "Hide URL" else "Paste URL", fontSize = 11.sp, color = TextSecondary)
                }
            }

            if (showUrlInput) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentPath,
                    onValueChange = onManualInput,
                    label = { Text("Stream URL / File path") },
                    singleLine = true,
                    colors = adminTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AdminSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandRed,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurface
            )
        )
    }
}

@Composable
fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandRed,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = BrandRed,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedContainerColor = DarkSurface,
    unfocusedContainerColor = DarkSurface
)
