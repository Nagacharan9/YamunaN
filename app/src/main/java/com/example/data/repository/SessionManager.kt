package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lens_gallery_session", Context.MODE_PRIVATE)

    private val _loggedInEmail = MutableStateFlow<String?>(prefs.getString(KEY_LOGGED_IN_EMAIL, null))
    val loggedInEmail: StateFlow<String?> = _loggedInEmail.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(
        if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    )
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    fun saveSession(email: String) {
        prefs.edit().putString(KEY_LOGGED_IN_EMAIL, email).apply()
        _loggedInEmail.value = email
    }

    fun clearSession() {
        prefs.edit().remove(KEY_LOGGED_IN_EMAIL).apply()
        _loggedInEmail.value = null
    }

    fun setDarkMode(dark: Boolean?) {
        if (dark == null) {
            prefs.edit().remove(KEY_DARK_MODE).apply()
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, dark).apply()
        }
        _isDarkMode.value = dark
    }

    companion object {
        private const val KEY_LOGGED_IN_EMAIL = "logged_in_user_email"
        private const val KEY_DARK_MODE = "user_pref_dark_mode"
    }
}
