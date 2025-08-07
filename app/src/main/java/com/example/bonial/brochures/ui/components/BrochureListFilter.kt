package com.example.bonial.brochures.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bonial.R
import com.example.bonial.brochures.ui.BrochureApplyFilters
import com.example.bonial.brochures.ui.Filters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrochureListFilter(
    filters: Filters,
    onApplyFiltersClicked: (BrochureApplyFilters) -> Unit,
    onResetFiltersClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    ModalBottomSheet(
        modifier = modifier.testTag("filters"),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        BrochureListFilterContent(
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            ),
            filters = filters,
            sheetState = sheetState,
            onApplyFiltersClicked = onApplyFiltersClicked,
            onResetFiltersClicked = onResetFiltersClicked,
            onDismissRequest = onDismissRequest,
            coroutineScope = coroutineScope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrochureListFilterContent(
    filters: Filters,
    sheetState: SheetState,
    onApplyFiltersClicked: (BrochureApplyFilters) -> Unit,
    onResetFiltersClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val hideBottomSheet: () -> Unit = {
        coroutineScope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.filters_title),
                style = MaterialTheme.typography.titleLarge
            )

            TextButton(
                modifier = Modifier.testTag("close_filters"),
                onClick = hideBottomSheet
            ) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }

        Spacer(Modifier.height(10.dp))

        val range = 1f..filters.maxDistance.toFloat()
        val sliderState = remember {
            SliderState(
                value = filters.distance.toFloat(),
                valueRange = range,
                steps = (range.endInclusive - range.start).toInt() - 1
            )
        }

        DistanceFilter(sliderState = sliderState)

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                modifier = Modifier.testTag("reset_filters"),
                onClick = {
                    onResetFiltersClicked()
                    hideBottomSheet()
                }) {
                Text(text = stringResource(R.string.action_reset))
            }

            Spacer(Modifier.width(10.dp))

            Button(
                modifier = Modifier.testTag("apply_filters"),
                onClick = {
                    onApplyFiltersClicked(BrochureApplyFilters(distance = sliderState.value.toDouble()))
                    hideBottomSheet()

                }) {
                Text(text = stringResource(R.string.action_apply))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DistanceFilter(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.filters_distance, sliderState.value),
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(4.dp))
        Slider(state = sliderState)
    }
}
