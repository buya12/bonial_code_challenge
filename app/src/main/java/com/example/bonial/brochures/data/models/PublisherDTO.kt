package com.example.bonial.brochures.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PublisherDTO(
    val id: String,
    val name: String? = null,
    val type: String? = null
)
