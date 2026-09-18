package com.morningbrief.app.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morningbrief.app.model.MetroShift
import com.morningbrief.app.model.MetroShiftStatus
import com.morningbrief.app.repository.MrtStation
import com.morningbrief.app.ui.theme.MetroOrange

private fun getStationLineColor(line: String): Color {
    return when {
        line.contains("松山新店") || line.contains("綠線") -> Color(0xFF10B981)
        line.contains("淡水信義") || line.contains("紅線") -> Color(0xFFEF4444)
        line.contains("板南") || line.contains("藍線") -> Color(0xFF2563EB)
        line.contains("文湖") || line.contains("棕線") -> Color(0xFF8B5CF6)
        line.contains("環狀") || line.contains("黃線") -> Color(0xFFEAB308)
        else -> MetroOrange
    }
}

enum class StationPickerTarget {
    ORIGIN,
    DESTINATION
}

@Composable
fun MetroEtaBadge(
    destStationName: String,
    etaTime: String,
    travelMinutes: Int,
    destColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    val cleanName = if (destStationName.endsWith("站")) destStationName else "${destStationName}站"
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Destination station name: same style (xx站)
        Text(
            text = cleanName,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = destColor
        )

        // ETA time: similar color but different (vibrant Sky Blue)
        Text(
            text = if (etaTime.isNotBlank()) "ETA $etaTime" else "ETA --:--",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF0284C7)
        )

        // Travel time (約xx分鐘)
        if (travelMinutes > 0) {
            Text(
                text = "約 $travelMinutes 分鐘",
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetroDepartureCard(
    shifts: List<MetroShift>,
    selectedOrigin: String = "三民高中",
    selectedDestination: String = "南勢角",
    availableStations: List<MrtStation> = emptyList(),
    onSelectOrigin: (String) -> Unit = {},
    onSelectDestination: (String) -> Unit = {},
    onResetOriginToGps: () -> Unit = {},
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("metro_favorites_pref", Context.MODE_PRIVATE)
    }

    var favoriteStations by remember {
        val saved = sharedPrefs.getStringSet("favorites", null)
        mutableStateOf(
            saved ?: setOf("台北101/世貿", "台北車站", "小南門", "南勢角", "忠孝新生", "東門", "西門")
        )
    }

    val toggleFavorite: (String) -> Unit = { stationName ->
        val updated = if (favoriteStations.contains(stationName)) {
            favoriteStations - stationName
        } else {
            favoriteStations + stationName
        }
        favoriteStations = updated
        sharedPrefs.edit().putStringSet("favorites", updated).apply()
    }

    var activePickerTarget by remember { mutableStateOf<StationPickerTarget?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Requirement 1 & 2: Transit Header with Origin (Left) -> Middle Arrow -> Destination (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Origin Station (Left) - Clickable to change origin station
                val originClean = selectedOrigin.removeSuffix("站").trim()
                val originStationObj = availableStations.find { it.name == originClean || it.name.contains(originClean) }
                val originLineColor = getStationLineColor(originStationObj?.line ?: "")

                Surface(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { activePickerTarget = StationPickerTarget.ORIGIN },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(originLineColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsSubway,
                                contentDescription = "Origin Metro",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (originClean.endsWith("站")) originClean else "${originClean}站",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                if (originStationObj != null) {
                                    Surface(
                                        color = originLineColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = originStationObj.code.split(" / ").firstOrNull() ?: originStationObj.code,
                                            color = originLineColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "出發站 • 點擊更換",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 2. Middle: Prominent Direction Indicator
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "To Destination",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 3. Destination Station (Right) - Prominently visible, yet balanced with left
                val destClean = selectedDestination.removeSuffix("站").trim()
                val destStationObj = availableStations.find { it.name == destClean || it.name.contains(destClean) }
                val destLineColor = getStationLineColor(destStationObj?.line ?: "")
                val isDestFav = favoriteStations.contains(destClean)

                Surface(
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { activePickerTarget = StationPickerTarget.DESTINATION },
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                            .background(destLineColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Destination",
                                tint = destLineColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isDestFav) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorite",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = if (destClean.endsWith("站")) destClean else "${destClean}站",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Change",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "目的地 • 點擊更換",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shifts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading Metro timetable...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val nextShift = shifts.first()

                // Primary Next Shift Countdown Banner
                val bannerBgColor = when (nextShift.status) {
                    MetroShiftStatus.DEPARTING_SOON -> Color(0xFFDC2626).copy(alpha = 0.12f)
                    MetroShiftStatus.APPROACHING -> Color(0xFFD97706).copy(alpha = 0.12f)
                    MetroShiftStatus.ON_SCHEDULE -> Color(0xFF16A34A).copy(alpha = 0.12f)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = bannerBgColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Next Train Shift (${nextShift.stationName})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (nextShift.minutesUntilDeparture == 0) "即將進站" else "${nextShift.minutesUntilDeparture}",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (nextShift.status) {
                                        MetroShiftStatus.DEPARTING_SOON -> Color(0xFFDC2626)
                                        MetroShiftStatus.APPROACHING -> Color(0xFFD97706)
                                        MetroShiftStatus.ON_SCHEDULE -> Color(0xFF16A34A)
                                    }
                                )
                                if (nextShift.minutesUntilDeparture > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "mins",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Departure: ${nextShift.departureTimeFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = when (nextShift.status) {
                                    MetroShiftStatus.DEPARTING_SOON -> Color(0xFFDC2626)
                                    MetroShiftStatus.APPROACHING -> Color(0xFFD97706)
                                    MetroShiftStatus.ON_SCHEDULE -> Color(0xFF16A34A)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = when (nextShift.status) {
                                        MetroShiftStatus.DEPARTING_SOON -> "即將進站"
                                        MetroShiftStatus.APPROACHING -> "即將抵達"
                                        MetroShiftStatus.ON_SCHEDULE -> "準點發車"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextShift.platform,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            val nextDestName = nextShift.destinationStationName.ifBlank {
                                val d = nextShift.destination.removeSuffix("站")
                                "${d}站"
                            }
                            MetroEtaBadge(
                                destStationName = nextDestName,
                                etaTime = nextShift.etaTimeFormatted,
                                travelMinutes = nextShift.travelTimeMinutes,
                                destColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Upcoming Shift List
                Text(
                    text = "後續班次 (Upcoming Shifts)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shifts.drop(1).forEach { shift ->
                        val departStationName = shift.stationName.let {
                            if (it.endsWith("站")) it else "${it}站"
                        }
                        val destStationName = shift.destinationStationName.ifBlank {
                            val d = shift.destination.removeSuffix("站")
                            "${d}站"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Row 1: Departure Time, Depart Station (xx), and "in xx mins" countdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = shift.departureTimeFormatted,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // User requirement 2: xx should be the depart station not destination
                                    Text(
                                        text = "($departStationName)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // "in xx mins" - similar color with time (onSurface) but more obvious (bold badge with outline)
                                Surface(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (shift.minutesUntilDeparture <= 0) "即將進站" else "${shift.minutesUntilDeparture} mins",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Row 2: Destination station (xx站) and ETA time with similar color but different
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MetroEtaBadge(
                                    destStationName = destStationName,
                                    etaTime = shift.etaTimeFormatted,
                                    travelMinutes = shift.travelTimeMinutes,
                                    destColor = MaterialTheme.colorScheme.primary
                                )

                                Text(
                                    text = shift.platform,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Select Origin or Destination MRT Station
    if (activePickerTarget != null) {
        val isPickingOrigin = activePickerTarget == StationPickerTarget.ORIGIN

        AlertDialog(
            onDismissRequest = { activePickerTarget = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPickingOrigin) Icons.Default.DirectionsSubway else Icons.Default.NearMe,
                        contentDescription = null,
                        tint = if (isPickingOrigin) MetroOrange else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPickingOrigin) "選擇出發捷運站 (Origin)" else "選擇目的地捷運站 (Destination)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                var searchQuery by remember { mutableStateOf("") }

                val filteredStations = remember(searchQuery, availableStations) {
                    val q = searchQuery.trim()
                    if (q.isBlank()) {
                        availableStations
                    } else {
                        val normalized = q.removeSuffix("站").removePrefix("捷運").trim()
                        availableStations.filter {
                            it.name.contains(q, ignoreCase = true) ||
                            it.name.contains(normalized, ignoreCase = true) ||
                            normalized.contains(it.name, ignoreCase = true) ||
                            it.nameEn.contains(q, ignoreCase = true) ||
                            it.code.contains(q, ignoreCase = true) ||
                            it.line.contains(q, ignoreCase = true)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    // Quick Action for Origin: Use Phone GPS Location
                    if (isPickingOrigin) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onResetOriginToGps()
                                    activePickerTarget = null
                                },
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "GPS",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📍 依照目前手機 GPS 定位自動設為最近捷運站",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Search input with quick clear
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜尋捷運站 (例如: 小南門, 台北車站, G11)") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Favorite Stations Section (only for destination mode)
                    if (!isPickingOrigin && favoriteStations.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ 常用收藏 (Favorite Stations):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "點擊星星可加/退最愛",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            favoriteStations.forEach { favName ->
                                SuggestionChip(
                                    onClick = {
                                        onSelectDestination(favName)
                                        activePickerTarget = null
                                    },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(favName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (selectedDestination == favName)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Station List
                    if (filteredStations.isEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (searchQuery.isNotBlank()) {
                                        if (isPickingOrigin) onSelectOrigin(searchQuery.trim()) else onSelectDestination(searchQuery.trim())
                                        activePickerTarget = null
                                    }
                                },
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "使用自訂站點: \"$searchQuery\"",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredStations) { station ->
                                val isSelected = if (isPickingOrigin) selectedOrigin.contains(station.name) else selectedDestination.contains(station.name)
                                val isFav = favoriteStations.contains(station.name)
                                val lineColor = getStationLineColor(station.line)

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (isPickingOrigin) {
                                                onSelectOrigin(station.name)
                                            } else {
                                                onSelectDestination(station.name)
                                            }
                                            activePickerTarget = null
                                        },
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    tonalElevation = if (isSelected) 2.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = lineColor.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = station.code,
                                                    color = lineColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = station.name,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = station.nameEn,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = "${station.line} • ${if (station.travelTimeFromO19 == 0) "首站" else "約 ${station.travelTimeFromO19} 分鐘"}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (!isPickingOrigin) {
                                            IconButton(
                                                onClick = { toggleFavorite(station.name) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                                    contentDescription = if (isFav) "Remove Favorite" else "Add Favorite",
                                                    tint = if (isFav) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activePickerTarget = null }) {
                    Text("關閉 (Close)")
                }
            }
        )
    }
}

