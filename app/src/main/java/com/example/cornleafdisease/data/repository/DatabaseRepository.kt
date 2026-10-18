package com.example.cornleafdisease.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.cornleafdisease.data.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface DatabaseRepository {
    suspend fun saveScanResult(result: ScanResult): Boolean
    suspend fun getScanHistory(): List<ScanResult>
    suspend fun deleteScanResult(id: String): Boolean
}

class DatabaseRepositoryImpl(private val context: Context) : DatabaseRepository {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("corn_disease_local_prefs", Context.MODE_PRIVATE)
    }
    private val keyHistory = "scan_history_json_list"
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    override suspend fun saveScanResult(result: ScanResult): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentHistory = getScanHistory().toMutableList()
            if (currentHistory.none { it.id == result.id }) {
                currentHistory.add(0, result) // Add to top of list
            }
            val jsonString = json.encodeToString(currentHistory)
            prefs.edit().putString(keyHistory, jsonString).apply()
            Log.d("DatabaseRepository", "Saved scan result locally: $result")
            true
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error saving scan result locally", e)
            false
        }
    }

    override suspend fun getScanHistory(): List<ScanResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            val jsonString = prefs.getString(keyHistory, null)
            if (!jsonString.isNullOrEmpty()) {
                json.decodeFromString<List<ScanResult>>(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching local scan history", e)
            emptyList()
        }
    }

    override suspend fun deleteScanResult(id: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val currentHistory = getScanHistory().toMutableList()
            val removed = currentHistory.removeAll { it.id == id }
            if (removed) {
                val jsonString = json.encodeToString(currentHistory)
                prefs.edit().putString(keyHistory, jsonString).apply()
                Log.d("DatabaseRepository", "Deleted local scan record with id: $id")
            }
            true
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error deleting local scan record", e)
            false
        }
    }
}
