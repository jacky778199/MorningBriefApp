package com.morningbrief.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morningbrief.app.model.CalendarEvent
import com.morningbrief.app.model.MetroShift
import com.morningbrief.app.repository.CalendarRepository
import com.morningbrief.app.repository.MetroRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.morningbrief.app.repository.MrtStation

data class MorningBriefUiState(
    val dateString: String = "",
    val todayEvents: List<CalendarEvent> = emptyList(),
    val metroShifts: List<MetroShift> = emptyList(),
    val selectedMetroOrigin: String = "三民高中",
    val selectedMetroDestination: String = "台北101/世貿",
    val availableMetroStations: List<MrtStation> = emptyList(),
    val hasUserSelectedOrigin: Boolean = false,
    val hasUserSelectedDestination: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isRefreshing: Boolean = false,
    val isDarkMode: Boolean = false
)

class MorningBriefViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarRepository = CalendarRepository(application)
    private val metroRepository = MetroRepository()
    private val locationPrefs = application.getSharedPreferences("user_event_location_overrides", android.content.Context.MODE_PRIVATE)

    private fun saveLocationOverride(event: CalendarEvent) {
        locationPrefs.edit().apply {
            putString("loc_${event.id}", event.location)
            if (event.latitude != null) {
                putString("lat_${event.id}", event.latitude.toString())
            } else {
                remove("lat_${event.id}")
            }
            if (event.longitude != null) {
                putString("lon_${event.id}", event.longitude.toString())
            } else {
                remove("lon_${event.id}")
            }
            putString("metro_${event.id}", event.nearestMetroStation ?: "")
            putBoolean("unique_${event.id}", event.isLocationUnique)
            apply()
        }
    }

    private fun applyLocationOverrides(events: List<CalendarEvent>): List<CalendarEvent> {
        return events.map { event ->
            val savedLoc = locationPrefs.getString("loc_${event.id}", null)
            if (!savedLoc.isNullOrBlank()) {
                val lat = try {
                    locationPrefs.getString("lat_${event.id}", null)?.toDoubleOrNull()
                        ?: if (locationPrefs.contains("lat_${event.id}")) locationPrefs.getFloat("lat_${event.id}", 0f).toDouble() else null
                } catch (e: Exception) {
                    null
                } ?: event.latitude

                val lon = try {
                    locationPrefs.getString("lon_${event.id}", null)?.toDoubleOrNull()
                        ?: if (locationPrefs.contains("lon_${event.id}")) locationPrefs.getFloat("lon_${event.id}", 0f).toDouble() else null
                } catch (e: Exception) {
                    null
                } ?: event.longitude

                val savedMetro = locationPrefs.getString("metro_${event.id}", null)
                val savedUnique = locationPrefs.getBoolean("unique_${event.id}", event.isLocationUnique)
                event.copy(
                    location = savedLoc,
                    latitude = lat,
                    longitude = lon,
                    nearestMetroStation = if (!savedMetro.isNullOrBlank()) savedMetro else event.nearestMetroStation,
                    isLocationUnique = savedUnique
                )
            } else {
                event
            }
        }
    }

    private val _uiState = MutableStateFlow(
        MorningBriefUiState(
            availableMetroStations = metroRepository.allStations
        )
    )
    val uiState: StateFlow<MorningBriefUiState> = _uiState.asStateFlow()

    init {
        updateDateString()
        loadData()
        startMetroTimer()
    }

    private fun updateDateString() {
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.ENGLISH)
        _uiState.update { it.copy(dateString = dateFormat.format(Date())) }
    }

    /**
     * Requirement 3: 依照現在時間最接近的下一個要去的行程 的目的地最近的捷運站
     */
    fun computeNextUpcomingDestination(events: List<CalendarEvent>): String {
        val now = System.currentTimeMillis()
        val eventsWithLoc = events.filter { it.hasLocation }
        if (eventsWithLoc.isEmpty()) return "南勢角"

        // 1) Find the next upcoming event (event end time has not passed yet)
        // 2) Sorted by start time ascending
        val upcoming = eventsWithLoc
            .filter { it.endEpochMillis >= now }
            .minByOrNull { it.startEpochMillis }

        // If all today's events have already finished, fallback to last or first event
        val targetEvent = upcoming ?: eventsWithLoc.lastOrNull() ?: eventsWithLoc.first()

        val stationName = metroRepository.extractStationName(targetEvent.nearestMetroStation)
            ?: targetEvent.let {
                val coords = if (it.latitude != null && it.longitude != null) {
                    Pair(it.latitude, it.longitude)
                } else {
                    calendarRepository.resolveCoordinates(it.location)
                }
                val desc = metroRepository.findNearestMetroStation(coords.first, coords.second, it.location)
                metroRepository.extractStationName(desc)
            }

        return stationName ?: "南勢角"
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val (hasCalPermission, hasLocPermission, events, deviceLoc) = withContext(Dispatchers.IO) {
                val calPerm = calendarRepository.hasCalendarPermission()
                val locPerm = calendarRepository.hasLocationPermission()
                val evs = calendarRepository.getTodayEvents(useDemoIfNoPermission = false)
                val devLoc = calendarRepository.getDeviceLocation()
                listOf(calPerm, locPerm, evs, devLoc)
            }

            @Suppress("UNCHECKED_CAST")
            val rawEvents = events as List<CalendarEvent>
            val evs = applyLocationOverrides(rawEvents)
            @Suppress("UNCHECKED_CAST")
            val devLocation = deviceLoc as? Pair<Double, Double>

            // Requirement 2: Origin station defaults to phone GPS location's nearest metro station
            val currentOrigin = if (!_uiState.value.hasUserSelectedOrigin && devLocation != null) {
                val nearestStation = metroRepository.findClosestStation(devLocation.first, devLocation.second)
                nearestStation?.name ?: _uiState.value.selectedMetroOrigin
            } else {
                _uiState.value.selectedMetroOrigin
            }

            // Requirement 3: Destination station defaults to next upcoming event's nearest metro station
            val currentDestination = if (!_uiState.value.hasUserSelectedDestination) {
                computeNextUpcomingDestination(evs)
            } else {
                _uiState.value.selectedMetroDestination
            }

            val shifts = metroRepository.getUpcomingShifts(currentOrigin, currentDestination, count = 4)

            _uiState.update {
                it.copy(
                    todayEvents = evs,
                    metroShifts = shifts,
                    selectedMetroOrigin = currentOrigin,
                    selectedMetroDestination = currentDestination,
                    hasCalendarPermission = hasCalPermission as Boolean,
                    hasLocationPermission = hasLocPermission as Boolean,
                    isRefreshing = false
                )
            }
        }
    }

    fun selectMetroOrigin(origin: String) {
        _uiState.update {
            it.copy(
                selectedMetroOrigin = origin,
                hasUserSelectedOrigin = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val destination = _uiState.value.selectedMetroDestination
            val shifts = metroRepository.getUpcomingShifts(origin, destination, count = 4)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(metroShifts = shifts) }
            }
        }
    }

    fun resetOriginToCurrentLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceLoc = calendarRepository.getDeviceLocation()
            val station = if (deviceLoc != null) {
                metroRepository.findClosestStation(deviceLoc.first, deviceLoc.second)?.name ?: "三民高中"
            } else {
                "三民高中"
            }
            val destination = _uiState.value.selectedMetroDestination
            val shifts = metroRepository.getUpcomingShifts(station, destination, count = 4)
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        selectedMetroOrigin = station,
                        metroShifts = shifts,
                        hasUserSelectedOrigin = false
                    )
                }
            }
        }
    }

    fun selectMetroDestination(destination: String) {
        _uiState.update {
            it.copy(
                selectedMetroDestination = destination,
                hasUserSelectedDestination = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val origin = _uiState.value.selectedMetroOrigin
            val shifts = metroRepository.getUpcomingShifts(origin, destination, count = 4)
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(metroShifts = shifts) }
            }
        }
    }

    fun updateEventLocation(
        eventId: Long,
        newLocation: String,
        customLat: Double? = null,
        customLon: Double? = null,
        isUnique: Boolean = true
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val resolution = if (customLat != null && customLon != null) {
                com.morningbrief.app.repository.LocationResolutionResult(
                    latitude = customLat,
                    longitude = customLon,
                    isUnique = isUnique,
                    formattedAddress = newLocation
                )
            } else {
                calendarRepository.resolveLocationDetails(newLocation)
            }

            val nearestMetro = metroRepository.findNearestMetroStation(
                resolution.latitude,
                resolution.longitude,
                newLocation
            )

            val cleanLocation = newLocation.trim()
            val updatedEvents = _uiState.value.todayEvents.map { event ->
                if (event.id == eventId) {
                    val updated = event.copy(
                        location = cleanLocation,
                        latitude = resolution.latitude,
                        longitude = resolution.longitude,
                        nearestMetroStation = nearestMetro,
                        isLocationUnique = resolution.isUnique,
                        candidateAddresses = resolution.candidates.map { it.fullAddress }
                    )
                    saveLocationOverride(updated)
                    updated
                } else {
                    event
                }
            }

            // If user hasn't manually overridden destination, sync to next upcoming event
            val newDestination = if (!_uiState.value.hasUserSelectedDestination) {
                computeNextUpcomingDestination(updatedEvents)
            } else {
                _uiState.value.selectedMetroDestination
            }

            val origin = _uiState.value.selectedMetroOrigin
            val shifts = metroRepository.getUpcomingShifts(origin, newDestination, count = 4)

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        todayEvents = updatedEvents,
                        selectedMetroDestination = newDestination,
                        metroShifts = shifts
                    )
                }
            }
        }
    }

    private fun startMetroTimer() {
        viewModelScope.launch {
            while (true) {
                delay(60_000) // Auto-refresh Metro countdowns every 60 seconds (1 min) to conserve API usage
                val origin = _uiState.value.selectedMetroOrigin
                val destination = if (!_uiState.value.hasUserSelectedDestination) {
                    computeNextUpcomingDestination(_uiState.value.todayEvents)
                } else {
                    _uiState.value.selectedMetroDestination
                }
                val shifts = withContext(Dispatchers.IO) {
                    metroRepository.getUpcomingShifts(origin, destination, count = 4)
                }
                _uiState.update {
                    it.copy(
                        selectedMetroDestination = destination,
                        metroShifts = shifts
                    )
                }
            }
        }
    }

    fun onCalendarPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasCalendarPermission = isGranted) }
        loadData()
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = isGranted) }
        loadData()
    }

    fun onPermissionResult(isGranted: Boolean) {
        onCalendarPermissionResult(isGranted)
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }
}
