package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val passwordHash: String,
    val role: String, // "ADMIN" or "USER"
    val isActive: Boolean = true,
    val profilePhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val posterUri: String,
    val bannerUri: String,
    val description: String,
    val genre: String,
    val language: String,
    val releaseYear: Int,
    val durationMinutes: Int,
    val cast: String,
    val director: String,
    val ageRating: String, // e.g., "U/A 16+", "A", "U"
    val trailerUri: String = "",
    val videoUri: String,
    val subtitleUri: String? = null,
    val isDownloadable: Boolean = true,
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val rating: Float = 0.0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val posterUri: String,
    val bannerUri: String,
    val description: String,
    val genre: String,
    val language: String,
    val releaseYear: Int,
    val cast: String,
    val director: String,
    val ageRating: String,
    val trailerUri: String = "",
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val rating: Float = 0.0f,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "seasons")
data class SeasonEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonNumber: Int,
    val title: String
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val seasonId: String,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val description: String,
    val thumbnailUri: String,
    val videoUri: String,
    val subtitleUri: String? = null,
    val durationMinutes: Int,
    val isDownloadable: Boolean = true,
    val isPublished: Boolean = true
)

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey
    val id: String,
    val name: String
)

@Entity(tableName = "languages")
data class LanguageEntity(
    @PrimaryKey
    val id: String,
    val name: String
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String,
    val contentType: String, // "MOVIE" or "SERIES"
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String, // movieId or episodeId
    val contentType: String, // "MOVIE" or "EPISODE"
    val title: String,
    val subtitleInfo: String = "", // e.g. "S1 E2" for episode
    val thumbnailUri: String,
    val videoUri: String,
    val positionMs: Long,
    val durationMs: Long,
    val seriesId: String? = null,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val contentId: String,
    val contentType: String, // "MOVIE" or "EPISODE"
    val title: String,
    val subtitleInfo: String = "",
    val thumbnailUri: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val downloadStatus: String, // "DOWNLOADING", "COMPLETED", "FAILED"
    val progressPercent: Int = 0,
    val downloadedAt: Long = System.currentTimeMillis()
)
