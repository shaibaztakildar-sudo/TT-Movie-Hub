package com.example.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.repository.UserDataRepository
import com.example.ui.components.formatTime
import com.example.ui.theme.BrandRed
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VideoPlayerScreen(
    contentId: String,
    contentType: String, // "MOVIE" or "EPISODE"
    videoUri: String,
    title: String,
    subtitleInfo: String = "",
    thumbnailUri: String = "",
    startPositionMs: Long = 0L,
    seriesId: String? = null,
    currentUserId: String?,
    userDataRepository: UserDataRepository,
    onBack: () -> Unit,
    onNextEpisode: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentPositionMs by remember { mutableLongStateOf(startPositionMs) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekSliderPosition by remember { mutableFloatStateOf(0f) }

    var controlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    var showResumeDialog by remember { mutableStateOf(startPositionMs > 15_000L) }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Handle Fullscreen orientation toggling
    fun toggleFullscreen() {
        isFullscreen = !isFullscreen
        activity?.let { act ->
            if (isFullscreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    // Set Media Item and prepare
    LaunchedEffect(videoUri) {
        try {
            isError = false
            errorMessage = null
            isBuffering = true

            val mediaUri = when {
                videoUri.startsWith("http://", ignoreCase = true) ||
                videoUri.startsWith("https://", ignoreCase = true) -> Uri.parse(videoUri)
                videoUri.startsWith("content://", ignoreCase = true) -> Uri.parse(videoUri)
                videoUri.startsWith("file://", ignoreCase = true) -> Uri.parse(videoUri)
                else -> {
                    val file = File(videoUri)
                    if (file.exists()) Uri.fromFile(file) else Uri.parse(videoUri)
                }
            }

            val mediaItem = MediaItem.fromUri(mediaUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            if (startPositionMs > 0L) {
                exoPlayer.seekTo(startPositionMs)
            }
        } catch (e: Exception) {
            isError = true
            errorMessage = e.localizedMessage ?: "Failed to initialize video player"
        }
    }

    // Player state listener
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        durationMs = exoPlayer.duration.coerceAtLeast(0L)
                    }
                    Player.STATE_ENDED -> {
                        isBuffering = false
                        if (onNextEpisode != null) {
                            onNextEpisode()
                        }
                    }
                    Player.STATE_IDLE -> {
                        // idle
                    }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                isError = true
                errorMessage = "Playback error: ${error.localizedMessage ?: "Video unavailable or invalid format"}"
            }
        }

        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            // Save position on exit
            if (currentUserId != null && durationMs > 0) {
                val lastPos = exoPlayer.currentPosition
                scope.launch {
                    userDataRepository.savePlaybackPosition(
                        userId = currentUserId,
                        contentId = contentId,
                        contentType = contentType,
                        title = title,
                        subtitleInfo = subtitleInfo,
                        thumbnailUri = thumbnailUri,
                        videoUri = videoUri,
                        positionMs = lastPos,
                        durationMs = durationMs,
                        seriesId = seriesId
                    )
                }
            }
            // Restore portrait when exiting
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            exoPlayer.release()
        }
    }

    // Periodic time and auto-save tracker
    LaunchedEffect(Unit) {
        var tickCount = 0
        while (isActive) {
            if (exoPlayer.playbackState == Player.STATE_READY || exoPlayer.isPlaying) {
                if (!isUserSeeking) {
                    currentPositionMs = exoPlayer.currentPosition
                    durationMs = exoPlayer.duration.coerceAtLeast(0L)
                }
                tickCount++
                // Auto-save every 4 seconds
                if (tickCount >= 16 && currentUserId != null && durationMs > 0) {
                    tickCount = 0
                    userDataRepository.savePlaybackPosition(
                        userId = currentUserId,
                        contentId = contentId,
                        contentType = contentType,
                        title = title,
                        subtitleInfo = subtitleInfo,
                        thumbnailUri = thumbnailUri,
                        videoUri = videoUri,
                        positionMs = exoPlayer.currentPosition,
                        durationMs = durationMs,
                        seriesId = seriesId
                    )
                }
            }
            delay(250)
        }
    }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    BackHandler {
        if (isFullscreen) {
            toggleFullscreen()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                controlsVisible = !controlsVisible
            }
    ) {
        // Video Viewport
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom Compose controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering && !isError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(54.dp),
                    color = BrandRed,
                    strokeWidth = 3.dp
                )
            }
        }

        // Error State Overlay
        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = StatusError,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Unable to Play Video",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage ?: "The video stream or file could not be loaded.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row {
                        Button(
                            onClick = {
                                isError = false
                                exoPlayer.prepare()
                                exoPlayer.play()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                            modifier = Modifier.testTag("retry_playback_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }

        // Resume Prompt
        if (showResumeDialog && !isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Continue Watching?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You left off at ${formatTime(startPositionMs / 1000)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row {
                            Button(
                                onClick = {
                                    showResumeDialog = false
                                    exoPlayer.seekTo(startPositionMs)
                                    exoPlayer.play()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                                modifier = Modifier.testTag("resume_video_button")
                            ) {
                                Text("Resume")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    showResumeDialog = false
                                    exoPlayer.seekTo(0L)
                                    exoPlayer.play()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text("Start Over", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Custom Video Controls Overlay
        AnimatedVisibility(
            visible = controlsVisible && !isError && !showResumeDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            ) {
                // Top Header Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("player_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitleInfo.isNotEmpty()) {
                            Text(
                                text = subtitleInfo,
                                style = MaterialTheme.typography.bodySmall.copy(color = BrandRed),
                                maxLines = 1
                            )
                        }
                    }

                    // Playback speed menu button
                    Box {
                        IconButton(
                            onClick = { showSpeedMenu = true },
                            modifier = Modifier.testTag("player_speed_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${playbackSpeed}x",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${speed}x",
                                            color = if (playbackSpeed == speed) BrandRed else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        playbackSpeed = speed
                                        exoPlayer.setPlaybackSpeed(speed)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Mute / Volume toggle
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0.0f else 1.0f
                        },
                        modifier = Modifier.testTag("player_volume_toggle")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = TextPrimary
                        )
                    }

                    // Fullscreen toggle
                    IconButton(
                        onClick = { toggleFullscreen() },
                        modifier = Modifier.testTag("player_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Toggle Fullscreen",
                            tint = TextPrimary
                        )
                    }
                }

                // Center Seek & Playback Controls: [Replay 10s] [Play/Pause] [Forward 10s]
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 10s Backward Button
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .testTag("player_backward_10s_button")
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Main Play / Pause Button
                    IconButton(
                        onClick = {
                            if (exoPlayer.isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .testTag("player_play_pause_button")
                            .size(70.dp)
                            .background(BrandRed, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // 10s Forward Button
                    IconButton(
                        onClick = {
                            val maxDur = exoPlayer.duration.coerceAtLeast(0L)
                            val newPos = (exoPlayer.currentPosition + 10_000L).coerceAtMost(maxDur)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .testTag("player_forward_10s_button")
                            .size(54.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = TextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Seek Bar and Time Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Draggable and clickable Seek Bar
                    val currentSliderValue = if (isUserSeeking) {
                        seekSliderPosition
                    } else {
                        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                    }

                    Slider(
                        value = currentSliderValue,
                        onValueChange = { newValue ->
                            isUserSeeking = true
                            seekSliderPosition = newValue
                        },
                        onValueChangeFinished = {
                            val targetMs = (seekSliderPosition * durationMs).toLong()
                            exoPlayer.seekTo(targetMs)
                            currentPositionMs = targetMs
                            isUserSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = BrandRed,
                            activeTrackColor = BrandRed,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_progress_slider")
                    )

                    // Time display: MM:SS / MM:SS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayPos = if (isUserSeeking) (seekSliderPosition * durationMs).toLong() else currentPositionMs
                        Text(
                            text = formatTime(displayPos / 1000),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatTime(durationMs / 1000),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
