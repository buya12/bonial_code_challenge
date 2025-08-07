package com.example.bonial.brochures.ui

import androidx.compose.runtime.Immutable
import com.example.bonial.brochures.domain.models.Brochure

@Immutable
data class BrochureItem(
    val id: Long,
    val publisherName: String,
    val brochureImage: String? = null,
    val premium: Boolean = false,
    val distance: Double? = null
)

internal fun Brochure.toUiModel() = BrochureItem(
    id = id,
    publisherName = publisherName,
    brochureImage = brochureImage,
    premium = premium,
    distance = distance
)
