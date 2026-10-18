package com.example.cornleafdisease.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val diseaseName: String,
    val scientificName: String,
    val confidence: Float,
    val severity: String,
    val severityColorHex: Long,
    val imageUrl: String?,
    val recommendations: List<String>,
    val anfisFeatures: List<String>
)
