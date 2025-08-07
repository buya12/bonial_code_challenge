package com.example.bonial.brochures.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.bonial.core.TestTags.BROCHURE_BROKEN_IMAGE
import com.example.bonial.core.TestTags.BROCHURE_GRID
import com.example.bonial.core.TestTags.BROCHURE_ITEM
import com.example.bonial.core.TestTags.BROCHURE_LIST_EMPTY
import com.example.bonial.core.TestTags.BROCHURE_LIST_ERROR
import com.example.bonial.core.TestTags.BROCHURE_LIST_FILTER_BUTTON
import com.example.bonial.core.TestTags.BROCHURE_LIST_LOADING
import com.example.bonial.core.TestTags.FILTERS
import com.example.bonial.core.TestTags.FILTERS_CLOSE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test

class BrochureListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testStateFlow = MutableStateFlow(BrochureListState())

    private val brochureItem1 = BrochureItem(
        id = 1L,
        publisherName = "Pub1",
        brochureImage = "img1.jpg",
        distance = 1.0,
        premium = false
    )
    private val brochureItem2 = BrochureItem(
        id = 2L,
        publisherName = "Pub2",
        brochureImage = "img2.jpg",
        distance = 2.0,
        premium = false
    )
    private val brochureItem3 = BrochureItem(
        id = 3L,
        publisherName = "Pub3",
        brochureImage = "img3.jpg",
        distance = 3.0,
        premium = false
    )
    private val brochureWithNoImage = BrochureItem(
        id = 3L,
        publisherName = "PubWithoutImage",
        brochureImage = "null",
        distance = 3.0,
        premium = false
    )
    private val testBrochureItemList = listOf(brochureItem1, brochureItem2, brochureItem3)

    // BrochureListScreen wrapper for testing
    @Composable
    private fun TestScreen() {
        val state by testStateFlow.collectAsState()
        BrochureListScreen(
            state = state,
            onFilterClicked = {
                testStateFlow.update { state -> state.copy(showFilters = true) }
            },
            onApplyFiltersClicked = { apply ->
                testStateFlow.update { state ->
                    state.copy(filters = state.filters.copy(distance = apply.distance))
                }
            },
            onResetFiltersClicked = {
                testStateFlow.update { state ->
                    state.copy(filters = Filters())
                }
            },
            onFilterDismissed = {
                testStateFlow.update { state -> state.copy(showFilters = false) }
            }
        )
    }

    @Test
    fun displays_loading_state() {
        composeTestRule.setContent { TestScreen() }

        testStateFlow.value = BrochureListState(loading = true)

        composeTestRule.onNodeWithTag(BROCHURE_LIST_LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun displays_error_state() {
        composeTestRule.setContent { TestScreen() }

        testStateFlow.value = BrochureListState(loading = false, error = true)

        composeTestRule.onNodeWithTag(BROCHURE_LIST_ERROR)
            .assertIsDisplayed()
    }

    @Test
    fun displays_empty_state() {
        composeTestRule.setContent { TestScreen() }

        testStateFlow.value = BrochureListState(brochures = emptyList())

        composeTestRule.onNodeWithTag(BROCHURE_LIST_EMPTY)
            .assertIsDisplayed()
    }

    @Test
    fun displays_content() {
        composeTestRule.setContent { TestScreen() }

        testStateFlow.value = BrochureListState(brochures = testBrochureItemList)

        composeTestRule.onNodeWithText(brochureItem1.publisherName)
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(brochureItem2.publisherName)
            .assertIsDisplayed()
    }

    @Test
    fun displays_content_without_image() {
        composeTestRule.setContent { TestScreen() }

        testStateFlow.value = BrochureListState(brochures = listOf(brochureWithNoImage))

        composeTestRule.onNode(
            hasTestTag(BROCHURE_ITEM) and hasAnyDescendant(hasText(brochureWithNoImage.publisherName))
                    and hasAnyDescendant(hasTestTag(BROCHURE_BROKEN_IMAGE))
        )
            .assertIsDisplayed()
    }

    @Test
    fun clicking_filter_button_displays_and_dismisses_filter_panel() {
        composeTestRule.setContent { TestScreen() }

        composeTestRule.onNodeWithTag(BROCHURE_LIST_FILTER_BUTTON)
            .performClick()

        composeTestRule.onNodeWithTag(FILTERS)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(FILTERS_CLOSE)
            .performClick()

        composeTestRule.onNodeWithTag(FILTERS)
            .assertDoesNotExist()
    }

    @Test
    fun displays_two_columns_in_narrow_layout() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(width = 600.dp, height = 1000.dp))
            ) {
                TestScreen()
            }
        }

        testStateFlow.value = BrochureListState(brochures = testBrochureItemList)

        composeTestRule.onNodeWithTag(BROCHURE_GRID)
            .assert(SemanticsMatcher("has 2 columns") {
                with(it.config[SemanticsProperties.CollectionInfo]) {
                    columnCount == 2
                }
            })
    }

    @Test
    fun displays_three_columns_in_wide_layout() {
        composeTestRule.setContent {
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.ForcedSize(DpSize(width = 1000.dp, height = 600.dp))
            ) {
                TestScreen()
            }
        }

        testStateFlow.value = BrochureListState(brochures = testBrochureItemList)

        composeTestRule.onNodeWithTag(BROCHURE_GRID)
            .assert(SemanticsMatcher("has 3 columns") {
                with(it.config[SemanticsProperties.CollectionInfo]) {
                    columnCount == 3
                }
            })
    }
}
