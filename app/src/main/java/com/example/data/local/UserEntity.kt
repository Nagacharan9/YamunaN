package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val fullName: String,
    val gender: String,
    val mobileNumber: String,
    val address: String,
    val city: String,
    val password: String,
    val avatarId: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
