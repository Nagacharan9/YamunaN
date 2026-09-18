package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PicsumImage

@Entity(tableName = "favorites")
data class FavoriteImageEntity(
    @PrimaryKey
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    val downloadUrl: String,
    val favoritedAt: Long = System.currentTimeMillis()
) {
    fun toPicsumImage(): PicsumImage = PicsumImage(
        id = id,
        author = author,
        width = width,
        height = height,
        url = url,
        downloadUrl = downloadUrl
    )

    companion object {
        fun fromPicsumImage(image: PicsumImage): FavoriteImageEntity = FavoriteImageEntity(
            id = image.id,
            author = image.author,
            width = image.width,
            height = image.height,
            url = image.url,
            downloadUrl = image.downloadUrl
        )
    }
}
