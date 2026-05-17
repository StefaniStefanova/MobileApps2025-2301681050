package com.example.pinmemory.util

import com.example.pinmemory.data.local.MemoryEntity
import com.example.pinmemory.model.Memory

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = this.id,
        title = this.title,
        note = this.note,
        latitude = this.latitude,
        longitude = this.longitude,
        locationName = this.locationName,
        imageUrl = this.imageUrl,
        date = this.date,
        userId = this.userId
    )
}

fun MemoryEntity.toModel(): Memory {
    return Memory(
        id = this.id,
        title = this.title,
        note = this.note,
        latitude = this.latitude,
        longitude = this.longitude,
        locationName = this.locationName,
        imageUrl = this.imageUrl,
        date = this.date,
        userId = this.userId
    )
}