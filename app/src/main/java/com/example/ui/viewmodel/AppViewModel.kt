package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteImageEntity
import com.example.data.local.UserEntity
import com.example.data.model.PicsumImage
import com.example.data.repository.AuthorFilter
import com.example.data.repository.AuthResult
import com.example.data.repository.GalleryRepository
import com.example.data.repository.SessionManager
import com.example.data.repository.UserRepository
import com.example.util.ImageUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val galleryRepository: GalleryRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {

    // --- Session & Auth State ---
    val currentUser: StateFlow<UserEntity?> = userRepository.observeLoggedInUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = sessionManager.loggedInEmail
        .combine(currentUser) { email, user ->
            !email.isNullOrBlank() && user != null
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDarkMode: StateFlow<Boolean?> = sessionManager.isDarkMode

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<String?> = _authError.asStateFlow()

    // --- Gallery State ---
    private val _allImages = MutableStateFlow<List<PicsumImage>>(emptyList())
    val allImages: StateFlow<List<PicsumImage>> = _allImages.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _authorFilter = MutableStateFlow(AuthorFilter.ALL)
    val authorFilter: StateFlow<AuthorFilter> = _authorFilter.asStateFlow()

    private val _isLoadingFirstPage = MutableStateFlow(false)
    val isLoadingFirstPage: StateFlow<Boolean> = _isLoadingFirstPage.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _galleryError = MutableStateFlow<String?>(null)
    val galleryError: StateFlow<String?> = _galleryError.asStateFlow()

    private var currentPage = 1
    private var isLastPageReached = false
    private var searchDebounceJob: Job? = null

    // Combined filtered gallery images
    val filteredImages: StateFlow<List<PicsumImage>> = combine(
        _allImages,
        _searchQuery,
        _authorFilter
    ) { images, query, filter ->
        GalleryRepository.filterImages(images, query, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Favorites State ---
    val favoriteIds: StateFlow<Set<String>> = galleryRepository.favoriteIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val allFavorites: StateFlow<List<FavoriteImageEntity>> = galleryRepository.allFavoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _favoritesSearchQuery = MutableStateFlow("")
    val favoritesSearchQuery: StateFlow<String> = _favoritesSearchQuery.asStateFlow()

    val filteredFavorites: StateFlow<List<FavoriteImageEntity>> = combine(
        allFavorites,
        _favoritesSearchQuery
    ) { favs, query ->
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            favs
        } else {
            favs.filter { it.author.contains(trimmed, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selected Image & Full-screen viewer ---
    private val _selectedImage = MutableStateFlow<PicsumImage?>(null)
    val selectedImage: StateFlow<PicsumImage?> = _selectedImage.asStateFlow()

    private val _isFullScreenViewer = MutableStateFlow(false)
    val isFullScreenViewer: StateFlow<Boolean> = _isFullScreenViewer.asStateFlow()

    // --- Download status message ---
    private val _downloadStatusMessage = MutableStateFlow<String?>(null)
    val downloadStatusMessage: StateFlow<String?> = _downloadStatusMessage.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.seedInitialUserIfEmpty()
        }
        loadInitialImages()
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            _isAuthLoading.value = true
            when (val result = userRepository.loginUser(email, password)) {
                is AuthResult.Success -> {
                    _isAuthLoading.value = false
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                    onError(result.message)
                }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        gender: String,
        mobileNumber: String,
        address: String,
        city: String,
        password: String,
        confirmPassword: String,
        avatarId: Int = 0,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validationError = UserRepository.validateRegistration(
            fullName, email, gender, mobileNumber, address, city, password, confirmPassword
        )
        if (validationError != null) {
            _authError.value = validationError
            onError(validationError)
            return
        }

        viewModelScope.launch {
            _authError.value = null
            _isAuthLoading.value = true
            when (val result = userRepository.registerUser(
                fullName, email, gender, mobileNumber, address, city, password, avatarId
            )) {
                is AuthResult.Success -> {
                    _isAuthLoading.value = false
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _isAuthLoading.value = false
                    _authError.value = result.message
                    onError(result.message)
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        userRepository.logout()
        _authError.value = null
        onLoggedOut()
    }

    fun updateProfile(
        fullName: String,
        gender: String,
        mobileNumber: String,
        address: String,
        city: String,
        avatarId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (fullName.isBlank()) {
            onError("Full name is required")
            return
        }
        val digits = mobileNumber.filter { it.isDigit() }
        if (digits.length != 10) {
            onError("Mobile number must be exactly 10 digits")
            return
        }
        if (address.isBlank()) {
            onError("Address is required")
            return
        }
        if (city.isBlank()) {
            onError("City is required")
            return
        }

        viewModelScope.launch {
            val success = userRepository.updateProfile(fullName, gender, mobileNumber, address, city, avatarId)
            if (success) {
                onSuccess()
            } else {
                onError("Failed to update profile. User session may have expired.")
            }
        }
    }

    fun setDarkMode(isDark: Boolean?) {
        sessionManager.setDarkMode(isDark)
    }

    // --- Gallery Actions ---
    fun loadInitialImages() {
        if (_allImages.value.isNotEmpty() || _isLoadingFirstPage.value) return
        viewModelScope.launch {
            _isLoadingFirstPage.value = true
            _galleryError.value = null
            currentPage = 1
            isLastPageReached = false

            val result = galleryRepository.fetchImages(page = currentPage, limit = 30)
            result.onSuccess { images ->
                _allImages.value = images
                _isLoadingFirstPage.value = false
            }.onFailure { ex ->
                _galleryError.value = ex.localizedMessage ?: "Failed to load images. Please check network."
                _isLoadingFirstPage.value = false
            }
        }
    }

    fun refreshImages() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            _galleryError.value = null
            currentPage = 1
            isLastPageReached = false

            val result = galleryRepository.fetchImages(page = 1, limit = 30)
            result.onSuccess { images ->
                _allImages.value = images
                _isRefreshing.value = false
            }.onFailure { ex ->
                _galleryError.value = ex.localizedMessage ?: "Failed to refresh images"
                _isRefreshing.value = false
            }
        }
    }

    fun loadMoreImages() {
        if (_isLoadingMore.value || _isLoadingFirstPage.value || _isRefreshing.value || isLastPageReached) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            val result = galleryRepository.fetchImages(page = nextPage, limit = 30)
            result.onSuccess { newImages ->
                if (newImages.isEmpty()) {
                    isLastPageReached = true
                } else {
                    currentPage = nextPage
                    // Filter out duplicates if any
                    val currentSet = _allImages.value.map { it.id }.toSet()
                    val uniqueNew = newImages.filter { it.id !in currentSet }
                    _allImages.value = _allImages.value + uniqueNew
                }
                _isLoadingMore.value = false
            }.onFailure {
                _isLoadingMore.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchDebounceJob?.cancel()
        // Debounce update for smooth typing
        searchDebounceJob = viewModelScope.launch {
            delay(150)
            _searchQuery.value = query
        }
    }

    fun setAuthorFilter(filter: AuthorFilter) {
        _authorFilter.value = filter
    }

    fun setFavoritesSearchQuery(query: String) {
        _favoritesSearchQuery.value = query
    }

    fun toggleFavorite(image: PicsumImage) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(image.id)
            galleryRepository.toggleFavorite(image, isFav)
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            galleryRepository.removeFavorite(id)
        }
    }

    fun setSelectedImage(image: PicsumImage?) {
        _selectedImage.value = image
    }

    fun setFullScreenViewer(open: Boolean) {
        _isFullScreenViewer.value = open
    }

    fun clearDownloadStatus() {
        _downloadStatusMessage.value = null
    }

    fun downloadImage(context: Context, image: PicsumImage) {
        if (_isDownloading.value) return
        viewModelScope.launch {
            _isDownloading.value = true
            _downloadStatusMessage.value = "Downloading image to Gallery..."
            val result = ImageUtils.downloadImageToGallery(
                context = context,
                imageUrl = image.downloadUrl,
                author = image.author,
                imageId = image.id
            )
            _isDownloading.value = false
            result.onSuccess { message ->
                _downloadStatusMessage.value = message
            }.onFailure { ex ->
                _downloadStatusMessage.value = "Download failed: ${ex.localizedMessage}"
            }
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.getInstance(application)
                    val sessionManager = SessionManager(application)
                    val userRepository = UserRepository(db.userDao(), sessionManager)
                    val galleryRepository = GalleryRepository(db.favoriteDao())
                    return AppViewModel(application, userRepository, galleryRepository, sessionManager) as T
                }
            }
    }
}
