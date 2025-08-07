package com.example.bonial.brochures.data.repository

import com.example.bonial.brochures.data.models.ShelfContentDTO
import com.example.bonial.brochures.data.models.ShelfItemDTO
import com.example.bonial.brochures.data.source.ShelfService
import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.brochures.domain.repository.BrochuresRepository
import com.example.bonial.core.cache.Cache
import javax.inject.Inject

internal class BrochuresRepositoryImpl @Inject constructor(
    private val service: ShelfService,
    private val cache: Cache<List<Brochure>>
) : BrochuresRepository {

    override suspend fun getBrochures(forceRefresh: Boolean): List<Brochure> {
        val cached = cache.runCatching { get() }
            .getOrNull()

        if (forceRefresh || cached.isNullOrEmpty()) {
            val result = loadShelfItems()
            cache.runCatching { set(result) }

            return result
        }

        return cached
    }

    private suspend fun loadShelfItems(): List<Brochure> {
        return service.getShelf()
            .embedded
            .contents
            .mapNotNull { dto ->
                val content: ShelfContentDTO
                val premium: Boolean
                when (dto) {
                    is ShelfItemDTO.Brochure -> {
                        content = dto.content
                        premium = false
                    }
                    is ShelfItemDTO.BrochurePremium -> {
                        content = dto.content
                        premium = true
                    }
                    else -> {
                        return@mapNotNull null
                    }
                }

                Brochure(
                    id = content.id,
                    publisherName = content.publisher?.name ?: "",
                    brochureImage = content.brochureImage,
                    distance = content.distance,
                    premium = premium
                )
            }
    }
}
