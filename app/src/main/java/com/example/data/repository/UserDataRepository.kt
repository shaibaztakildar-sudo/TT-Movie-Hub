package com.example.data.repository

import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DownloadDao
import com.example.data.local.dao.MovieDao
import com.example.data.local.dao.PlaybackHistoryDao
import com.example.data.local.dao.SeriesDao
import com.example.data.local.dao.WatchlistDao
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.GenreEntity
import com.example.data.local.entity.LanguageEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.WatchlistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class UserDataRepository(
    private val watchlistDao: WatchlistDao,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val downloadDao: DownloadDao,
    private val categoryDao: CategoryDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val mediaStorageManager: MediaStorageManager
) {
    // Watchlist
    fun getWatchlistForUser(userId: String): Flow<List<WatchlistEntity>> =
        watchlistDao.getWatchlistForUserFlow(userId)

    fun isInWatchlistFlow(userId: String, contentId: String): Flow<Boolean> =
        watchlistDao.isInWatchlistFlow(userId, contentId)

    suspend fun isInWatchlist(userId: String, contentId: String): Boolean =
        watchlistDao.isInWatchlist(userId, contentId)

    suspend fun toggleWatchlist(userId: String, contentId: String, contentType: String): Boolean {
        val inList = watchlistDao.isInWatchlist(userId, contentId)
        if (inList) {
            watchlistDao.deleteFromWatchlist(userId, contentId)
            return false
        } else {
            val item = WatchlistEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                contentId = contentId,
                contentType = contentType
            )
            watchlistDao.insertWatchlist(item)
            return true
        }
    }

    // Playback History / Continue Watching
    fun getContinueWatchingFlow(userId: String): Flow<List<PlaybackHistoryEntity>> =
        playbackHistoryDao.getHistoryForUserFlow(userId)

    suspend fun getSavedPosition(userId: String, contentId: String): Long {
        return playbackHistoryDao.getPlaybackPosition(userId, contentId)?.positionMs ?: 0L
    }

    suspend fun savePlaybackPosition(
        userId: String,
        contentId: String,
        contentType: String,
        title: String,
        subtitleInfo: String = "",
        thumbnailUri: String,
        videoUri: String,
        positionMs: Long,
        durationMs: Long,
        seriesId: String? = null
    ) {
        if (durationMs > 0 && positionMs >= durationMs - 5000) {
            // Reached near end of video: clear from continue watching
            playbackHistoryDao.deletePlaybackHistory(userId, contentId)
            return
        }
        if (positionMs <= 1000) return // Skip saving if just started

        val existing = playbackHistoryDao.getPlaybackPosition(userId, contentId)
        val history = PlaybackHistoryEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            userId = userId,
            contentId = contentId,
            contentType = contentType,
            title = title,
            subtitleInfo = subtitleInfo,
            thumbnailUri = thumbnailUri,
            videoUri = videoUri,
            positionMs = positionMs,
            durationMs = durationMs,
            seriesId = seriesId,
            lastWatchedAt = System.currentTimeMillis()
        )
        playbackHistoryDao.upsertPlaybackHistory(history)
    }

    suspend fun removeContinueWatching(userId: String, contentId: String) {
        playbackHistoryDao.deletePlaybackHistory(userId, contentId)
    }

    // Downloads
    fun getDownloadsFlow(userId: String): Flow<List<DownloadEntity>> =
        downloadDao.getDownloadsForUserFlow(userId)

    suspend fun startDownload(
        userId: String,
        contentId: String,
        contentType: String,
        title: String,
        subtitleInfo: String,
        thumbnailUri: String,
        videoUri: String
    ): Result<Unit> {
        val existing = downloadDao.getDownload(userId, contentId)
        if (existing != null && existing.downloadStatus == "COMPLETED") {
            return Result.success(Unit) // Already downloaded
        }

        val downloadId = existing?.id ?: UUID.randomUUID().toString()
        val downloadEntity = DownloadEntity(
            id = downloadId,
            userId = userId,
            contentId = contentId,
            contentType = contentType,
            title = title,
            subtitleInfo = subtitleInfo,
            thumbnailUri = thumbnailUri,
            localFilePath = "",
            fileSizeBytes = 0L,
            downloadStatus = "DOWNLOADING",
            progressPercent = 0
        )
        downloadDao.insertDownload(downloadEntity)

        // Real file copy/download in background
        val downloadResult = mediaStorageManager.downloadMediaFile(
            sourceUrlOrPath = videoUri,
            subfolder = "offline_videos",
            prefix = "vid_${contentId.take(8)}"
        ) { progressPercent ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                downloadDao.insertDownload(
                    downloadEntity.copy(progressPercent = progressPercent)
                )
            }
        }

        return if (downloadResult.isSuccess) {
            val (savedPath, sizeBytes) = downloadResult.getOrThrow()
            downloadDao.updateDownload(
                downloadEntity.copy(
                    localFilePath = savedPath,
                    fileSizeBytes = sizeBytes,
                    downloadStatus = "COMPLETED",
                    progressPercent = 100
                )
            )
            Result.success(Unit)
        } else {
            downloadDao.updateDownload(
                downloadEntity.copy(
                    downloadStatus = "FAILED",
                    progressPercent = 0
                )
            )
            Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
        }
    }

    suspend fun deleteDownload(downloadId: String) {
        val download = downloadDao.getDownloadById(downloadId)
        if (download != null) {
            if (download.localFilePath.isNotEmpty()) {
                mediaStorageManager.deleteFile(download.localFilePath)
            }
            downloadDao.deleteDownload(downloadId)
        }
    }

    // Categories (Genres & Languages)
    val allGenres: Flow<List<GenreEntity>> = categoryDao.getAllGenresFlow()
    val allLanguages: Flow<List<LanguageEntity>> = categoryDao.getAllLanguagesFlow()

    suspend fun addGenre(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            categoryDao.insertGenre(GenreEntity(id = UUID.randomUUID().toString(), name = trimmed))
        }
    }

    suspend fun deleteGenre(id: String) {
        categoryDao.deleteGenre(id)
    }

    suspend fun addLanguage(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty()) {
            categoryDao.insertLanguage(LanguageEntity(id = UUID.randomUUID().toString(), name = trimmed))
        }
    }

    suspend fun deleteLanguage(id: String) {
        categoryDao.deleteLanguage(id)
    }

    suspend fun getTotalDownloadsCount(): Int = downloadDao.getTotalDownloadsCount()
}
