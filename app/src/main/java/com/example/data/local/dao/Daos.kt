package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.EpisodeEntity
import com.example.data.local.entity.GenreEntity
import com.example.data.local.entity.LanguageEntity
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.PlaybackHistoryEntity
import com.example.data.local.entity.SeasonEntity
import com.example.data.local.entity.SeriesEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :identifier OR (phone != '' AND phone = :identifier) LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = :isActive WHERE id = :userId")
    suspend fun setUserActive(userId: String, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUsersCount(): Int

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>
}

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getPublishedMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isFeatured = 1 ORDER BY createdAt DESC")
    fun getFeaturedMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isPublished = 1 AND isTrending = 1 ORDER BY rating DESC")
    fun getTrendingMoviesFlow(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun getMovieByIdFlow(id: String): Flow<MovieEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovie(id: String)

    @Query("SELECT COUNT(*) FROM movies")
    suspend fun getTotalMoviesCount(): Int

    @Query("SELECT COUNT(*) FROM movies WHERE isPublished = 1")
    suspend fun getPublishedMoviesCount(): Int

    @Query("SELECT COUNT(*) FROM movies WHERE isPublished = 0")
    suspend fun getUnpublishedMoviesCount(): Int

    @Query("SELECT * FROM movies ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentMovies(limit: Int): List<MovieEntity>
}

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY createdAt DESC")
    fun getAllSeriesFlow(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 ORDER BY createdAt DESC")
    fun getPublishedSeriesFlow(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 AND isFeatured = 1 ORDER BY createdAt DESC")
    fun getFeaturedSeriesFlow(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isPublished = 1 AND isTrending = 1 ORDER BY rating DESC")
    fun getTrendingSeriesFlow(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    suspend fun getSeriesById(id: String): SeriesEntity?

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    fun getSeriesByIdFlow(id: String): Flow<SeriesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Query("DELETE FROM series WHERE id = :id")
    suspend fun deleteSeries(id: String)

    @Query("SELECT COUNT(*) FROM series")
    suspend fun getTotalSeriesCount(): Int

    @Query("SELECT COUNT(*) FROM series WHERE isPublished = 1")
    suspend fun getPublishedSeriesCount(): Int

    @Query("SELECT COUNT(*) FROM series WHERE isPublished = 0")
    suspend fun getUnpublishedSeriesCount(): Int

    @Query("SELECT * FROM series ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSeries(limit: Int): List<SeriesEntity>
}

@Dao
interface SeasonDao {
    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId ORDER BY seasonNumber ASC")
    fun getSeasonsForSeriesFlow(seriesId: String): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE seriesId = :seriesId ORDER BY seasonNumber ASC")
    suspend fun getSeasonsForSeries(seriesId: String): List<SeasonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeason(season: SeasonEntity)

    @Query("DELETE FROM seasons WHERE id = :id")
    suspend fun deleteSeason(id: String)

    @Query("DELETE FROM seasons WHERE seriesId = :seriesId")
    suspend fun deleteSeasonsForSeries(seriesId: String)

    @Query("SELECT COUNT(*) FROM seasons")
    suspend fun getTotalSeasonsCount(): Int
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    fun getEpisodesForSeasonFlow(seasonId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesForSeriesFlow(seriesId: String): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes ORDER BY seriesId, seasonNumber, episodeNumber")
    fun getAllEpisodesFlow(): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    suspend fun getEpisodeById(id: String): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE id = :id LIMIT 1")
    fun getEpisodeByIdFlow(id: String): Flow<EpisodeEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteEpisode(id: String)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesForSeries(seriesId: String)

    @Query("SELECT COUNT(*) FROM episodes")
    suspend fun getTotalEpisodesCount(): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE isPublished = 1")
    suspend fun getPublishedEpisodesCount(): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE isPublished = 0")
    suspend fun getUnpublishedEpisodesCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenresFlow(): Flow<List<GenreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: GenreEntity)

    @Query("DELETE FROM genres WHERE id = :id")
    suspend fun deleteGenre(id: String)

    @Query("SELECT * FROM languages ORDER BY name ASC")
    fun getAllLanguagesFlow(): Flow<List<LanguageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(language: LanguageEntity)

    @Query("DELETE FROM languages WHERE id = :id")
    suspend fun deleteLanguage(id: String)
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistForUserFlow(userId: String): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND contentId = :contentId)")
    fun isInWatchlistFlow(userId: String, contentId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND contentId = :contentId)")
    suspend fun isInWatchlist(userId: String, contentId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND contentId = :contentId")
    suspend fun deleteFromWatchlist(userId: String, contentId: String)
}

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history WHERE userId = :userId ORDER BY lastWatchedAt DESC")
    fun getHistoryForUserFlow(userId: String): Flow<List<PlaybackHistoryEntity>>

    @Query("SELECT * FROM playback_history WHERE userId = :userId AND contentId = :contentId LIMIT 1")
    suspend fun getPlaybackPosition(userId: String, contentId: String): PlaybackHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlaybackHistory(item: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE userId = :userId AND contentId = :contentId")
    suspend fun deletePlaybackHistory(userId: String, contentId: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadsForUserFlow(userId: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE userId = :userId AND contentId = :contentId LIMIT 1")
    suspend fun getDownload(userId: String, contentId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun getDownloadById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("SELECT COUNT(*) FROM downloads")
    suspend fun getTotalDownloadsCount(): Int
}
