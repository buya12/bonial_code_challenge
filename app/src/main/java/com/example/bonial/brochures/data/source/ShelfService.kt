package com.example.bonial.brochures.data.source

import com.example.bonial.brochures.data.models.ShelfResponse
import retrofit2.http.GET

interface ShelfService {
    @GET("shelf.json")
    suspend fun getShelf(): ShelfResponse
}
