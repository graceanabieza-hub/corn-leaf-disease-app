package com.example.cornleafdisease.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanResultDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(result: ScanResultEntity): Long

    @Query(
        "SELECT * FROM classification_results " +
                "ORDER BY savedAt DESC"
    )
    suspend fun getAll(): List<ScanResultEntity>

    @Query("DELETE FROM classification_results WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query(
        "SELECT * FROM classification_results " +
                "WHERE id = :id LIMIT 1"
    )
    suspend fun getById(id: String): ScanResultEntity?
}