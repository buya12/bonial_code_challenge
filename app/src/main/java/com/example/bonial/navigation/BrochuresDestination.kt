package com.example.bonial.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.bonial.brochures.ui.BrochureListScreen

internal const val BROCHURES_ROUTE = "brochures"

internal fun NavGraphBuilder.brochuresDestination() {
    composable(
        route = BROCHURES_ROUTE
    ) {
        BrochureListScreen(viewModel = hiltViewModel())
    }
}
