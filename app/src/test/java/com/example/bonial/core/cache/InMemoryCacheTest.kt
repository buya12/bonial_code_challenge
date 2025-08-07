package com.example.bonial.core.cache

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InMemoryCacheTest {

    private lateinit var cache: InMemoryCache<String>

    private val testValue = "testValue"
    private val anotherValue = "anotherValue"

    @Before
    fun setUp() {
        cache = InMemoryCache()
    }

    @Test
    fun `get initially returns null`() = runTest {
        val retrievedValue = cache.get()
        assertNull("Cache should be empty initially", retrievedValue)
    }

    @Test
    fun `set then get returns stored value`() = runTest {
        cache.set(testValue)
        val retrievedValue = cache.get()
        assertEquals("Retrieved value should match stored value", testValue, retrievedValue)
    }

    @Test
    fun `set overwrites existing value`() = runTest {
        cache.set(testValue)
        cache.set(anotherValue)
        val retrievedValue = cache.get()
        assertEquals("Retrieved value should be the overwritten value", anotherValue, retrievedValue)
    }

    @Test
    fun `clear removes stored value`() = runTest {
        cache.set(testValue)
        cache.clear()
        val retrievedValue = cache.get()
        assertNull("Cache should be empty after clear", retrievedValue)
    }

    @Test
    fun `clear on empty cache does nothing and get still returns null`() = runTest {
        cache.clear()
        val retrievedValue = cache.get()
        assertNull("Cache should remain empty", retrievedValue)
    }

    @Test
    fun `set value get it then clear then get returns null`() = runTest {
        cache.set(testValue)
        assertEquals("Value should be set", testValue, cache.get())

        cache.clear()
        assertNull("Value should be cleared", cache.get())
    }

    @Test
    fun `set multiple times only last value is stored`() = runTest {
        cache.set("value1")
        cache.set("value2")
        cache.set(testValue)
        assertEquals(testValue, cache.get())
    }
}
