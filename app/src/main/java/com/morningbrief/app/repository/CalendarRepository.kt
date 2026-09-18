package com.morningbrief.app.repository

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.morningbrief.app.model.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarRepository(private val context: Context) {

    private val metroRepo = MetroRepository()

    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Gets the latest known device location via LocationManager.
     */
    fun getDeviceLocation(): Pair<Double, Double>? {
        if (!hasLocationPermission()) return null
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return null
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER,
                android.location.LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        return Pair(loc.latitude, loc.longitude)
                    }
                }
            }
        } catch (e: SecurityException) {
            // Ignored
        } catch (e: Exception) {
            // Ignored
        }
        return null
    }

    /**
     * Fetches today's events from device calendar.
     * Returns empty list if no calendar permission or no events scheduled.
     */
    fun getTodayEvents(useDemoIfNoPermission: Boolean = false): List<CalendarEvent> {
        if (!hasCalendarPermission()) {
            return emptyList()
        }

        val events = mutableListOf<CalendarEvent>()
        val contentResolver: ContentResolver = context.contentResolver

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_COLOR
        )

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?) AND (${CalendarContract.Events.DELETED} != 1)"
        val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        try {
            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow(CalendarContract.Events._ID)
                val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val startIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                val locIdx = it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
                val descIdx = it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)

                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val title = it.getString(titleIdx) ?: "Untitled Event"
                    val startMillis = it.getLong(startIdx)
                    val endMillis = it.getLong(endIdx)
                    val location = it.getString(locIdx) ?: ""
                    val description = it.getString(descIdx)

                    val locResult = if (location.isNotBlank()) resolveLocationDetails(location) else null
                    val nearestMetro = if (locResult != null) metroRepo.findNearestMetroStation(locResult.latitude, locResult.longitude, location) else null

                    events.add(
                        CalendarEvent(
                            id = id,
                            title = title,
                            startTime = timeFormatter.format(Date(startMillis)),
                            endTime = timeFormatter.format(Date(endMillis)),
                            startEpochMillis = startMillis,
                            endEpochMillis = endMillis,
                            location = location,
                            description = description,
                            latitude = locResult?.latitude,
                            longitude = locResult?.longitude,
                            nearestMetroStation = nearestMetro,
                            isLocationUnique = locResult?.isUnique ?: true,
                            candidateAddresses = locResult?.candidates?.map { it.fullAddress } ?: emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return events
    }

    /**
     * Realistic morning demo schedule tailored for Taipei / New Taipei region.
     */
    fun getDemoEvents(): List<CalendarEvent> {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        fun getTimeString(hour: Int, minute: Int): String {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            return timeFormat.format(calendar.time)
        }

        fun getMillis(hour: Int, minute: Int): Long {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            return calendar.timeInMillis
        }

        return listOf(
            CalendarEvent(
                id = 101,
                title = "Morning Team Sync & Standup",
                startTime = getTimeString(9, 30),
                endTime = getTimeString(10, 15),
                startEpochMillis = getMillis(9, 30),
                endEpochMillis = getMillis(10, 15),
                location = "Taipei 101 Tower, 35F Office",
                description = "Review weekly Q3 progress & release roadmap with product team.",
                colorHex = "#3B82F6",
                latitude = 25.0339,
                longitude = 121.5645,
                nearestMetroStation = "台北101/世貿站 (R03) • 步行約 3 分鐘",
                isLocationUnique = true
            ),
            CalendarEvent(
                id = 102,
                title = "Client Tech Architecture Lunch",
                startTime = getTimeString(12, 0),
                endTime = getTimeString(13, 30),
                startEpochMillis = getMillis(12, 0),
                endEpochMillis = getMillis(13, 30),
                location = "Starbucks (Meeting)",
                description = "Discuss cloud architecture strategy. Location is non-unique, tap to select exact store.",
                colorHex = "#10B981",
                latitude = 25.0416,
                longitude = 121.5511,
                nearestMetroStation = "忠孝敦化站 (BL16) • 步行約 2 分鐘",
                isLocationUnique = false,
                candidateAddresses = listOf(
                    "星巴克 敦化門市 (台北市大安區敦化南路一段219號)",
                    "星巴克 忠孝延吉門市 (台北市大安區忠孝東路四段248巷)",
                    "星巴克 台北101門市 (台北市信義區信義路五段7號35樓)"
                )
            ),
            CalendarEvent(
                id = 103,
                title = "Product Demo & Code Review",
                startTime = getTimeString(15, 0),
                endTime = getTimeString(16, 0),
                startEpochMillis = getMillis(15, 0),
                endEpochMillis = getMillis(16, 0),
                location = "Neihu Technology Park, Building B",
                description = "Present new Compose UI Morning Brief prototype to stakeholders.",
                colorHex = "#8B5CF6",
                latitude = 25.0792,
                longitude = 121.5753,
                nearestMetroStation = "港墘站 (BR17) • 步行約 5 分鐘",
                isLocationUnique = true
            )
        )
    }

    /**
     * Resolves geographical coordinates from location name using Android Geocoder with regional fallbacks.
     */
    fun resolveCoordinates(location: String): Pair<Double, Double> {
        val result = resolveLocationDetails(location)
        return Pair(result.latitude, result.longitude)
    }

    /**
     * Normalizes Taiwan addresses:
     * - Strips Chinese fullwidth （...） and ASCII (...) parentheses
     * - Normalizes Arabic section numbers (1段 -> 一段, etc.)
     * - Strips floor numbers and interior room descriptions
     */
    fun normalizeTaiwanAddress(rawAddress: String): String {
        return rawAddress
            // Remove parenthetical expressions: both fullwidth （...） and ASCII (...)
            .replace(Regex("[（(][^）)]*[）)]"), "")
            // Normalize section numbers 1段..9段 to 一段..九段
            .replace(Regex("(?<=[^\\d])1段|(?<=^)1段"), "一段")
            .replace(Regex("(?<=[^\\d])2段|(?<=^)2段"), "二段")
            .replace(Regex("(?<=[^\\d])3段|(?<=^)3段"), "三段")
            .replace(Regex("(?<=[^\\d])4段|(?<=^)4段"), "四段")
            .replace(Regex("(?<=[^\\d])5段|(?<=^)5段"), "五段")
            .replace(Regex("(?<=[^\\d])6段|(?<=^)6段"), "六段")
            .replace(Regex("(?<=[^\\d])7段|(?<=^)7段"), "七段")
            .replace(Regex("(?<=[^\\d])8段|(?<=^)8段"), "八段")
            .replace(Regex("(?<=[^\\d])9段|(?<=^)9段"), "九段")
            // Remove floor / room information: e.g. "10樓", "10F", "B1", "Room 302", "301室", "辦公室"
            .replace(Regex("(?<=\\d號)\\s*\\d+[fF樓].*"), "")
            .replace(Regex("(?<=\\d號)\\s*B\\d+.*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?<=\\d號)\\s*\\d+室.*"), "")
            .replace(Regex("\\s*\\d+樓.*"), "")
            .replace(Regex("\\s*\\d+[fF]\\b.*"), "")
            .replace(Regex("\\s*B\\d+\\b.*", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun extractParentheticalHint(rawAddress: String): String? {
        val match = Regex("[（(]([^）)]+)[）)]").find(rawAddress)
        return match?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
    }

    /**
     * Full Geocoding and Uniqueness check.
     */
    fun resolveLocationDetails(location: String): LocationResolutionResult {
        if (location.isBlank()) {
            return LocationResolutionResult(
                latitude = 25.0339,
                longitude = 121.5645,
                isUnique = false,
                formattedAddress = ""
            )
        }

        val cleanQuery = normalizeTaiwanAddress(location)
        val parentheticalHint = extractParentheticalHint(location)

        val isStreetAddress = cleanQuery.contains("路") || cleanQuery.contains("街") ||
                cleanQuery.contains("段") || cleanQuery.contains("號") ||
                cleanQuery.contains("巷") || cleanQuery.contains("弄")

        // 1. Explicit MRT Station match (Station name ONLY if not a street address, or if explicitly "站"/"捷運")
        if (!isStreetAddress || location.contains("捷運") || cleanQuery.endsWith("站")) {
            val matchedStation = metroRepo.allStations.find { station ->
                val stName = station.name
                cleanQuery == stName ||
                        cleanQuery == "${stName}站" ||
                        cleanQuery == "捷運${stName}" ||
                        cleanQuery == "捷運${stName}站" ||
                        cleanQuery.endsWith("${stName}站") ||
                        cleanQuery.startsWith("捷運${stName}") ||
                        cleanQuery.equals(station.nameEn, ignoreCase = true)
            }
            if (matchedStation != null) {
                return LocationResolutionResult(
                    latitude = matchedStation.latitude,
                    longitude = matchedStation.longitude,
                    isUnique = true,
                    formattedAddress = location,
                    candidates = emptyList()
                )
            }
        }

        // 2. Known Taipei / New Taipei Landmarks (including District Offices and Key Venues)
        val landmarks = mapOf(
            "中正區公所" to Pair(25.0322, 121.5182),
            "南門市場" to Pair(25.0325, 121.5188),
            "國父紀念館" to Pair(25.0402, 121.5598),
            "中正紀念堂" to Pair(25.0353, 121.5197),
            "台北101" to Pair(25.0339, 121.5645),
            "世貿" to Pair(25.0339, 121.5645),
            "鼎泰豐" to Pair(25.0416, 121.5511),
            "內湖科技園區" to Pair(25.0792, 121.5753),
            "內科" to Pair(25.0792, 121.5753),
            "微風廣場" to Pair(25.0478, 121.5170),
            "台北車站" to Pair(25.0478, 121.5170),
            "新光三越信義" to Pair(25.0365, 121.5670),
            "信義a11" to Pair(25.0365, 121.5670),
            "三民高中" to Pair(25.0858, 121.4728),
            "華山" to Pair(25.0441, 121.5294),
            "松菸" to Pair(25.0438, 121.5606),
            "松山文創" to Pair(25.0438, 121.5606),
            "大巨蛋" to Pair(25.0427, 121.5597),
            "小巨蛋" to Pair(25.0516, 121.5501),
            "龍山寺" to Pair(25.0366, 121.4998),
            "行天宮" to Pair(25.0598, 121.5332),
            "美麗華" to Pair(25.0836, 121.5576),
            "圓山大飯店" to Pair(25.0781, 121.5262),
            "士林夜市" to Pair(25.0881, 121.5245),
            "饒河夜市" to Pair(25.0509, 121.5775),
            "寧夏夜市" to Pair(25.0556, 121.5154),
            "師大夜市" to Pair(25.0255, 121.5290),
            "公館" to Pair(25.0135, 121.5365),
            "台灣大學" to Pair(25.0173, 121.5405),
            "台大" to Pair(25.0173, 121.5405),
            "師範大學" to Pair(25.0267, 121.5278),
            "政治大學" to Pair(24.9875, 121.5760),
            "動物園" to Pair(24.9982, 121.5796),
            "淡水老街" to Pair(25.1712, 121.4398),
            "陽明山" to Pair(25.1558, 121.5475),
            "板橋大遠百" to Pair(25.0135, 121.4660)
        )

        // Check if parenthetical hint directly matches landmark
        if (parentheticalHint != null) {
            for ((k, coords) in landmarks) {
                if (parentheticalHint.contains(k, ignoreCase = true)) {
                    return LocationResolutionResult(
                        latitude = coords.first,
                        longitude = coords.second,
                        isUnique = true,
                        formattedAddress = location,
                        candidates = emptyList()
                    )
                }
            }
        }

        // If not a street address, check landmark match
        if (!isStreetAddress) {
            for ((k, coords) in landmarks) {
                if (cleanQuery.contains(k, ignoreCase = true) || location.contains(k, ignoreCase = true)) {
                    return LocationResolutionResult(
                        latitude = coords.first,
                        longitude = coords.second,
                        isUnique = true,
                        formattedAddress = location,
                        candidates = emptyList()
                    )
                }
            }
        }

        // 3. Android Geocoder with Normalized Taiwan Address
        try {
            val geocoder = android.location.Geocoder(context, Locale.TAIWAN)
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(cleanQuery, 5)
                ?: geocoder.getFromLocationName("台灣 $cleanQuery", 5)
                ?: if (parentheticalHint != null) geocoder.getFromLocationName(parentheticalHint, 5) else null

            if (!addresses.isNullOrEmpty()) {
                val candidates = addresses.map { addr ->
                    val fullAddr = (0..addr.maxAddressLineIndex)
                        .mapNotNull { addr.getAddressLine(it) }
                        .joinToString(" ")
                    val title = addr.featureName ?: fullAddr
                    CandidateLocation(
                        title = title,
                        fullAddress = if (fullAddr.isNotBlank()) fullAddr else title,
                        latitude = addr.latitude,
                        longitude = addr.longitude
                    )
                }

                val first = addresses[0]
                val isUnique = addresses.size == 1 || cleanQuery.contains("號") || first.subThoroughfare != null
                return LocationResolutionResult(
                    latitude = first.latitude,
                    longitude = first.longitude,
                    isUnique = isUnique,
                    formattedAddress = location,
                    candidates = candidates
                )
            }
        } catch (e: Exception) {
            // Geocoder service may be unavailable on certain devices
        }

        // 4. Online Photon (OpenStreetMap based) Geocoder Fallback
        val photonCoords = queryOnlinePhoton(cleanQuery)
            ?: if (parentheticalHint != null) queryOnlinePhoton(parentheticalHint) else null
            ?: queryOnlineNominatim(cleanQuery)

        if (photonCoords != null) {
            return LocationResolutionResult(
                latitude = photonCoords.first,
                longitude = photonCoords.second,
                isUnique = true,
                formattedAddress = location,
                candidates = emptyList()
            )
        }

        // 5. District Centers in Taipei / New Taipei
        val districts = mapOf(
            "中正區" to Pair(25.0322, 121.5183),
            "大同區" to Pair(25.0628, 121.5129),
            "中山區" to Pair(25.0685, 121.5332),
            "松山區" to Pair(25.0598, 121.5575),
            "大安區" to Pair(25.0261, 121.5434),
            "萬華區" to Pair(25.0345, 121.4988),
            "信義區" to Pair(25.0339, 121.5645),
            "士林區" to Pair(25.0934, 121.5262),
            "北投區" to Pair(25.1319, 121.4986),
            "內湖區" to Pair(25.0836, 121.5944),
            "南港區" to Pair(25.0531, 121.6070),
            "文山區" to Pair(24.9985, 121.5583),
            "板橋區" to Pair(25.0135, 121.4637),
            "三重區" to Pair(25.0615, 121.4883),
            "中和區" to Pair(25.0003, 121.4947),
            "永和區" to Pair(25.0076, 121.5135),
            "新莊區" to Pair(25.0366, 121.4504),
            "新店區" to Pair(24.9681, 121.5414),
            "蘆洲區" to Pair(25.0858, 121.4728),
            "土城區" to Pair(24.9731, 121.4444),
            "汐止區" to Pair(25.0658, 121.6548)
        )
        for ((dist, coords) in districts) {
            if (cleanQuery.contains(dist)) {
                return LocationResolutionResult(
                    latitude = coords.first,
                    longitude = coords.second,
                    isUnique = true,
                    formattedAddress = location,
                    candidates = emptyList()
                )
            }
        }

        // 6. General fallback: Taipei Station / Center
        val isGeneric = cleanQuery.length < 5 || listOf(
            "office", "starbucks", "cafe", "coffee", "meeting", "room",
            "辦公室", "會議室", "咖啡廳", "路易莎", "星巴克"
        ).any { cleanQuery.lowercase(Locale.ROOT).contains(it) }

        return LocationResolutionResult(
            latitude = 25.0478,
            longitude = 121.5170,
            isUnique = !isGeneric,
            formattedAddress = location,
            candidates = emptyList()
        )
    }

    private fun queryOnlinePhoton(query: String): Pair<Double, Double>? {
        return try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = java.net.URL("https://photon.komoot.io/api/?q=$encoded&limit=1")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "MorningBriefApp/1.0 (Android; Location)")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = org.json.JSONObject(jsonStr)
                val features = root.optJSONArray("features")
                if (features != null && features.length() > 0) {
                    val first = features.getJSONObject(0)
                    val geom = first.getJSONObject("geometry")
                    val coords = geom.getJSONArray("coordinates")
                    val lon = coords.getDouble(0)
                    val lat = coords.getDouble(1)
                    Pair(lat, lon)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun queryOnlineNominatim(query: String): Pair<Double, Double>? {
        return try {
            val encoded = java.net.URLEncoder.encode("台灣 $query", "UTF-8")
            val url = java.net.URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.setRequestProperty("User-Agent", "MorningBriefApp/1.0 (Android; Location)")
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArr = org.json.JSONArray(jsonStr)
                if (jsonArr.length() > 0) {
                    val first = jsonArr.getJSONObject(0)
                    val lat = first.getString("lat").toDouble()
                    val lon = first.getString("lon").toDouble()
                    Pair(lat, lon)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

data class LocationResolutionResult(
    val latitude: Double,
    val longitude: Double,
    val isUnique: Boolean,
    val formattedAddress: String,
    val candidates: List<CandidateLocation> = emptyList()
)

data class CandidateLocation(
    val title: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double
)
