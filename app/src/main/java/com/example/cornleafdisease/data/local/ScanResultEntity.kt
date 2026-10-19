package com.example.cornleafdisease.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "classification_results",
    foreignKeys = [
        ForeignKey(
            entity = ImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DiseaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["diseaseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("imageId"),
        Index("diseaseId")
    ]
)
data class ScanResultEntity(
    @PrimaryKey
    val id: String,
    val imageId: String,
    val diseaseId: String,
    val confidence: Float,
    val severity: String,
    val severityColorHex: Long,
    val anfisFeaturesJson: String,
    val classifiedAt: String,
    val savedAt: Long = System.currentTimeMillis()
)