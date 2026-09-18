package com.morningbrief.app

import android.content.Context
import android.content.ContextWrapper
import com.morningbrief.app.repository.CalendarRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class AddressNormalizationTest {

    private val dummyContext = ContextWrapper(null)
    private val repository = CalendarRepository(dummyContext)

    @Test
    fun testNormalizeTaiwanAddress() {
        val raw = "臺北市中正區羅斯福路1段8號10樓（中正區公所）"
        val normalized = repository.normalizeTaiwanAddress(raw)
        assertEquals("臺北市中正區羅斯福路一段8號", normalized)
    }

    @Test
    fun testNormalizeArabicSections() {
        assertEquals("新北市板橋區文化路一段100號", repository.normalizeTaiwanAddress("新北市板橋區文化路1段100號"))
        assertEquals("台北市中山區南京東路二段50號", repository.normalizeTaiwanAddress("台北市中山區南京東路2段50號"))
        assertEquals("台北市大安區信義路三段", repository.normalizeTaiwanAddress("台北市大安區信義路3段"))
    }

    @Test
    fun testStripFloorAndInteriorDetails() {
        assertEquals("台北市信義區信義路五段7號", repository.normalizeTaiwanAddress("台北市信義區信義路五段7號35樓 (Office)"))
        assertEquals("台北市南港區三重路66號", repository.normalizeTaiwanAddress("台北市南港區三重路66號3樓 (Meeting Room B)"))
    }

    @Test
    fun testLocationAmbiguityDetection() {
        val eventExact1 = com.morningbrief.app.model.CalendarEvent(
            id = 1, title = "Test", startTime = "09:00", endTime = "10:00",
            startEpochMillis = 0, endEpochMillis = 0,
            location = "台北市信義區信義路五段7號 35樓辦公室",
            isLocationUnique = true
        )
        org.junit.Assert.assertFalse("Street address with house number should not be ambiguous", eventExact1.isLocationAmbiguous)

        val eventExact2 = com.morningbrief.app.model.CalendarEvent(
            id = 2, title = "Test", startTime = "09:00", endTime = "10:00",
            startEpochMillis = 0, endEpochMillis = 0,
            location = "新北市板橋區文化路一段100號 會議室",
            isLocationUnique = true
        )
        org.junit.Assert.assertFalse("Street address with number should not be ambiguous", eventExact2.isLocationAmbiguous)

        val eventVague = com.morningbrief.app.model.CalendarEvent(
            id = 3, title = "Test", startTime = "09:00", endTime = "10:00",
            startEpochMillis = 0, endEpochMillis = 0,
            location = "辦公室",
            isLocationUnique = true
        )
        org.junit.Assert.assertTrue("Pure vague keyword should be ambiguous", eventVague.isLocationAmbiguous)
    }
}
