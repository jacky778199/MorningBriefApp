package com.morningbrief.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.morningbrief.app.ui.components.CalendarAgendaCard
import com.morningbrief.app.ui.components.HeaderCard
import com.morningbrief.app.ui.components.MetroDepartureCard
import com.morningbrief.app.ui.theme.MorningBriefTheme
import com.morningbrief.app.ui.viewmodel.MorningBriefViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MorningBriefViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            MorningBriefTheme(darkTheme = uiState.isDarkMode) {
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val calGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
                    val locGranted = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false) ||
                                     (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false)
                    viewModel.onCalendarPermissionResult(calGranted)
                    viewModel.onLocationPermissionResult(locGranted)
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    permissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    MorningBriefScreen(
                        dateString = uiState.dateString,
                        agendaEvents = uiState.todayEvents,
                        metroShifts = uiState.metroShifts,
                        selectedOrigin = uiState.selectedMetroOrigin,
                        selectedDestination = uiState.selectedMetroDestination,
                        availableStations = uiState.availableMetroStations,
                        onSelectOrigin = { viewModel.selectMetroOrigin(it) },
                        onSelectDestination = { viewModel.selectMetroDestination(it) },
                        onResetOriginToGps = { viewModel.resetOriginToCurrentLocation() },
                        hasCalendarPermission = uiState.hasCalendarPermission,
                        isDarkMode = uiState.isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onRequestCalendarPermission = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        onRefresh = { viewModel.loadData() },
                        onUpdateLocation = { eventId, newLocation ->
                            viewModel.updateEventLocation(eventId, newLocation)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MorningBriefScreen(
    dateString: String,
    agendaEvents: List<com.morningbrief.app.model.CalendarEvent>,
    metroShifts: List<com.morningbrief.app.model.MetroShift>,
    selectedOrigin: String,
    selectedDestination: String,
    availableStations: List<com.morningbrief.app.repository.MrtStation>,
    onSelectOrigin: (String) -> Unit,
    onSelectDestination: (String) -> Unit,
    onResetOriginToGps: () -> Unit,
    hasCalendarPermission: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRefresh: () -> Unit,
    onUpdateLocation: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Morning Brief Greeting Header with Dark Mode Toggle
        HeaderCard(
            dateString = dateString,
            agendaCount = agendaEvents.size,
            nextMetroMins = metroShifts.firstOrNull()?.minutesUntilDeparture,
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
            onRefreshClick = onRefresh
        )

        // Taipei Metro Board: Customizable Origin (Defaults to Phone GPS) -> Customizable Destination (Defaults to Next Upcoming Agenda Item)
        MetroDepartureCard(
            shifts = metroShifts,
            selectedOrigin = selectedOrigin,
            selectedDestination = selectedDestination,
            availableStations = availableStations,
            onSelectOrigin = onSelectOrigin,
            onSelectDestination = onSelectDestination,
            onResetOriginToGps = onResetOriginToGps,
            onRefresh = onRefresh
        )

        // Today's Google Calendar Agenda with Visual Location Cards
        CalendarAgendaCard(
            events = agendaEvents,
            hasCalendarPermission = hasCalendarPermission,
            onRequestPermission = onRequestCalendarPermission,
            onUpdateLocation = onUpdateLocation
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
