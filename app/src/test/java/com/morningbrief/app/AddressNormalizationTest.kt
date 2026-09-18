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
}
