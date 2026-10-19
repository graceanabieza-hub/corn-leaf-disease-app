package com.example.cornleafdisease.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: ImageEntity)

    @Query("SELECT * FROM images WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ImageEntity?

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query(
        "SELECT COUNT(*) FROM classification_results " +
                "WHERE imageId = :imageId"
    )
    suspend fun countReferences(imageId: String): Int
}