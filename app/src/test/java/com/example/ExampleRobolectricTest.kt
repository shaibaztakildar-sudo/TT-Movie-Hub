package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.MovieEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.MovieRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var movieRepository: MovieRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authRepository = AuthRepository(db.userDao(), context)
        movieRepository = MovieRepository(db.movieDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun appNameIsTTMovieHub() {
        val appName = context.getString(R.string.app_name)
        assertEquals("TT Movie Hub", appName)
    }

    @Test
    fun databaseStartsEmpty() = runBlocking {
        val movies = movieRepository.publishedMovies.first()
        assertTrue("Database must start with 0 movies", movies.isEmpty())
        assertFalse("Initial state should have no admin", authRepository.hasAnyAdmin())
    }

    @Test
    fun userSignUpAndAdminCreation() = runBlocking {
        // First user signed up should become ADMIN
        val adminResult = authRepository.signUp("Admin Master", "admin@ttmoviehub.com", "admin123", makeAdmin = true)
        assertTrue(adminResult.isSuccess)
        val admin = adminResult.getOrThrow()
        assertEquals("ADMIN", admin.role)
        assertTrue(authRepository.isAdmin)

        // Second normal user signs up
        val userResult = authRepository.signUp("Regular User", "user@test.com", "user123", makeAdmin = false)
        assertTrue(userResult.isSuccess)
        val user = userResult.getOrThrow()
        assertEquals("USER", user.role)
    }

    @Test
    fun movieCrudAndPublishToggle() = runBlocking {
        val movie = MovieEntity(
            id = UUID.randomUUID().toString(),
            title = "Inception",
            posterUri = "file:///path/to/poster.jpg",
            bannerUri = "file:///path/to/banner.jpg",
            description = "A thief who steals corporate secrets.",
            genre = "Sci-Fi",
            language = "English",
            releaseYear = 2010,
            durationMinutes = 148,
            cast = "Leonardo DiCaprio",
            director = "Christopher Nolan",
            ageRating = "PG-13",
            videoUri = "file:///path/to/inception.mp4",
            isPublished = true,
            isFeatured = true
        )

        movieRepository.saveMovie(movie)
        val fetched = movieRepository.getMovieById(movie.id)
        assertNotNull(fetched)
        assertEquals("Inception", fetched?.title)

        // Toggle publish
        movieRepository.togglePublish(fetched!!)
        val unpublished = movieRepository.getMovieById(movie.id)
        assertFalse(unpublished!!.isPublished)

        // Delete movie
        movieRepository.deleteMovie(movie.id)
        val deleted = movieRepository.getMovieById(movie.id)
        assertEquals(null, deleted)
    }

    @Test
    fun adminAuthenticationAndAccessControl() = runBlocking {
        // Create initial admin
        val adminResult = authRepository.createInitialAdmin(
            name = "TT Admin",
            email = "admin@ttmoviehub.com",
            phone = "9876543210",
            password = "AdminSecretPassword@123"
        )
        assertTrue(adminResult.isSuccess)

        // Try admin login with correct credentials
        val loginSuccess = authRepository.loginAdmin("admin@ttmoviehub.com", "AdminSecretPassword@123")
        assertTrue(loginSuccess.isSuccess)
        assertEquals("ADMIN", authRepository.currentAdmin.value?.role)

        // Try admin login with wrong password
        val wrongPass = authRepository.loginAdmin("admin@ttmoviehub.com", "wrongpass")
        assertFalse(wrongPass.isSuccess)

        // Normal user signup
        val userResult = authRepository.signUp("Regular Viewer", "viewer@ttmoviehub.com", "viewerpass")
        assertTrue(userResult.isSuccess)
        assertEquals("USER", userResult.getOrThrow().role)

        // Normal user cannot login as Admin
        val normalUserAsAdmin = authRepository.loginAdmin("viewer@ttmoviehub.com", "viewerpass")
        assertFalse("Normal user must not be granted admin login", normalUserAsAdmin.isSuccess)
    }
}
