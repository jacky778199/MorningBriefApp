package com.morningbrief.app.repository.tdx

import android.util.Log
import com.morningbrief.app.model.MetroShift
import com.morningbrief.app.repository.MrtStation
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class TdxRepository {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://tdx.transportdata.tw/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val authApi: TdxAuthApi by lazy { retrofit.create(TdxAuthApi::class.java) }
    private val metroApi: TdxMetroApi by lazy { retrofit.create(TdxMetroApi::class.java) }

    @Volatile
    private var cachedToken: String? = null
    @Volatile
    private var tokenExpiresAt: Long = 0L

    // In-memory cache for station timetables: StationID -> Pair(fetchTimeMillis, Timetables)
    // Timetables are cached for 12 hours so we DO NOT burn API quota!
    private val timetableCache = ConcurrentHashMap<String, Pair<Long, List<TdxStationTimetable>>>()

    suspend fun getValidToken(): String? {
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiresAt - 60_000L) {
            return cachedToken
        }

        val clientId = TdxConfig.getClientId()
        val clientSecret = TdxConfig.getClientSecret()
        if (clientId.isBlank() || clientSecret.isBlank()) {
            return null
        }

        return try {
            val resp = authApi.getAccessToken(
                clientId = clientId,
                clientSecret = clientSecret
            )
            cachedToken = resp.accessToken
            tokenExpiresAt = now + (resp.expiresIn * 1000L)
            resp.accessToken
        } catch (e: Exception) {
            Log.e("TdxRepository", "Failed to obtain TDX token: ${e.message}")
            null
        }
    }

    suspend fun getStationTimetables(stationId: String): List<TdxStationTimetable>? {
        val now = System.currentTimeMillis()
        val cached = timetableCache[stationId]
        if (cached != null && (now - cached.first) < 12 * 3600 * 1000L) {
            return cached.second
        }

        val token = getValidToken() ?: return null
        return try {
            val filter = "StationID eq '$stationId'"
            val result = metroApi.getStationTimetable(
                authorization = "Bearer $token",
                filter = filter
            )
            if (result.isNotEmpty()) {
                timetableCache[stationId] = Pair(now, result)
            }
            result
        } catch (e: Exception) {
            Log.e("TdxRepository", "Failed to fetch TDX timetable for $stationId: ${e.message}")
            null
        }
    }

    /**
     * Parses real schedule entries from TDX and builds upcoming MetroShift list.
     */
    suspend fun getRealUpcomingShifts(
        originStation: MrtStation,
        destStation: MrtStation?,
        travelTimeMinutes: Int,
        lineColorHex: String,
        platformDesc: String,
        count: Int = 4
    ): List<MetroShift>? {
        if (!TdxConfig.hasCredentials()) {
            return null
        }

        val primaryCode = originStation.code.split(" / ").first().trim()
        val tables = getStationTimetables(primaryCode) ?: return null
        if (tables.isEmpty()) return null

        val destClean = destStation?.name?.removeSuffix("站")?.trim() ?: "南勢角"

        // Pick timetable matching desired travel direction
        val matchingTable = if (destStation != null) {
            tables.find { table ->
                val tableDest = table.destinationStationName?.zhTw ?: ""
                tableDest.contains(destClean) || destClean.contains(tableDest)
            } ?: tables.find { table ->
                // For transfers, check if line terminal direction matches
                val tableDest = table.destinationStationName?.zhTw ?: ""
                when {
                    originStation.line.contains("中和新蘆") -> tableDest.contains("南勢角")
                    originStation.line.contains("淡水信義") -> if (originStation.latitude > destStation.latitude) tableDest.contains("象山") else tableDest.contains("淡水")
                    originStation.line.contains("板南") -> if (originStation.longitude < destStation.longitude) tableDest.contains("南港") else tableDest.contains("頂埔")
                    else -> true
                }
            } ?: tables.firstOrNull()
        } else {
            tables.firstOrNull()
        } ?: return null

        val nowMillis = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Find upcoming departure times
        val upcomingEntries = matchingTable.timetables.mapNotNull { entry ->
            val parts = entry.departureTime.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null

            val depCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val depEpoch = depCal.timeInMillis
            val diffMillis = depEpoch - nowMillis

            // Boarding tolerance: train is departing/boarding if within 40 seconds
            if (diffMillis >= -40_000L) {
                Pair(entry, depEpoch)
            } else {
                null
            }
        }.sortedBy { it.second }.toMutableList()

        // If near end of day (e.g. late night) and remaining trains < count, wrap around to next morning trains
        if (upcomingEntries.size < count && matchingTable.timetables.isNotEmpty()) {
            val morningEntries = matchingTable.timetables.mapNotNull { entry ->
                val parts = entry.departureTime.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val depCal = Calendar.getInstance().apply {
                    timeInMillis = nowMillis
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                Pair(entry, depCal.timeInMillis)
            }.sortedBy { it.second }
            upcomingEntries.addAll(morningEntries.take(count - upcomingEntries.size))
        }

        val finalEntries = upcomingEntries.take(count)

        if (finalEntries.isEmpty()) {
            return null
        }

        val shifts = mutableListOf<MetroShift>()
        val destStationName = if (destClean.endsWith("站")) destClean else "${destClean}站"

        for (i in finalEntries.indices) {
            val (entry, depEpoch) = finalEntries[i]
            val diffMillis = depEpoch - nowMillis
            val minsUntil = if (diffMillis <= 40_000L) 0 else maxOf(1, ((diffMillis + 20_000L) / 60_000L).toInt())

            val headway = if (i > 0) {
                maxOf(2, ((depEpoch - upcomingEntries[i - 1].second) / 60_000L).toInt())
            } else {
                6
            }

            val depTimeStr = timeFormat.format(Date(depEpoch))
            val etaCal = Calendar.getInstance().apply {
                timeInMillis = depEpoch
                add(Calendar.MINUTE, travelTimeMinutes)
            }
            val etaStr = timeFormat.format(Date(etaCal.timeInMillis))

            val tdxTerminal = matchingTable.destinationStationName?.zhTw?.trim()
            val cleanPlatform = if (!tdxTerminal.isNullOrBlank()) "往 $tdxTerminal" else platformDesc

            shifts.add(
                MetroShift(
                    shiftId = "TDX_${originStation.code}_${depTimeStr}_$i",
                    stationName = "${originStation.name}站",
                    stationCode = originStation.code,
                    lineName = originStation.line,
                    lineColorHex = lineColorHex,
                    destination = destClean,
                    departureTimeFormatted = depTimeStr,
                    departureEpochMillis = depEpoch,
                    minutesUntilDeparture = minsUntil,
                    platform = cleanPlatform,
                    destinationStationName = destStationName,
                    etaTimeFormatted = etaStr,
                    travelTimeMinutes = travelTimeMinutes,
                    etaToDestinationFormatted = "$destStationName ETA $etaStr 約 $travelTimeMinutes 分鐘",
                    headwayFromPreviousMinutes = headway,
                    isOperating = true,
                    isRealTime = true
                )
            )
        }

        return shifts
    }
}
