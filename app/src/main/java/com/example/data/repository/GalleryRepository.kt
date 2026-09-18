package com.example.data.repository

import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteImageEntity
import com.example.data.model.PicsumImage
import com.example.data.remote.ApiClient
import com.example.data.remote.PicsumApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AuthorFilter(val title: String) {
    ALL("All Images"),
    A_TO_M("Author A-M"),
    N_TO_Z("Author N-Z")
}

class GalleryRepository(
    private val favoriteDao: FavoriteDao,
    private val apiService: PicsumApiService = ApiClient.picsumApi
) {
    val favoriteIdsFlow: Flow<Set<String>> = favoriteDao.getAllFavoriteIds().map { it.toSet() }

    val allFavoritesFlow: Flow<List<FavoriteImageEntity>> = favoriteDao.getAllFavorites()

    suspend fun fetchImages(page: Int, limit: Int = 30): Result<List<PicsumImage>> {
        return try {
            val response = apiService.getImages(page = page, limit = limit)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(image: PicsumImage, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.deleteFavorite(image.id)
        } else {
            favoriteDao.insertFavorite(FavoriteImageEntity.fromPicsumImage(image))
        }
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavorite(id)
    }

    suspend fun isFavorite(id: String): Boolean {
        return favoriteDao.isFavoriteSync(id)
    }

    companion object {
        fun filterImages(
            images: List<PicsumImage>,
            searchQuery: String,
            filter: AuthorFilter
        ): List<PicsumImage> {
            val trimmedQuery = searchQuery.trim()
            return images.filter { image ->
                // Search query condition
                val matchesSearch = if (trimmedQuery.isEmpty()) {
                    true
                } else {
                    image.author.contains(trimmedQuery, ignoreCase = true)
                }

                // Author range filter condition
                val matchesFilter = when (filter) {
                    AuthorFilter.ALL -> true
                    AuthorFilter.A_TO_M -> {
                        val firstLetter = image.author.trim().firstOrNull()?.uppercaseChar()
                        firstLetter != null && firstLetter in 'A'..'M'
                    }
                    AuthorFilter.N_TO_Z -> {
                        val firstLetter = image.author.trim().firstOrNull()?.uppercaseChar()
                        firstLetter != null && firstLetter in 'N'..'Z'
                    }
                }

                matchesSearch && matchesFilter
            }
        }
    }
}
