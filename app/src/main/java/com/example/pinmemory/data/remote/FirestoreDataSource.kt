package com.example.pinmemory.data.remote

import com.example.pinmemory.model.Memory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreDataSource {

    private val db = FirebaseFirestore.getInstance()
    private val memoriesCollection = db.collection("memories")

    suspend fun addMemory(memory: Memory) {
        memoriesCollection.document(memory.id).set(memory).await()
    }

    suspend fun updateMemory(memory: Memory) {
        memoriesCollection.document(memory.id).set(memory).await()
    }

    suspend fun deleteMemory(memoryId: String) {
        memoriesCollection.document(memoryId).delete().await()
    }

    suspend fun getMemoriesByUser(userId: String): List<Memory> {
        return memoriesCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(Memory::class.java)
    }
}