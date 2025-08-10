# Bonial Coding Challenge

This project is a solution for the Bonial coding challenge. It's an Android application designed to display a list of brochures, built with a focus on modern Android development practices and architecture.

## Implemented Features

*   **Brochure Display**: Utilizes Jetpack Compose to display a list of brochures.
*   **Data Source**: Fetches brochure data from a remote JSON endpoint.
*   **Data Caching**: Implements an in-memory caching mechanism (`InMemoryCache`) to store fetched brochure data. This helps improve performance by reducing redundant network calls and provides quicker access to previously loaded data. The `BrochuresRepositoryImpl` utilizes this cache, and a `forceRefresh` mechanism allows bypassing the cache when needed.
*   **Content Filtering**: The application ensures that only items with a `contentType` of "brochure" or "brochurePremium" are processed and displayed.
    *   This initial filtering based on `contentType` is performed within the `BrochuresRepositoryImpl`. During the data mapping process from network DTOs to domain `Brochure` models, any items not matching these specific content types are discarded.
    *   The `GetBrochuresUseCase` then receives this pre-filtered list from the repository and applies further business logic, such as distance-based filtering, operating on the already content-type-filtered data.
*   **Brochure Item Details**: Each item in the list clearly shows:
    *   The brochure image, loaded using `CoilImage` from the Skydoves/Landscapist library.
    *   **Placeholder for missing image**: If an image fails to load, the `CoilImage` composable in `BrochureItem` displays a fallback `Icon(Icons.Default.BrokenImage, ...)`.
    *   The retailer's name.
*   **Layout & Responsiveness**:
    *   **Adaptive Grid Layout**: The number of columns in the brochure grid adapts based on screen width. This is achieved in the `BrochureList` composable, which uses `rememberResponsiveColumnCount()`.
        *   `rememberResponsiveColumnCount()` leverages `currentWindowAdaptiveInfo()` from `androidx.compose.material3.adaptive.WindowAdaptiveInfo` to check the `WindowWidthSizeClass`.
        *   **Portrait/Default Mode**: For compact widths (typically portrait), it defaults to `BROCHURE_GRID_COLUM_COUNT_DEFAULT` (2 columns).
        *   **Landscape/Expanded Mode**: For expanded widths (typically landscape or larger screens), it switches to `BROCHURE_GRID_COLUM_COUNT_WIDE` (3 columns).
    *   **Premium Brochures Full Width**: "brochurePremium" content items are displayed full-width. In the `LazyVerticalGrid` within `BrochureList`, the `span` parameter is set dynamically: `span = { brochure -> GridItemSpan(if (brochure.premium) maxLineSpan else 1) }`. This makes premium items occupy all columns of the grid.
*   **Distance Filtering UI**:
    *   The `BrochureListScreen` displays a filter button that, when clicked, shows the `BrochureListFilter` composable.
    *   This component allows the user to apply filters, including one for distance (e.g., closer than 5km).
    *   The actual filtering logic based on distance is handled by the `BrochureListViewModel` when `onApplyFiltersClicked` is invoked with `BrochureApplyFilters` data. The `BrochureListState`'s `filters.hasFilters` boolean indicates if any filters are active, visually updating the filter icon with a badge.

## Automated Tests

The project includes automated tests to ensure the robustness and correctness of its components.

### Local Unit Tests

Local unit tests are implemented using **JUnit** and **MockK**. They cover the following areas:

*   **`BrochuresRepositoryImplTest.kt`**:
    *   Verifies the interaction with the `Cache` (e.g., returning cached data, fetching from the service when cache is empty/null or `forceRefresh` is true).
    *   Tests error handling, including propagation of exceptions from the `ShelfService` and handling of cache read/write exceptions.
    *   Ensures correct mapping of DTOs from the `ShelfService` to domain `Brochure` models, including filtering for "brochure" and "brochurePremium" content types, handling of unsupported item types, and missing/null publisher data.
*   **`GetBrochuresUseCaseTest.kt`**:
    *   Tests the core business logic for filtering brochures by distance, including cases for no filter (distance 0 or negative), positive distance filtering, and exclusion of items with null distance when a positive distance filter is active. It operates on data already filtered by content type by the repository.
    *   Checks handling of empty data sets from the repository.
    *   Verifies that the `forceRefresh` flag is correctly passed to the repository.
    *   Ensures exceptions from the repository are propagated.
