package com.example.pinmemory

import com.example.pinmemory.model.Memory
import com.example.pinmemory.util.toEntity
import org.junit.Test
import org.junit.Assert.*

class MemoryUnitTest {

    @Test
    fun memory_defaultValues_areCorrect() {
        val memory = Memory()
        assertEquals("", memory.id)
        assertEquals("", memory.title)
        assertEquals("", memory.note)
        assertEquals(0.0, memory.latitude, 0.0)
        assertEquals(0.0, memory.longitude, 0.0)
    }

    @Test
    fun memory_withValues_areCorrect() {
        val memory = Memory(
            id = "123",
            title = "Test Memory",
            note = "Test Note",
            latitude = 42.6977,
            longitude = 23.3219,
            locationName = "Sofia",
            imageUrl = "",
            date = 1000L,
            userId = "user1"
        )
        assertEquals("123", memory.id)
        assertEquals("Test Memory", memory.title)
        assertEquals("Test Note", memory.note)
        assertEquals(42.6977, memory.latitude, 0.0001)
        assertEquals(23.3219, memory.longitude, 0.0001)
        assertEquals("Sofia", memory.locationName)
        assertEquals(1000L, memory.date)
        assertEquals("user1", memory.userId)
    }

    @Test
    fun memory_toEntity_convertsCorrectly() {
        val memory = Memory(
            id = "123",
            title = "Test Memory",
            note = "Test Note",
            latitude = 42.6977,
            longitude = 23.3219,
            locationName = "Sofia",
            imageUrl = "http://example.com/image.jpg",
            date = 1000L,
            userId = "user1"
        )
        val entity = memory.toEntity()
        assertEquals(memory.id, entity.id)
        assertEquals(memory.title, entity.title)
        assertEquals(memory.note, entity.note)
        assertEquals(memory.latitude, entity.latitude, 0.0001)
        assertEquals(memory.longitude, entity.longitude, 0.0001)
        assertEquals(memory.locationName, entity.locationName)
        assertEquals(memory.imageUrl, entity.imageUrl)
        assertEquals(memory.date, entity.date)
        assertEquals(memory.userId, entity.userId)
    }

    @Test
    fun memory_title_cannotBeEmpty_validation() {
        val memory = Memory(title = "")
        assertTrue(memory.title.isEmpty())
    }

    @Test
    fun memory_coordinates_areValid() {
        val memory = Memory(latitude = 42.6977, longitude = 23.3219)
        assertTrue(memory.latitude in -90.0..90.0)
        assertTrue(memory.longitude in -180.0..180.0)
    }
}