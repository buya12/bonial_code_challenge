package com.example.bonial.brochures.domain.usecase

import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.brochures.domain.repository.BrochuresRepository
import javax.inject.Inject

class GetBrochuresUseCase @Inject constructor(
    private val repository: BrochuresRepository
) {

    suspend operator fun invoke(
        distance: Double,
        forceRefresh: Boolean = false
    ): List<Brochure> {
        return repository.getBrochures(forceRefresh = forceRefresh)
            .mapNotNull { brochure ->
                brochure.takeIf { filterBrochure(brochure, distance) }
            }
    }

    private fun filterBrochure(
        brochure: Brochure,
        distance: Double
    ): Boolean {
        return if (distance > 0.0) {
            brochure.distance != null && brochure.distance <= distance
        } else {
            true
        }
    }
}