*   **`BrochureListViewModelTest.kt`**:
    *   Covers the ViewModel's state management and event handling using `kotlinx-coroutines-test` and `Turbine` for Flow testing.
    *   Tests initial state, successful data loading, and error handling during initial load.
    *   Verifies the logic for showing and hiding the filter dialog (`onFilterClicked`, `onFilterDismissed`).
    *   Tests the application of new filters (`onApplyFiltersClicked`), including successful data reloading and error handling if the use case fails.
    *   Validates the filter reset functionality (`onResetFiltersClicked`) to ensure filters return to default and data is reloaded.
*   **`InMemoryCacheTest.kt`**:
    *   Tests the fundamental operations of the generic `InMemoryCache` implementation, such as initial state, setting and retrieving values, overwriting values, and clearing the cache.

### UI / Instrumented Tests

UI tests are conducted using Jetpack Compose testing utilities to verify the behavior of the `BrochureListScreen`.

*   **`BrochureListScreenTest.kt`** (located in `src/androidTest/java`):
    *   **State Display**: Verifies that the screen correctly displays various states:
        *   Loading state (`BROCHURE_LIST_LOADING`).
        *   Error state (`BROCHURE_LIST_ERROR`).
        *   Empty state when no brochures are available (`BROCHURE_LIST_EMPTY`).
        *   Content state with a list of brochures, including items with and without images (checking for placeholder `BROCHURE_BROKEN_IMAGE`).
    *   **Filter Interaction**:
        *   Tests that clicking the filter button (`BROCHURE_LIST_FILTER_BUTTON`) correctly shows the filter panel (`FILTERS`).
        *   Tests that dismissing the filter panel (`FILTERS_CLOSE`) hides it.
    *   **Responsive Layout**:
        *   Asserts that the brochure grid (`BROCHURE_GRID`) displays 2 columns in a narrow/portrait configuration.
        *   Asserts that the brochure grid displays 3 columns in a wide/landscape configuration, using `DeviceConfigurationOverride` for simulating different screen sizes.

## Potential Future Enhancements

While the current version fulfills the core requirements, here are some features and improvements that could be added:

*   **Advanced Error Handling**: More granular error messages and recovery options for network issues or data inconsistencies.
*   **UI/UX Polish**:
    *   The current implementation includes shimmer effects for image loading via `ShimmerPlugin` with `CoilImage`.
    *   Clear empty states are implemented for when no brochures match filters (`BrochureListEmpty`) or if the initial fetch fails (`BrochureListError`).
    *   Smooth animations for list item appearance using `animateItem()` modifier in `BrochureList`.
*   **Persistence Layer**: Replace or augment the in-memory cache with a persistent caching solution (e.g., Room database) for offline support and data retention across app sessions.
*   **Detailed Brochure View**: A dedicated screen to show more details when a user taps on a brochure.
*   **Accessibility Improvements**: Enhance accessibility features, ensuring compatibility with screen readers and other assistive technologies. Semantics for collection info are partially added to `BrochureList`.
*   **Performance Profiling**: Conduct thorough performance profiling and optimize image loading, list scrolling, and data processing.
*   **Expanded Test Coverage**: Add more unit tests and introduce integration tests for broader coverage.
*   **Dynamic Location**: For the distance filter, integrate with device location services to use the user's actual current location.
*   **Sorting & Advanced Filtering**: Allow users to sort brochures (e.g., by retailer, distance, expiry date) or apply more complex filters.
*   **User Preferences**: Allow users to save their filter preferences.
*   **Search Functionality**: The top bar includes a search input field UI, but the actual search functionality is marked as "work in progress".

## Tech Stack

*   **Kotlin**: Primary programming language.
*   **Jetpack Compose**: For building the UI declaratively.
    *   **`androidx.compose.material3.adaptive`**: For creating adaptive layouts.
*   **MVVM (Model-View-ViewModel)**: Architectural pattern.
*   **Retrofit**: For handling network requests.
*   **Kotlin Serialization**: For parsing JSON data.
*   **Hilt**: For dependency injection.
*   **Coroutines & Flow**: For managing asynchronous operations and state.
    *   **Turbine**: For testing Kotlin Flows.
*   **Skydoves Landscapist (Coil)**: For image loading with placeholder and shimmer support.
*   **JUnit & MockK**: For unit testing.

## How to Run

1.  **Clone the repository**: `git clone https://github.com/buya12/bonial_code_challenge`
2.  **Open in Android Studio**: Import the project into Android Studio (latest stable version recommended).
3.  **Run**: Build and run the application on an Android emulator or a physical device (minimum API level 24+).

## Notes

*   The application uses a mock/test backend endpoint as specified in the challenge requirements.
*   No real user data or credentials are required or handled by this application.
