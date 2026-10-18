package com.example.cornleafdisease.data.repository

import android.content.Context
import android.util.Log
import com.example.cornleafdisease.data.local.AppDatabase
import com.example.cornleafdisease.data.local.ScanResultEntity
import com.example.cornleafdisease.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DatabaseRepository {
    suspend fun saveScanResult(result: ScanResult): Boolean
    suspend fun getScanHistory(): List<ScanResult>
    suspend fun deleteScanResult(id: String): Boolean
}

class DatabaseRepositoryImpl(private val context: Context) : DatabaseRepository {
    private fun getDao() = try {
        AppDatabase.getDatabase(context).scanResultDao()
    } catch (t: Throwable) {
        Log.e("DatabaseRepository", "Failed to get Room DAO", t)
        null
    }

    override suspend fun saveScanResult(result: ScanResult): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val dao = getDao() ?: return@withContext false
            val entity = ScanResultEntity(
                id = result.id,
                date = result.date,
                diseaseName = result.diseaseName,
                scientificName = result.scientificName,
                confidence = result.confidence,
                severity = result.severity,
                severityColorHex = result.severityColorHex,
                imageUrl = result.imageUrl,
                recommendations = result.recommendations,
                anfisFeatures = result.anfisFeatures
            )
            dao.insertScan(entity)
            Log.d("DatabaseRepository", "Saved scan result to Room DB: $result")
            true
        } catch (t: Throwable) {
            Log.e("DatabaseRepository", "Error saving scan result to Room DB", t)
            false
        }
    }

    override suspend fun getScanHistory(): List<ScanResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            val dao = getDao() ?: return@withContext emptyList()
            dao.getAllScans().map { entity ->
                ScanResult(
                    id = entity.id,
                    date = entity.date,
                    diseaseName = entity.diseaseName,
                    scientificName = entity.scientificName,
                    confidence = entity.confidence,
                    severity = entity.severity,
                    severityColorHex = entity.severityColorHex,
                    imageUrl = entity.imageUrl,
                    recommendations = entity.recommendations,
                    anfisFeatures = entity.anfisFeatures
                )
            }
        } catch (t: Throwable) {
            Log.e("DatabaseRepository", "Error fetching scan history from Room DB", t)
            emptyList()
        }
    }

    override suspend fun deleteScanResult(id: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val dao = getDao() ?: return@withContext false
            dao.deleteScan(id)
            Log.d("DatabaseRepository", "Deleted scan record from Room DB with id: $id")
            true
        } catch (t: Throwable) {
            Log.e("DatabaseRepository", "Error deleting scan record from Room DB", t)
            false
        }
    }
}
