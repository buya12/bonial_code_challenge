package com.example.bonial.core.cache

interface Cache<T> {
    suspend fun get(): T?
    suspend fun set(value: T)
    suspend fun clear()
}
