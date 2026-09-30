package com.example.cornleafdisease.data.repository

import android.util.Log
import com.example.cornleafdisease.data.model.ScanResult
import com.example.cornleafdisease.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from

interface DatabaseRepository {
    suspend fun saveScanResult(result: ScanResult): Boolean
    suspend fun getScanHistory(): List<ScanResult>
    suspend fun deleteScanResult(id: String): Boolean
}

class DatabaseRepositoryImpl : DatabaseRepository {
    private val tableName = "scan_results"

    override suspend fun saveScanResult(result: ScanResult): Boolean {
        return try {
            Log.d("DatabaseRepository", "Saving scan record to Supabase: $result")
            SupabaseClient.client.from(tableName).insert(result)
            true
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error saving scan record to Supabase", e)
            false
        }
    }

    override suspend fun getScanHistory(): List<ScanResult> {
        return try {
            Log.d("DatabaseRepository", "Fetching scan history from Supabase")
            SupabaseClient.client.from(tableName)
                .select()
                .decodeList<ScanResult>()
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error fetching scan history from Supabase", e)
            emptyList()
        }
    }

    override suspend fun deleteScanResult(id: String): Boolean {
        return try {
            Log.d("DatabaseRepository", "Deleting scan record from Supabase with id: $id")
            SupabaseClient.client.from(tableName).delete {
                filter {
                    eq("id", id)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("DatabaseRepository", "Error deleting scan record from Supabase", e)
            false
        }
    }
}
