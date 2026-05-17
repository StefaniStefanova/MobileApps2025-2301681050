package com.example.pinmemory.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.pinmemory.data.local.MemoryEntity
import com.example.pinmemory.data.repository.MemoryRepository

class ListViewModel(private val repository: MemoryRepository) : ViewModel() {

    fun getMemories(userId: String): LiveData<List<MemoryEntity>> {
        return repository.getMemoriesByUser(userId)
    }
}