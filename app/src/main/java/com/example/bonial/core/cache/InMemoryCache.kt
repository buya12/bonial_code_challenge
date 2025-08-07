package com.example.bonial.core.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InMemoryCache<T> : Cache<T> {
    private val mutex = Mutex()
    private var value: T? = null

    override suspend fun get(): T? = mutex.withLock {
        value
    }

    override suspend fun set(value: T) = mutex.withLock {
        this.value = value
    }

    override suspend fun clear() = mutex.withLock {
        value = null
    }
}
