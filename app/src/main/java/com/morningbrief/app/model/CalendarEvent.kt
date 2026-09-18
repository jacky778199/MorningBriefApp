package com.morningbrief.app.model

data class CalendarEvent(
    val id: Long,
    val title: String,
    val startTime: String,
    val endTime: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val location: String,
    val description: String? = null,
    val colorHex: String = "#3B82F6",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val nearestMetroStation: String? = null,
    val isLocationUnique: Boolean = true,
    val candidateAddresses: List<String> = emptyList()
) {
    val hasLocation: Boolean
        get() = location.isNotBlank()

    /**
     * Checks whether location is vague, generic, or non-unique.
     */
    val isLocationAmbiguous: Boolean
        get() {
            if (!isLocationUnique) return true
            if (location.isBlank()) return true
            val lower = location.trim().lowercase(java.util.Locale.ROOT)
            val vagueKeywords = listOf(
                "office", "meeting room", "zoom", "teams", "google meet", "online", "tbd",
                "conference room", "starbucks", "cafe", "coffee", "room",
                "辦公室", "會議室", "線上", "待定", "咖啡廳", "路易莎", "星巴克", "遠端"
            )
            return vagueKeywords.any { lower == it || lower.startsWith(it) || lower.endsWith(it) } || location.trim().length < 5
        }

    /**
     * Generates a static map image URL for visual location preview.
     * Uses bilingual Traditional Chinese / English labels without internal pin overlays.
     */
    fun getStaticMapUrl(): String? {
        if (!hasLocation) return null
        val lat = latitude ?: 25.0339
        val lon = longitude ?: 121.5645
        return "https://static-maps.yandex.ru/1.x/?ll=$lon,$lat&z=15&size=650,320&l=map&lang=en_US"
    }
}
