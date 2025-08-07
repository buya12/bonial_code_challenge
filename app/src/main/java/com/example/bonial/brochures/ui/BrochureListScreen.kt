package com.example.bonial.brochures.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import coil.ImageLoader
import com.example.bonial.R
import com.example.bonial.brochures.ui.components.BrochureListFilter
import com.example.bonial.brochures.ui.components.rememberBrochureListImageLoader
import com.example.bonial.core.TestTags.BROCHURE_BROKEN_IMAGE
import com.example.bonial.core.TestTags.BROCHURE_GRID
import com.example.bonial.core.TestTags.BROCHURE_ITEM
import com.example.bonial.core.TestTags.BROCHURE_LIST_EMPTY
import com.example.bonial.core.TestTags.BROCHURE_LIST_ERROR
import com.example.bonial.core.TestTags.BROCHURE_LIST_FILTER_BUTTON
import com.example.bonial.core.TestTags.BROCHURE_LIST_LOADING
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage
import com.skydoves.landscapist.coil.LocalCoilImageLoader
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import kotlinx.coroutines.launch

private const val IMAGE_ASPECT_RATIO = .7f

private const val BROCHURE_GRID_COLUM_COUNT_DEFAULT = 2
private const val BROCHURE_GRID_COLUM_COUNT_WIDE = 3

@Composable
fun BrochureListScreen(
    viewModel: BrochureListViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BrochureListScreen(
        state = state,
        onFilterClicked = viewModel::onFilterClicked,
        onApplyFiltersClicked = viewModel::onApplyFiltersClicked,
        onResetFiltersClicked = viewModel::onResetFiltersClicked,
        onFilterDismissed = viewModel::onFilterDismissed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BrochureListScreen(
    state: BrochureListState,
    onFilterClicked: () -> Unit,
    onApplyFiltersClicked: (BrochureApplyFilters) -> Unit,
    onResetFiltersClicked: () -> Unit,
    onFilterDismissed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            BrochureListTopBar(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = TopAppBarDefaults.MediumAppBarCollapsedHeight)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                onFilterClicked = onFilterClicked,
                onInputClicked = {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.feature_work_in_progress)
                        )
                    }
                },
                hasFilters = state.filters.hasFilters
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        when {
            state.loading -> BrochureListLoading(
                modifier = Modifier
                    .testTag(BROCHURE_LIST_LOADING)
                    .fillMaxSize()
                    .padding(innerPadding)
            )
            state.error -> BrochureListError(
                modifier = Modifier
                    .testTag(BROCHURE_LIST_ERROR)
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            else -> BrochureContent(
                brochures = state.brochures,
                innerPadding = innerPadding
            )
        }

        if (state.showFilters) {
            BrochureListFilter(
                filters = state.filters,
                onApplyFiltersClicked = onApplyFiltersClicked,
                onResetFiltersClicked = onResetFiltersClicked,
                onDismissRequest = onFilterDismissed,
                coroutineScope = coroutineScope
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrochureListTopBar(
    onFilterClicked: () -> Unit,
    onInputClicked: () -> Unit,
    modifier: Modifier = Modifier,
    hasFilters: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = CircleShape
                )
                .clip(shape = CircleShape)
                .clickable(onClick = onInputClicked)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            text = stringResource(R.string.brochure_list_search_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            modifier = Modifier
                .testTag(BROCHURE_LIST_FILTER_BUTTON)
                .padding(end = 10.dp),
            onClick = onFilterClicked
        ) {
            Box {
                if (hasFilters) {
                    Badge(modifier = Modifier.align(Alignment.TopEnd))
                }
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun BrochureContent(
    brochures: List<BrochureItem>,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    when {
        brochures.isEmpty() -> BrochureListEmpty(
            modifier = modifier
                .testTag(BROCHURE_LIST_EMPTY)
                .fillMaxSize()
                .padding(innerPadding)
        )
        else -> BrochureList(
            modifier = modifier,
            brochures = brochures,
            contentPadding = PaddingValues(
                start = 10.dp + innerPadding.calculateLeftPadding(LocalLayoutDirection.current),
                top = 10.dp + innerPadding.calculateTopPadding(),
                end = 10.dp + innerPadding.calculateRightPadding(LocalLayoutDirection.current),
                bottom = 10.dp + innerPadding.calculateBottomPadding()
            )
        )
    }
}

@Composable
private fun BrochureList(
    brochures: List<BrochureItem>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    imageLoader: ImageLoader = rememberBrochureListImageLoader(),
    columnCount: Int = rememberResponsiveColumnCount()
) {
    LazyVerticalGrid(
        modifier = modifier
            .testTag(BROCHURE_GRID)
            .semantics {
                collectionInfo = CollectionInfo(rowCount = -1, columnCount = columnCount)
            },
        state = rememberLazyGridState(),
        columns = GridCells.Fixed(columnCount),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = contentPadding
    ) {
        items(
            items = brochures,
            key = { brochure -> brochure.id },
            span = { brochure ->
                GridItemSpan(if (brochure.premium) maxLineSpan else 1)
            },
        ) { brochure ->
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                LocalCoilImageLoader provides imageLoader
            ) {
                BrochureItem(
                    modifier = Modifier
                        .testTag(BROCHURE_ITEM)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .animateItem(),
                    brochure = brochure,
                )
            }
        }
    }
}

@Composable
private fun rememberResponsiveColumnCount(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
): Int {
    return remember(windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass) {
        when (windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.EXPANDED -> BROCHURE_GRID_COLUM_COUNT_WIDE
            else -> BROCHURE_GRID_COLUM_COUNT_DEFAULT
        }
    }
}

@Composable
private fun BrochureItem(
    brochure: BrochureItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            text = brochure.publisherName,
            style = MaterialTheme.typography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        CoilImage(
            modifier = Modifier
                .aspectRatio(IMAGE_ASPECT_RATIO),
            imageModel = { brochure.brochureImage },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Fit,
                alignment = Alignment.Center
            ),
            component = rememberImageComponent {
                +ShimmerPlugin(
                    Shimmer.Fade(
                        baseColor = MaterialTheme.colorScheme.surfaceVariant,
                        highlightColor = MaterialTheme.colorScheme.surfaceBright
                    ),
                )
            },
            failure = {
                Icon(
                    modifier = Modifier
                        .testTag(BROCHURE_BROKEN_IMAGE)
                        .requiredSize(48.dp),
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null
                )
            }
        )

        if (brochure.distance != null) {
            Text(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                text = stringResource(R.string.brochure_list_distance, brochure.distance),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun BrochureListLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BrochureListEmpty(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.brochure_list_no_data))
    }
}

@Composable
private fun BrochureListError(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.error_default))
    }
}
