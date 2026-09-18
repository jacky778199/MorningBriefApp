package com.morningbrief.app.repository.tdx

import com.google.gson.annotations.SerializedName

data class TdxTokenResponse(
    @SerializedName("access_token")
    val accessToken: String = "",
    @SerializedName("expires_in")
    val expiresIn: Long = 86400,
    @SerializedName("token_type")
    val tokenType: String = "Bearer"
)

data class TdxStationTimetable(
    @SerializedName("StationID")
    val stationId: String = "",
    @SerializedName("StationName")
    val stationName: TdxName? = null,
    @SerializedName("DestinationStationID")
    val destinationStationId: String? = null,
    @SerializedName("DestinationStationName")
    val destinationStationName: TdxName? = null,
    @SerializedName("Direction")
    val direction: Int = 0,
    @SerializedName("Timetables")
    val timetables: List<TdxTimetableEntry> = emptyList()
)

data class TdxName(
    @SerializedName("Zh_tw")
    val zhTw: String? = null,
    @SerializedName("En")
    val en: String? = null
)

data class TdxTimetableEntry(
    @SerializedName("Sequence")
    val sequence: Int = 0,
    @SerializedName("DepartureTime")
    val departureTime: String = "", // e.g. "06:03" or "06:03:00"
    @SerializedName("DestinationStationID")
    val destinationStationId: String? = null
)
