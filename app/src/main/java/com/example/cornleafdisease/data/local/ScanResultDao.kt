package com.example.cornleafdisease.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanResultDao {
    @Query("SELECT * FROM scan_results")
    suspend fun getAllScans(): List<ScanResultEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanResultEntity)

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteScan(id: String)
}
