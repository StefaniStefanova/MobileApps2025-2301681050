package com.example.pinmemory.model

data class Memory(
    val id: String = "",
    val title: String = "",
    val note: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val imageUrl: String = "",
    val date: Long = System.currentTimeMillis(),
    val userId: String = ""
)