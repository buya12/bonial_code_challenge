package com.example.bonial.brochures.domain.repository

import com.example.bonial.brochures.domain.models.Brochure

interface BrochuresRepository {
    suspend fun getBrochures(forceRefresh: Boolean = false): List<Brochure>
}
