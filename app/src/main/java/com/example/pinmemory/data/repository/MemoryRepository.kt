package com.example.pinmemory.data.repository

import androidx.lifecycle.LiveData
import com.example.pinmemory.data.local.MemoryDao
import com.example.pinmemory.data.local.MemoryEntity
import com.example.pinmemory.data.remote.FirestoreDataSource
import com.example.pinmemory.model.Memory

class MemoryRepository(
    private val memoryDao: MemoryDao,
    private val firestoreDataSource: FirestoreDataSource
) {

    fun getMemoriesByUser(userId: String): LiveData<List<MemoryEntity>> {
        return memoryDao.getMemoriesByUser(userId)
    }

    suspend fun getMemoryById(id: String) = memoryDao.getMemoryById(id)
    suspend fun addMemory(memory: Memory, entity: MemoryEntity) {
        memoryDao.insertMemory(entity)
        firestoreDataSource.addMemory(memory)
    }

    suspend fun updateMemory(memory: Memory, entity: MemoryEntity) {
        memoryDao.updateMemory(entity)
        firestoreDataSource.updateMemory(memory)
    }

    suspend fun deleteMemory(memoryId: String, entity: MemoryEntity) {
        memoryDao.deleteMemory(entity)
        firestoreDataSource.deleteMemory(memoryId)
    }
}