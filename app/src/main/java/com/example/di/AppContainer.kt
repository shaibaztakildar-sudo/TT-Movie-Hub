package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.MediaStorageManager
import com.example.data.repository.MovieRepository
import com.example.data.repository.SeriesRepository
import com.example.data.repository.UserDataRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val mediaStorageManager: MediaStorageManager = MediaStorageManager(context)

    val authRepository: AuthRepository = AuthRepository(database.userDao(), context)
    val movieRepository: MovieRepository = MovieRepository(database.movieDao())
    val seriesRepository: SeriesRepository = SeriesRepository(
        database.seriesDao(),
        database.seasonDao(),
        database.episodeDao()
    )
    val userDataRepository: UserDataRepository = UserDataRepository(
        database.watchlistDao(),
        database.playbackHistoryDao(),
        database.downloadDao(),
        database.categoryDao(),
        database.movieDao(),
        database.seriesDao(),
        mediaStorageManager
    )

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                val instance = AppContainer(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
