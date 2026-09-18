package com.morningbrief.app.model

enum class MetroShiftStatus {
    DEPARTING_SOON, // < 2 mins
    APPROACHING,     // 2-5 mins
    ON_SCHEDULE      // > 5 mins
}

data class MetroShift(
    val shiftId: String,
    val stationName: String = "三民高中站",
    val stationCode: String = "O19",
    val lineName: String = "中和新蘆線",
    val lineColorHex: String = "#F8961E", // Luzhou Line Orange
    val destination: String = "南勢角",
    val departureTimeFormatted: String, // e.g. "08:14"
    val departureEpochMillis: Long,
    val minutesUntilDeparture: Int,
    val platform: String = "1號月台 (Platform 1)",
    val etaToDestinationFormatted: String = "",
    val destinationStationName: String = "",
    val etaTimeFormatted: String = "",
    val travelTimeMinutes: Int = 0
) {
    val status: MetroShiftStatus
        get() = when {
            minutesUntilDeparture <= 1 -> MetroShiftStatus.DEPARTING_SOON
            minutesUntilDeparture <= 4 -> MetroShiftStatus.APPROACHING
            else -> MetroShiftStatus.ON_SCHEDULE
        }

    val countdownDisplay: String
        get() = when {
            minutesUntilDeparture <= 0 -> "即將進站 (Arriving)"
            minutesUntilDeparture == 1 -> "1 分鐘 (1 min)"
            else -> "$minutesUntilDeparture 分鐘 ($minutesUntilDeparture mins)"
        }
}
