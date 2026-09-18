package com.morningbrief.app

import com.morningbrief.app.repository.MetroRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

import kotlinx.coroutines.runBlocking

class MetroShiftCalculationTest {

    private val metroRepository = MetroRepository()

    @Test
    fun testUpcomingShiftsCount() = runBlocking {
        val shifts = metroRepository.getUpcomingShifts("三民高中", "南勢角", count = 4)
        assertEquals(4, shifts.size)
    }

    @Test
    fun testHeadwayBetweenSubsequentShifts() = runBlocking {
        val shifts = metroRepository.getUpcomingShifts("三民高中", "南勢角", count = 4)
        // Check that subsequent shifts follow consistent headway
        for (i in 1 until shifts.size) {
            val prev = shifts[i - 1]
            val curr = shifts[i]
            val diffMinutes = (curr.departureEpochMillis - prev.departureEpochMillis) / 60_000
            assertEquals("Headway between shift $i and ${i-1} should match headway", curr.headwayFromPreviousMinutes.toLong(), diffMinutes)
            assertTrue("Subsequent shift departure epoch should be strictly after previous", curr.departureEpochMillis > prev.departureEpochMillis)
        }
    }

    @Test
    fun testPlatformDirectionFormat() = runBlocking {
        val shifts = metroRepository.getUpcomingShifts("三民高中", "台北101/世貿", count = 4)
        assertNotNull(shifts.first().platform)
        assertTrue(shifts.first().platform.contains("月台"))
    }
}
