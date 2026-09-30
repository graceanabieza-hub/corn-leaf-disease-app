package com.example.cornleafdisease.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanResult(
    val id: String,
    val date: String,
    @SerialName("disease_name")
    val diseaseName: String,
    @SerialName("scientific_name")
    val scientificName: String,
    val confidence: Float,
    val severity: String,
    @SerialName("severity_color_hex")
    val severityColorHex: Long,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val recommendations: List<String>,
    @SerialName("anfis_features")
    val anfisFeatures: List<String>
)
