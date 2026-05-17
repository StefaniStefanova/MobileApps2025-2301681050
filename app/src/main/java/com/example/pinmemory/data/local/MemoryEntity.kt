package com.example.pinmemory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val note: String,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val imageUrl: String,
    val date: Long,
    val userId: String
)