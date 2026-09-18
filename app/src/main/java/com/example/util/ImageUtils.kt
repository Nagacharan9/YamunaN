package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.model.PicsumImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {

    suspend fun downloadImageToGallery(
        context: Context,
        imageUrl: String,
        author: String,
        imageId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val input: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))

            val filename = "LensGallery_${imageId}_${System.currentTimeMillis()}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LensGallery")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                Result.success("Image saved to Gallery: $filename")
            } else {
                Result.failure(Exception("Unable to create MediaStore entry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareImage(context: Context, image: PicsumImage) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Photo by ${image.author} on LensGallery")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this photo by ${image.author} (#${image.id}):\n${image.downloadUrl}"
            )
        }
        val chooser = Intent.createChooser(shareIntent, "Share Image Link")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
