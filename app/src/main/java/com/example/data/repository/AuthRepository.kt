package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tt_movie_hub_auth", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentAdmin = MutableStateFlow<UserEntity?>(null)
    val currentAdmin: StateFlow<UserEntity?> = _currentAdmin.asStateFlow()

    val allUsersFlow: Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    suspend fun initializeSession() {
        // Restore user session
        val savedUserId = prefs.getString(KEY_LOGGED_IN_USER_ID, null)
        if (savedUserId != null) {
            val user = userDao.getUserById(savedUserId)
            if (user != null && user.isActive) {
                _currentUser.value = user
            } else {
                prefs.edit().remove(KEY_LOGGED_IN_USER_ID).apply()
            }
        }

        // Restore admin session
        val savedAdminId = prefs.getString(KEY_LOGGED_IN_ADMIN_ID, null)
        if (savedAdminId != null) {
            val adminUser = userDao.getUserById(savedAdminId)
            if (adminUser != null && adminUser.role == "ADMIN" && adminUser.isActive) {
                _currentAdmin.value = adminUser
            } else {
                prefs.edit().remove(KEY_LOGGED_IN_ADMIN_ID).apply()
            }
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No account found with this email"))

        if (!user.isActive) {
            return Result.failure(Exception("Account is disabled. Please contact support."))
        }

        val hash = hashPassword(password)
        if (user.passwordHash != hash) {
            return Result.failure(Exception("Incorrect password. Please try again."))
        }

        persistSession(user)
        return Result.success(user)
    }

    suspend fun loginAdmin(identifier: String, password: String): Result<UserEntity> {
        val cleanIdentifier = identifier.trim().lowercase()
        val user = userDao.getUserByEmailOrPhone(cleanIdentifier)
            ?: return Result.failure(Exception("No administrator account found with this email or mobile"))

        if (!user.isActive) {
            return Result.failure(Exception("Administrator account is inactive or disabled."))
        }

        // Backend security check: Role MUST be ADMIN
        if (user.role != "ADMIN") {
            return Result.failure(SecurityException("Access Denied: This account does not have administrator privileges."))
        }

        val hash = hashPassword(password)
        if (user.passwordHash != hash) {
            return Result.failure(Exception("Incorrect password for administrator account."))
        }

        persistAdminSession(user)
        return Result.success(user)
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        phone: String = "",
        makeAdmin: Boolean = false
    ): Result<UserEntity> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedPhone = phone.trim()

        if (trimmedName.isEmpty()) return Result.failure(Exception("Please enter your name"))
        if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
            return Result.failure(Exception("Please enter a valid email address"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists"))
        }

        val adminCount = userDao.getAdminCount()
        val role = if (makeAdmin || adminCount == 0) "ADMIN" else "USER"

        val newUser = UserEntity(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            phone = trimmedPhone,
            passwordHash = hashPassword(password),
            role = role,
            isActive = true,
            profilePhotoUri = null
        )

        userDao.insertUser(newUser)
        if (role == "ADMIN") {
            persistAdminSession(newUser)
        }
        persistSession(newUser)
        return Result.success(newUser)
    }

    suspend fun createInitialAdmin(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<UserEntity> {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()
        val trimmedPhone = phone.trim()

        if (trimmedName.isEmpty() || trimmedEmail.isEmpty() || password.length < 6) {
            return Result.failure(Exception("Please provide a name, valid email, and password of at least 6 characters"))
        }

        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            if (existing.role == "ADMIN") {
                return Result.failure(Exception("An administrator account with this email already exists."))
            } else {
                // Elevate role to ADMIN
                val elevated = existing.copy(role = "ADMIN", phone = trimmedPhone.ifEmpty { existing.phone })
                userDao.updateUser(elevated)
                persistAdminSession(elevated)
                return Result.success(elevated)
            }
        }

        val adminUser = UserEntity(
            id = UUID.randomUUID().toString(),
            name = trimmedName,
            email = trimmedEmail,
            phone = trimmedPhone,
            passwordHash = hashPassword(password),
            role = "ADMIN",
            isActive = true,
            profilePhotoUri = null
        )

        userDao.insertUser(adminUser)
        persistAdminSession(adminUser)
        return Result.success(adminUser)
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Unit> {
        val trimmedEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(trimmedEmail)
            ?: return Result.failure(Exception("No account found with this email"))

        if (newPassword.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters"))
        }

        val updated = user.copy(passwordHash = hashPassword(newPassword))
        userDao.updateUser(updated)
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = updated
        }
        if (_currentAdmin.value?.id == user.id) {
            _currentAdmin.value = updated
        }
        return Result.success(Unit)
    }

    suspend fun updateProfile(name: String, photoUri: String?): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        val updated = user.copy(name = name.trim(), profilePhotoUri = photoUri ?: user.profilePhotoUri)
        userDao.updateUser(updated)
        _currentUser.value = updated
        if (_currentAdmin.value?.id == user.id) {
            _currentAdmin.value = updated
        }
        return Result.success(updated)
    }

    suspend fun setUserStatus(userId: String, isActive: Boolean) {
        requireAdmin()
        userDao.setUserActive(userId, isActive)
        if (_currentUser.value?.id == userId && !isActive) {
            logout()
        }
    }

    suspend fun hasAnyAdmin(): Boolean {
        return userDao.getAdminCount() > 0
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER_ID).apply()
        _currentUser.value = null
    }

    fun logoutAdmin() {
        prefs.edit().remove(KEY_LOGGED_IN_ADMIN_ID).apply()
        _currentAdmin.value = null
    }

    val isAdmin: Boolean
        get() = _currentUser.value?.role == "ADMIN" || _currentAdmin.value != null

    val isCurrentAdminLoggedIn: Boolean
        get() = _currentAdmin.value != null

    fun requireAdmin(): UserEntity {
        val admin = _currentAdmin.value ?: _currentUser.value
        if (admin == null || admin.role != "ADMIN" || !admin.isActive) {
            throw SecurityException("Access Denied: Administrator authentication required.")
        }
        return admin
    }

    private fun persistSession(user: UserEntity) {
        prefs.edit().putString(KEY_LOGGED_IN_USER_ID, user.id).apply()
        _currentUser.value = user
    }

    private fun persistAdminSession(admin: UserEntity) {
        prefs.edit().putString(KEY_LOGGED_IN_ADMIN_ID, admin.id).apply()
        _currentAdmin.value = admin
        if (_currentUser.value == null) {
            _currentUser.value = admin
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id"
        private const val KEY_LOGGED_IN_ADMIN_ID = "logged_in_admin_id"
    }
}
