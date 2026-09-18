package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PicsumImage(
    @Json(name = "id") val id: String,
    @Json(name = "author") val author: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "url") val url: String,
    @Json(name = "download_url") val downloadUrl: String
) {
    /**
     * Efficient thumbnail image URL optimized for grid viewing
     */
    val thumbnailUrl: String
        get() = "https://picsum.photos/id/$id/600/450"

    /**
     * High definition image URL for full-screen details
     */
    val fullHdUrl: String
        get() = "https://picsum.photos/id/$id/1920/1280"
}
