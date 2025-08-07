package com.example.bonial.brochures.data.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("contentType")
sealed class ShelfItemDTO {

    @Serializable
    @SerialName("brochure")
    data class Brochure(
        val content: ShelfContentDTO
    ) : ShelfItemDTO()

    @Serializable
    @SerialName("brochurePremium")
    data class BrochurePremium(
        val content: ShelfContentDTO
    ) : ShelfItemDTO()

    @Suppress("Unused")
    @Serializable
    @SerialName("superBannerCarousel")
    data object SuperBannerCarousel : ShelfItemDTO()
}
