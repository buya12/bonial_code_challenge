package com.example.bonial.brochures.domain.usecase

import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.brochures.domain.repository.BrochuresRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetBrochuresUseCaseTest {

    @RelaxedMockK
    private lateinit var mockRepository: BrochuresRepository

    private lateinit var getBrochuresUseCase: GetBrochuresUseCase

    // Companion object for test data
    companion object {
        val brochure1Km = Brochure(id = 1L, publisherName = "Publisher 1km", distance = 1.0)
        val brochure5Km = Brochure(id = 2L, publisherName = "Publisher 5km", distance = 5.0)
        val brochure10Km = Brochure(id = 3L, publisherName = "Publisher 10km", distance = 10.0)
        val brochureNullDistance = Brochure(id = 4L, publisherName = "Publisher Null Distance", distance = null)

        // Example of a brochure that would be filtered out if distance is a positive number
        val brochureFarAway = Brochure(id = 5L, publisherName = "Publisher Far Away", distance = 100.0)
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getBrochuresUseCase = GetBrochuresUseCase(mockRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `invoke with distance 0 should return all brochures including those with null distance`() = runTest {
        val allBrochures = listOf(brochure1Km, brochure5Km, brochureNullDistance, brochure10Km)
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns allBrochures

        val result = getBrochuresUseCase(distance = 0.0, forceRefresh = false)

        assertEquals(4, result.size)
        assertTrue(result.containsAll(listOf(brochure1Km, brochure5Km, brochureNullDistance, brochure10Km)))
        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test
    fun `invoke with negative distance should return all brochures`() = runTest {
        val allBrochures = listOf(brochure1Km, brochureNullDistance, brochure5Km)
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns allBrochures

        val result = getBrochuresUseCase(distance = -10.0, forceRefresh = false)

        assertEquals(3, result.size)
        assertTrue(result.containsAll(listOf(brochure1Km, brochureNullDistance, brochure5Km)))
        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test
    fun `invoke with positive distance filter returns only brochures within or at that distance`() = runTest {
        val allBrochures = listOf(
            brochure1Km,      // distance 1.0
            brochure5Km,      // distance 5.0
            brochure10Km,     // distance 10.0
            brochureNullDistance,
            brochureFarAway   // distance 100.0
        )
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns allBrochures

        val result = getBrochuresUseCase(distance = 5.0, forceRefresh = false)

        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(brochure1Km, brochure5Km)))
        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test
    fun `invoke with positive distance filter excludes brochures with null distance`() = runTest {
        val brochuresWithNull = listOf(brochureNullDistance, brochure1Km)
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns brochuresWithNull

        val result = getBrochuresUseCase(distance = 5.0, forceRefresh = false)

        assertEquals(1, result.size) // Only brochure1Km should remain
        assertTrue(result.contains(brochure1Km))
        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test
    fun `invoke when repository returns empty list should return empty list`() = runTest {
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns emptyList()

        val result = getBrochuresUseCase(distance = 5.0, forceRefresh = false)

        assertTrue(result.isEmpty())
        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test
    fun `invoke with forceRefresh true should call repository with forceRefresh true`() = runTest {
        coEvery { mockRepository.getBrochures(forceRefresh = true) } returns emptyList()

        getBrochuresUseCase(distance = 0.0, forceRefresh = true)

        coVerify { mockRepository.getBrochures(forceRefresh = true) }
    }

    @Test
    fun `invoke with forceRefresh false should call repository with forceRefresh false`() = runTest {
        coEvery { mockRepository.getBrochures(forceRefresh = false) } returns emptyList()

        getBrochuresUseCase(distance = 0.0, forceRefresh = false)

        coVerify { mockRepository.getBrochures(forceRefresh = false) }
    }

    @Test(expected = RuntimeException::class)
    fun `invoke should propagate exception from repository`() = runTest {
        coEvery { mockRepository.getBrochures(any()) } throws RuntimeException()

        getBrochuresUseCase(0.0)

        coVerify { mockRepository.getBrochures(any()) }
    }
}
