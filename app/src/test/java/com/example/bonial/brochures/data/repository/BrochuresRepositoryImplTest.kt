package com.example.bonial.brochures.data.repository

import com.example.bonial.brochures.data.models.EmbeddedContents
import com.example.bonial.brochures.data.models.PublisherDTO
import com.example.bonial.brochures.data.models.ShelfContentDTO
import com.example.bonial.brochures.data.models.ShelfItemDTO
import com.example.bonial.brochures.data.models.ShelfResponse
import com.example.bonial.brochures.data.source.ShelfService
import com.example.bonial.brochures.domain.models.Brochure
import com.example.bonial.core.cache.Cache
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class BrochuresRepositoryImplTest {

    @RelaxedMockK
    private lateinit var mockService: ShelfService

    @RelaxedMockK
    private lateinit var mockCache: Cache<List<Brochure>>

    private lateinit var repository: BrochuresRepositoryImpl

    private val brochure1 = Brochure(
        id = 1L,
        publisherName = "Publisher One",
        brochureImage = "image1.jpg",
        distance = 1.0,
        premium = false
    )
    private val brochure2Premium =
        Brochure(id = 2L, publisherName = "Publisher Two", brochureImage = "image2.jpg", distance = 2.5, premium = true)
    private val brochure3NoPublisher =
        Brochure(id = 3L, publisherName = "", brochureImage = "image3.jpg", distance = 3.0, premium = false)

    private val cachedBrochures = listOf(brochure1, brochure2Premium)

    private val contentDto1 = ShelfContentDTO(
        id = 1L,
        publisher = PublisherDTO(id = "id1", name = "Publisher One"),
        brochureImage = "image1.jpg",
        distance = 1.0
    )
    private val contentDto2Premium = ShelfContentDTO(
        id = 2L,
        publisher = PublisherDTO(id = "id2", name = "Publisher Two"),
        brochureImage = "image2.jpg",
        distance = 2.5
    )
    private val contentDto3NoPublisher =
        ShelfContentDTO(id = 3L, publisher = null, brochureImage = "image3.jpg", distance = 3.0)
    private val contentDto4NullPublisherName = ShelfContentDTO(
        id = 4L,
        publisher = PublisherDTO(id = "id4", name = null),
        brochureImage = "image4.jpg",
        distance = 4.0
    )

    private val brochureItemDto1 = ShelfItemDTO.Brochure(content = contentDto1)
    private val brochurePremiumItemDto2 = ShelfItemDTO.BrochurePremium(content = contentDto2Premium)
    private val brochureItemDto3NoPublisher = ShelfItemDTO.Brochure(content = contentDto3NoPublisher)
    private val brochureItemDto4NullPublisherName = ShelfItemDTO.Brochure(content = contentDto4NullPublisherName)
    private val otherItemDto = ShelfItemDTO.SuperBannerCarousel

    private val shelfResponseWithBrochures = ShelfResponse(
        embedded = EmbeddedContents(
            contents = listOf(brochureItemDto1, brochurePremiumItemDto2, otherItemDto, brochureItemDto3NoPublisher)
        )
    )
    private val shelfResponseEmptyContents = ShelfResponse(embedded = EmbeddedContents(contents = emptyList()))
    private val shelfResponseOnlyOtherItems =
        ShelfResponse(embedded = EmbeddedContents(contents = listOf(otherItemDto)))


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = BrochuresRepositoryImpl(mockService, mockCache)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getBrochures when cache populated and not force refresh returns cached data`() = runTest {
        coEvery { mockCache.get() } returns cachedBrochures

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(cachedBrochures, result)
        coVerify(exactly = 1) { mockCache.get() }
        coVerify(exactly = 0) { mockService.getShelf() }
    }

    @Test
    fun `getBrochures when cache empty and not force refresh fetches from service updates cache`() = runTest {
        coEvery { mockCache.get() } returns emptyList()
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(expectedBrochures, result)
        coVerifyOrder {
            mockCache.get()
            mockService.getShelf()
            mockCache.set(expectedBrochures)
        }
    }

    @Test
    fun `getBrochures when cache null and not force refresh fetches from service updates cache`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(expectedBrochures, result)
        coVerifyOrder {
            mockCache.get()
            mockService.getShelf()
            mockCache.set(expectedBrochures)
        }
    }

    @Test
    fun `getBrochures when force refresh true with populated cache fetches from service updates cache`() = runTest {
        coEvery { mockCache.get() } returns listOf(
            Brochure(
                id = 99L,
                publisherName = "Old Data",
                brochureImage = "old.jpg",
                distance = 0.0,
                premium = false
            )
        )
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)

        val result = repository.getBrochures(forceRefresh = true)

        assertEquals(expectedBrochures, result)
        coVerifyOrder {
            mockCache.get()
            mockService.getShelf()
            mockCache.set(expectedBrochures)
        }
    }

    @Test
    fun `getBrochures when force refresh true with empty cache fetches from service updates cache`() = runTest {
        coEvery { mockCache.get() } returns emptyList()
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)

        val result = repository.getBrochures(forceRefresh = true)

        assertEquals(expectedBrochures, result)
        coVerifyOrder {
            mockCache.get()
            mockService.getShelf()
            mockCache.set(expectedBrochures)
        }
    }


    @Test
    fun `getBrochures when service throws IOException propagates exception and not update cache`() = runTest {
        coEvery { mockCache.get() } returns null
        val networkException = IOException("Network error")
        coEvery { mockService.getShelf() } throws networkException

        var caughtException: Exception? = null
        try {
            repository.getBrochures(forceRefresh = false)
        } catch (e: IOException) {
            caughtException = e
        }

        assertNotNull("IOException was expected", caughtException)
        assertEquals(networkException, caughtException)
        coVerify(exactly = 0) { mockCache.set(any()) }
    }

    @Test
    fun `getBrochures when cache get throws IOException attempts to fetch from service`() = runTest {
        val cacheException = IOException("Cache read error")
        coEvery { mockCache.get() } throws cacheException
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)


        val result = repository.getBrochures(forceRefresh = false)


        assertEquals(expectedBrochures, result)
        coVerify { mockService.getShelf() }
        coVerify { mockCache.set(expectedBrochures) }
    }

    @Test
    fun `getBrochures when cache set throws IOException still returns fetched data`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns shelfResponseWithBrochures
        val cacheSetException = IOException("Cache write error")
        coEvery { mockCache.set(any()) } throws cacheSetException
        val expectedBrochures = listOf(brochure1, brochure2Premium, brochure3NoPublisher)

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(expectedBrochures, result)
    }

    @Test
    fun `loadShelfItems maps regular and premium brochures correctly`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns ShelfResponse(
            embedded = EmbeddedContents(contents = listOf(brochureItemDto1, brochurePremiumItemDto2))
        )

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(2, result.size)
        assertTrue("Contains brochure1", result.any { it.id == brochure1.id && !it.premium })
        assertTrue("Contains brochure2Premium", result.any { it.id == brochure2Premium.id && it.premium })
    }

    @Test
    fun `loadShelfItems skips unsupported item types`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns ShelfResponse(
            embedded = EmbeddedContents(
                contents = listOf(
                    otherItemDto,
                    brochureItemDto1,
                    otherItemDto,
                    brochurePremiumItemDto2
                )
            )
        )
        val expectedBrochures = listOf(brochure1, brochure2Premium)

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(expectedBrochures.size, result.size)
        assertTrue(result.containsAll(expectedBrochures))
    }

    @Test
    fun `loadShelfItems handles missing publisher in DTO gracefully`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns ShelfResponse(
            embedded = EmbeddedContents(contents = listOf(brochureItemDto3NoPublisher))
        )

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(1, result.size)
        assertEquals(brochure3NoPublisher.id, result[0].id)
        assertEquals("", result[0].publisherName)
    }

    @Test
    fun `loadShelfItems handles null publisher name in DTO gracefully`() = runTest {
        coEvery { mockCache.get() } returns null
        val expectedBrochureWithEmptyPublisher =
            Brochure(id = 4L, publisherName = "", brochureImage = "image4.jpg", distance = 4.0, premium = false)
        coEvery { mockService.getShelf() } returns ShelfResponse(
            embedded = EmbeddedContents(contents = listOf(brochureItemDto4NullPublisherName))
        )

        val result = repository.getBrochures(forceRefresh = false)

        assertEquals(1, result.size)
        assertEquals(expectedBrochureWithEmptyPublisher.id, result[0].id)
        assertEquals("", result[0].publisherName)
        assertEquals(expectedBrochureWithEmptyPublisher.brochureImage, result[0].brochureImage)
    }

    @Test
    fun `loadShelfItems when service returns empty contents list returns empty list`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns shelfResponseEmptyContents

        val result = repository.getBrochures(forceRefresh = false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `loadShelfItems when service returns only unsupported items returns empty list`() = runTest {
        coEvery { mockCache.get() } returns null
        coEvery { mockService.getShelf() } returns shelfResponseOnlyOtherItems

        val result = repository.getBrochures(forceRefresh = false)

        assertTrue(result.isEmpty())
    }
}
