package com.example.cornleafdisease.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.example.cornleafdisease.data.local.AppDatabase
import com.example.cornleafdisease.data.local.DiseaseEntity
import com.example.cornleafdisease.data.local.ImageEntity
import com.example.cornleafdisease.data.local.ScanResultEntity
import com.example.cornleafdisease.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

interface DatabaseRepository {
    suspend fun saveScanResult(result: ScanResult): Boolean
    suspend fun getScanHistory(): List<ScanResult>
    suspend fun deleteScanResult(id: String): Boolean
}

class DatabaseRepositoryImpl(
    context: Context
) : DatabaseRepository {

    // Application Context
    private val appContext = context.applicationContext

    // Room Database
    private val database = AppDatabase.getDatabase(appContext)

    private val scanDao = database.scanResultDao()
    private val imageDao = database.imageDao()
    private val diseaseDao = database.diseaseDao()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // =====================================================
    // SAVE CLASSIFICATION RESULT
    // =====================================================

    override suspend fun saveScanResult(
        result: ScanResult
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            database.withTransaction {

                val diseaseId = result.diseaseName

                diseaseDao.insert(
                    DiseaseEntity(
                        id = diseaseId,
                        diseaseName = result.diseaseName,
                        scientificName = result.scientificName,
                        recommendationsJson =
                            json.encodeToString(
                                result.recommendations
                            )
                    )
                )

                imageDao.insert(
                    ImageEntity(
                        id = result.id,
                        imagePath = result.imageUrl,
                        dateCaptured = result.date
                    )
                )

                scanDao.insert(
                    ScanResultEntity(
                        id = result.id,
                        imageId = result.id,
                        diseaseId = diseaseId,
                        confidence = result.confidence,
                        severity = result.severity,
                        severityColorHex =
                            result.severityColorHex,
                        anfisFeaturesJson =
                            json.encodeToString(
                                result.anfisFeatures
                            ),
                        classifiedAt = result.date
                    )
                )
            }

            Log.d(
                "DatabaseRepository",
                "Classification saved successfully: ${result.id}"
            )

            true

        } catch (e: Exception) {
            Log.e(
                "DatabaseRepository",
                "Error saving classification",
                e
            )
            false
        }
    }

    // =====================================================
    // GET CLASSIFICATION HISTORY
    // =====================================================

    override suspend fun getScanHistory():
            List<ScanResult> = withContext(Dispatchers.IO) {

        try {
            database.withTransaction {

                scanDao.getAll().mapNotNull { scan ->

                    val image = imageDao.getById(
                        scan.imageId
                    )

                    val disease = diseaseDao.getById(
                        scan.diseaseId
                    )

                    if (image == null || disease == null) {
                        null
                    } else {
                        ScanResult(
                            id = scan.id,
                            date = scan.classifiedAt,
                            diseaseName = disease.diseaseName,
                            scientificName =
                                disease.scientificName,
                            confidence = scan.confidence,
                            severity = scan.severity,
                            severityColorHex =
                                scan.severityColorHex,
                            imageUrl = image.imagePath,
                            recommendations =
                                json.decodeFromString<List<String>>(
                                    disease.recommendationsJson
                                ),
                            anfisFeatures =
                                json.decodeFromString<List<String>>(
                                    scan.anfisFeaturesJson
                                )
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(
                "DatabaseRepository",
                "Error loading classification history",
                e
            )
            emptyList()
        }
    }

    // =====================================================
    // DELETE CLASSIFICATION RESULT
    // =====================================================

    override suspend fun deleteScanResult(
        id: String
    ): Boolean = withContext(Dispatchers.IO) {

        try {
            var imagePath: String? = null

            val deleted = database.withTransaction {

                val scan = scanDao.getById(id)
                    ?: return@withTransaction false

                val image = imageDao.getById(
                    scan.imageId
                )

                val removed = scanDao.deleteById(id) > 0

                if (
                    removed &&
                    imageDao.countReferences(scan.imageId) == 0
                ) {
                    imageDao.deleteById(scan.imageId)
                    imagePath = image?.imagePath
                }

                removed
            }

            if (deleted && imagePath != null) {
                deleteOwnedImageFile(imagePath)
            }

            if (deleted) {
                Log.d(
                    "DatabaseRepository",
                    "Classification deleted: $id"
                )
            }

            deleted

        } catch (e: Exception) {
            Log.e(
                "DatabaseRepository",
                "Error deleting classification",
                e
            )
            false
        }
    }

    // =====================================================
    // DELETE APP-OWNED IMAGE FILE
    // =====================================================

    private fun deleteOwnedImageFile(imagePath: String?) {

        if (imagePath.isNullOrBlank()) return

        try {
            val uri = Uri.parse(imagePath)

            // Do not delete original gallery images.
            if (uri.scheme == "content") return

            val file = when (uri.scheme) {

                "file" -> {
                    File(uri.path ?: return)
                }

                null -> {
                    File(imagePath)
                }

                else -> return
            }

            // App's private scan image directory
            val ownedDirectory = File(
                appContext.filesDir,
                "scan_images"
            ).canonicalFile

            val target = file.canonicalFile

            // Delete only files inside scan_images
            if (target.parentFile == ownedDirectory) {

                if (target.exists()) {

                    val deleted = target.delete()

                    if (deleted) {
                        Log.d(
                            "DatabaseRepository",
                            "Image deleted successfully"
                        )
                    } else {
                        Log.w(
                            "DatabaseRepository",
                            "Unable to delete image file"
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(
                "DatabaseRepository",
                "Error deleting stored image",
                e
            )
        }
    }
}