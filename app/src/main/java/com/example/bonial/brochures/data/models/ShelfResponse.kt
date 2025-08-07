package com.example.bonial.brochures.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShelfResponse(
    @SerialName("_embedded")
    val embedded: EmbeddedContents
)

@Serializable
data class EmbeddedContents(
    val contents: List<ShelfItemDTO>
)
