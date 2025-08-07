package com.example.bonial.brochures.domain.models

data class Brochure(
    val id: Long,
    val publisherName: String,
    val brochureImage: String? = null,
    val premium: Boolean = false,
    val distance: Double? = null
)
