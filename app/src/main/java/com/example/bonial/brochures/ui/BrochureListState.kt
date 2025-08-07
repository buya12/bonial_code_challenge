package com.example.bonial.brochures.ui

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
data class BrochureListState(
    val brochures: List<BrochureItem> = emptyList(),
    val filters: Filters = Filters(),
    val showFilters: Boolean = false,
    val loading: Boolean = false,
    val error: Boolean = false
)

@Immutable
@Parcelize
data class Filters(
    val distance: Double = DEFAULT_DISTANCE,
    val maxDistance: Double = MAX_DISTANCE
) : Parcelable {

    val hasFilters: Boolean
        get() = distance != DEFAULT_DISTANCE

    companion object {
        private const val DEFAULT_DISTANCE = 5.0
        private const val MAX_DISTANCE = 9.0
    }
}
