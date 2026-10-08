package com.example.data.repository

import com.example.data.local.dao.MovieDao
import com.example.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val movieDao: MovieDao) {

    val publishedMovies: Flow<List<MovieEntity>> = movieDao.getPublishedMoviesFlow()
    val featuredMovies: Flow<List<MovieEntity>> = movieDao.getFeaturedMoviesFlow()
    val trendingMovies: Flow<List<MovieEntity>> = movieDao.getTrendingMoviesFlow()
    val allMoviesForAdmin: Flow<List<MovieEntity>> = movieDao.getAllMoviesFlow()

    fun getMovieByIdFlow(id: String): Flow<MovieEntity?> = movieDao.getMovieByIdFlow(id)

    suspend fun getMovieById(id: String): MovieEntity? = movieDao.getMovieById(id)

    suspend fun saveMovie(movie: MovieEntity) {
        movieDao.insertMovie(movie)
    }

    suspend fun updateMovie(movie: MovieEntity) {
        movieDao.updateMovie(movie)
    }

    suspend fun togglePublish(movie: MovieEntity) {
        movieDao.updateMovie(movie.copy(isPublished = !movie.isPublished))
    }

    suspend fun deleteMovie(id: String) {
        movieDao.deleteMovie(id)
    }

    suspend fun getTotalMoviesCount(): Int = movieDao.getTotalMoviesCount()
    suspend fun getPublishedCount(): Int = movieDao.getPublishedMoviesCount()
    suspend fun getUnpublishedCount(): Int = movieDao.getUnpublishedMoviesCount()
    suspend fun getRecentMovies(limit: Int = 5): List<MovieEntity> = movieDao.getRecentMovies(limit)
}
