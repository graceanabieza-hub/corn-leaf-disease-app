package com.example.cornleafdisease.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diseases")
data class DiseaseEntity(
    @PrimaryKey
    val id: String,
    val diseaseName: String,
    val scientificName: String,
    val recommendationsJson: String
)