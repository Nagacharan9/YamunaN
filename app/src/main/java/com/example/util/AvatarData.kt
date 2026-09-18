package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class AvatarOption(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color = Color.White
)

object AvatarData {
    val options = listOf(
        AvatarOption(0, "Photographer", Icons.Filled.CameraAlt, Color(0xFF4F46E5)),
        AvatarOption(1, "Creator", Icons.Filled.ColorLens, Color(0xFF0D9488)),
        AvatarOption(2, "Explorer", Icons.Filled.Landscape, Color(0xFFEA580C)),
        AvatarOption(3, "Star", Icons.Filled.Star, Color(0xFFEAB308)),
        AvatarOption(4, "Night Owl", Icons.Filled.Nightlight, Color(0xFF8B5CF6)),
        AvatarOption(5, "Default", Icons.Filled.Person, Color(0xFF475569))
    )

    fun getAvatar(id: Int): AvatarOption {
        return options.find { it.id == id } ?: options.first()
    }
}
