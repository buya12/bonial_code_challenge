package com.example.bonial.brochures.ui

import app.cash.turbine.test
import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.brochures.domain.usecase.GetBrochuresUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

@ExperimentalCoroutinesApi
class MainCoroutineRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class BrochureListViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @RelaxedMockK
    private lateinit var getBrochuresUseCase: GetBrochuresUseCase

    private lateinit var viewModel: BrochureListViewModel

    // Test Data
    private val defaultFilters = Filters()
    private val testFilters = Filters(distance = 10.0)
    private val applyFilters = BrochureApplyFilters(distance = 15.0)

    private val brochure1 = Brochure(
        id = 1L,
        publisherName = "Pub1",
        brochureImage = "img1.jpg",
        distance = 1.0,
        premium = false
    )
    private val brochure2 = Brochure(
        id = 2L,
        publisherName = "Pub2",
        brochureImage = "img2.jpg",
        distance = 2.0,
        premium = true
    )
    private val testBrochureList = listOf(brochure1, brochure2)

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
        premium = true
    )
    private val testBrochureItemList = listOf(brochureItem1, brochureItem2)

    private val defaultInitialUiState = BrochureListState(
        brochures = emptyList(),
        loading = false,
        error = false,
        filters = defaultFilters,
        showFilters = false
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initialization loads brochures successfully with default filters`() = runTest {
        coEvery { getBrochuresUseCase.invoke(defaultFilters.distance, false) } returns testBrochureList
        viewModel = BrochureListViewModel(getBrochuresUseCase)

        viewModel.state.test {
            assertEquals(
                defaultInitialUiState.copy(
                    loading = false,
                    brochures = testBrochureItemList,
                    filters = defaultFilters
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        coVerify(exactly = 1) { getBrochuresUseCase.invoke(defaultFilters.distance, false) }
    }

    @Test
    fun `initialization handles error when loading brochures`() = runTest {
        val error = IOException("Network error")
        coEvery { getBrochuresUseCase.invoke(defaultFilters.distance, false) } throws error
        viewModel = BrochureListViewModel(getBrochuresUseCase)

        viewModel.state.test {
            assertEquals(defaultInitialUiState.copy(error = true), awaitItem())
            ensureAllEventsConsumed()
        }
        coVerify(exactly = 1) { getBrochuresUseCase.invoke(defaultFilters.distance, false) }
    }

    @Test
    fun `onFilterClicked sets showFilters to true`() = runTest {
        viewModel = BrochureListViewModel(getBrochuresUseCase)
        viewModel.state.test {
            awaitItem()

            viewModel.onFilterClicked()
            assertEquals(defaultInitialUiState.copy(showFilters = true), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onFilterDismissed sets showFilters to false`() = runTest {
        viewModel = BrochureListViewModel(getBrochuresUseCase)
        viewModel.state.test {
            awaitItem()

            viewModel.onFilterClicked()
            awaitItem()

            viewModel.onFilterDismissed()
            assertEquals(defaultInitialUiState.copy(showFilters = false), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `onApplyFiltersClicked updates filters, hides filters, and reloads brochures`() = runTest {
        coEvery { getBrochuresUseCase.invoke(defaultFilters.distance, false) } returns emptyList()
        coEvery { getBrochuresUseCase.invoke(applyFilters.distance, false) } returns testBrochureList
        viewModel = BrochureListViewModel(getBrochuresUseCase)

        viewModel.state.test {
            awaitItem() // initial loading
            viewModel.onFilterClicked()
            awaitItem() // showFilters = true
            viewModel.onApplyFiltersClicked(applyFilters)
            awaitItem() // new filters
            viewModel.onFilterDismissed()

            assertEquals(
                defaultInitialUiState.copy(
                    brochures = testBrochureItemList,
                    filters = testFilters.copy(distance = applyFilters.distance),
                    showFilters = false,
                    loading = false
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        coVerify(exactly = 1) { getBrochuresUseCase.invoke(defaultFilters.distance, false) }
        coVerify(exactly = 1) { getBrochuresUseCase.invoke(applyFilters.distance, false) }
    }

    @Test
    fun `onApplyFiltersClicked when use case fails for new filters shows error`() = runTest {
        coEvery { getBrochuresUseCase.invoke(defaultFilters.distance, false) } returns emptyList()
        val applyError = IOException("Failed to load with new filters")
        coEvery { getBrochuresUseCase.invoke(applyFilters.distance, false) } throws applyError
        viewModel = BrochureListViewModel(getBrochuresUseCase)

        viewModel.state.test {
            awaitItem() // initial loading
            viewModel.onFilterClicked()
            awaitItem() // showFilters = true
            viewModel.onApplyFiltersClicked(applyFilters)
            awaitItem() // new filters
            viewModel.onFilterDismissed()

            assertEquals(
                defaultInitialUiState.copy(
                    loading = false,
                    error = true,
                    filters = testFilters.copy(distance = applyFilters.distance),
                    showFilters = false,
                    brochures = emptyList()
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }


    @Test
    fun `onResetFiltersClicked resets filters to default, and reloads brochures`() =
        runTest {
            coEvery { getBrochuresUseCase.invoke(testFilters.distance, false) } returns testBrochureList
            coEvery { getBrochuresUseCase.invoke(defaultFilters.distance, false) } returns emptyList()
            viewModel = BrochureListViewModel(getBrochuresUseCase)

            viewModel.state.test {
                awaitItem()
                viewModel.onApplyFiltersClicked(BrochureApplyFilters(distance = testFilters.distance))
                val initialStateWithTestFilters = awaitItem()
                assertEquals(testFilters, initialStateWithTestFilters.filters)
                assertEquals(testBrochureItemList, initialStateWithTestFilters.brochures)

                viewModel.onFilterClicked()
                awaitItem() // showFilters = true
                viewModel.onResetFiltersClicked()
                awaitItem() // new filters
                viewModel.onFilterDismissed()

                assertEquals(
                    defaultInitialUiState.copy(
                        loading = false,
                        brochures = emptyList(),
                        filters = defaultFilters,
                        showFilters = false
                    ), awaitItem()
                )
                ensureAllEventsConsumed()
            }
            coVerify(exactly = 1) { getBrochuresUseCase.invoke(testFilters.distance, false) }
            coVerify(exactly = 2) { getBrochuresUseCase.invoke(defaultFilters.distance, false) }
        }
}
