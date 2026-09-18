package com.example.data.repository

import androidx.core.util.PatternsCompat
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class UserRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val loggedInEmail = sessionManager.loggedInEmail

    suspend fun registerUser(
        fullName: String,
        email: String,
        gender: String,
        mobileNumber: String,
        address: String,
        city: String,
        password: String,
        avatarId: Int = 0
    ): AuthResult {
        val cleanEmail = email.trim()
        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return AuthResult.Error("An account with this email address already exists.")
        }

        val user = UserEntity(
            email = cleanEmail,
            fullName = fullName.trim(),
            gender = gender.trim(),
            mobileNumber = mobileNumber.trim(),
            address = address.trim(),
            city = city.trim(),
            password = password,
            avatarId = avatarId
        )
        userDao.insertUser(user)
        sessionManager.saveSession(user.email)
        return AuthResult.Success(user)
    }

    suspend fun loginUser(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return AuthResult.Error("Account not found. Please register first.")

        if (user.password != password) {
            return AuthResult.Error("Incorrect password. Please verify and try again.")
        }

        sessionManager.saveSession(user.email)
        return AuthResult.Success(user)
    }

    suspend fun getLoggedInUser(): UserEntity? {
        val email = sessionManager.loggedInEmail.value ?: return null
        return userDao.getUserByEmail(email)
    }

    fun observeLoggedInUser(): Flow<UserEntity?> {
        val email = sessionManager.loggedInEmail.value ?: ""
        return userDao.observeUserByEmail(email)
    }

    suspend fun updateProfile(
        fullName: String,
        gender: String,
        mobileNumber: String,
        address: String,
        city: String,
        avatarId: Int
    ): Boolean {
        val current = getLoggedInUser() ?: return false
        val updated = current.copy(
            fullName = fullName.trim(),
            gender = gender.trim(),
            mobileNumber = mobileNumber.trim(),
            address = address.trim(),
            city = city.trim(),
            avatarId = avatarId
        )
        userDao.updateUser(updated)
        return true
    }

    fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Seed initial demo user if database is empty for easy evaluation
     */
    suspend fun seedInitialUserIfEmpty() {
        if (userDao.getUserCount() == 0) {
            val demoUser = UserEntity(
                email = "demo@lensgallery.com",
                fullName = "Alex Morgan",
                gender = "Female",
                mobileNumber = "9876543210",
                address = "742 Evergreen Terrace",
                city = "San Francisco",
                password = "password123",
                avatarId = 1
            )
            userDao.insertUser(demoUser)
        }
    }

    companion object {
        fun validateRegistration(
            fullName: String,
            email: String,
            gender: String,
            mobileNumber: String,
            address: String,
            city: String,
            password: String,
            confirmPassword: String
        ): String? {
            if (fullName.isBlank()) return "Full Name is required"
            if (email.isBlank()) return "Email Address is required"
            if (!PatternsCompat.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                return "Please enter a valid email address"
            }
            if (gender.isBlank()) return "Please select your gender"
            if (mobileNumber.isBlank()) return "Mobile Number is required"
            val digitsOnly = mobileNumber.filter { it.isDigit() }
            if (digitsOnly.length != 10) {
                return "Mobile number must be exactly 10 digits"
            }
            if (address.isBlank()) return "Address is required"
            if (city.isBlank()) return "Please select your city"
            if (password.length < 6) return "Password must be at least 6 characters"
            if (password != confirmPassword) return "Passwords do not match"
            return null
        }
    }
}
