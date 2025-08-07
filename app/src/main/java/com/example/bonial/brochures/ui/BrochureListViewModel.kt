package com.example.bonial.brochures.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bonial.brochures.domain.usecase.GetBrochuresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BrochureListViewModel @Inject constructor(
    private val getBrochures: GetBrochuresUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BrochureListState(loading = true))
    val state: StateFlow<BrochureListState> = _state.asStateFlow()

    init {
        subscribeFiltersUpdate()
    }

    private fun subscribeFiltersUpdate() {
        viewModelScope.launch {
            _state.map { it.filters }
                .distinctUntilChanged()
                .mapLatest { filters ->
                    getBrochures(
                        distance = filters.distance,
                        forceRefresh = false
                    )
                }
                .catch { throwable ->
                    _state.update { state ->
                        state.copy(loading = false, error = state.brochures.isEmpty())
                    }
                }
                .collect { brochures ->
                    _state.update { state ->
                        state.copy(
                            brochures = brochures.map { brochure -> brochure.toUiModel() },
                            loading = false,
                            error = false
                        )
                    }
                }
        }
    }

    fun onFilterClicked() {
        _state.update { state -> state.copy(showFilters = true) }
    }

    fun onApplyFiltersClicked(filters: BrochureApplyFilters) {
        _state.update { state ->
            state.copy(filters = state.filters.copy(distance = filters.distance))
        }
    }

    fun onResetFiltersClicked() {
        _state.update { state -> state.copy(filters = Filters()) }
    }

    fun onFilterDismissed() {
        _state.update { state -> state.copy(showFilters = false) }
    }
}
