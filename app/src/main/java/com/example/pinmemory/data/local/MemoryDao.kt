package com.example.pinmemory.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemoryDao {

    @Query("SELECT * FROM memories WHERE userId = :userId ORDER BY date DESC")
    fun getMemoriesByUser(userId: String): LiveData<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE userId = :userId")
    suspend fun deleteAllMemoriesByUser(userId: String)
}