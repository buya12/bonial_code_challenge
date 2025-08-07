package com.example.bonial.brochures.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ShelfContentDTO(
    val id: Long,
    val brochureImage: String?,
    val publisher: PublisherDTO?,
    val distance: Double?,
)
