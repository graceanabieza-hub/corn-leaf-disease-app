package com.example.cornleafdisease.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey
    val id: String,
    val imagePath: String?,
    val dateCaptured: String
)