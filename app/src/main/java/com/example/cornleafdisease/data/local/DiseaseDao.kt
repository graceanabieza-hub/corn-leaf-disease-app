package com.example.cornleafdisease.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DiseaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(disease: DiseaseEntity)

    @Query("SELECT * FROM diseases WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): DiseaseEntity?
}