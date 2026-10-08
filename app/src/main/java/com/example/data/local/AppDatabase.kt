package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.DownloadDao
import com.example.data.local.dao.EpisodeDao
import com.example.data.local.dao.MovieDao
import com.example.data.local.dao.PlaybackHistoryDao
import com.example.data.local.dao.SeasonDao
import com.example.data.local.dao.SeriesDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WatchlistDao
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

@Database(
    entities = [
        UserEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        GenreEntity::class,
        LanguageEntity::class,
        WatchlistEntity::class,
        PlaybackHistoryEntity::class,
        DownloadEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tt_movie_hub.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
