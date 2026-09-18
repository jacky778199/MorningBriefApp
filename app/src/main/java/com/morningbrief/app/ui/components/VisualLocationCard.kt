package com.morningbrief.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.morningbrief.app.model.CalendarEvent

@Composable
fun VisualLocationCard(
    event: CalendarEvent,
    onUpdateLocation: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClarifyDialog by remember { mutableStateOf(false) }
    var showUniquenessWarningDialog by remember { mutableStateOf(false) }

    if (!event.hasLocation) return

    val launchDirectNavigationWithTarget: (String?, Double?, Double?) -> Unit = { targetLoc, targetLat, targetLon ->
        val loc = targetLoc ?: event.location
        val lat = targetLat ?: event.latitude
        val lon = targetLon ?: event.longitude

        // Clean target location: normalize sections 1段 -> 一段 and strip parenthetical descriptions for navigation
        val cleanLoc = loc
            .replace(Regex("[（(][^）)]*[）)]"), "")
            .replace(Regex("(?<=[^\\d])1段|(?<=^)1段"), "一段")
            .replace(Regex("(?<=[^\\d])2段|(?<=^)2段"), "二段")
            .replace(Regex("(?<=[^\\d])3段|(?<=^)3段"), "三段")
            .replace(Regex("(?<=\\d號)\\s*\\d+[fF樓].*"), "")
            .trim()

        val navQuery = if (cleanLoc.isNotBlank()) cleanLoc else loc
        val encodedQuery = Uri.encode(navQuery)

        // If the location has a meaningful address or place name, Google Maps navigation
        // resolves it with highest accuracy via address query: google.navigation:q=ADDRESS&mode=d
        // If no address text, fallback to lat,lon.
        val navUri = if (navQuery.isNotBlank()) {
            Uri.parse("google.navigation:q=$encodedQuery&mode=d")
        } else if (lat != null && lon != null) {
            Uri.parse("google.navigation:q=$lat,$lon&mode=d")
        } else {
            Uri.parse("google.navigation:q=$encodedQuery&mode=d")
        }

        val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val webDest = if (navQuery.isNotBlank()) encodedQuery else if (lat != null && lon != null) "$lat,$lon" else encodedQuery
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$webDest")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    val launchDirectNavigation: () -> Unit = {
        launchDirectNavigationWithTarget(null, null, null)
    }

    val onNavigateToLocation: () -> Unit = {
        if (!event.isLocationUnique) {
            showUniquenessWarningDialog = true
        } else {
            launchDirectNavigation()
        }
    }

    // Location Area Container with Ambiguity Warning, Map Card, and Nearest Metro Station Line
    Column(modifier = modifier.fillMaxWidth()) {

        // Issue 4 & User Request: Noticeable banner if the address from calendar is ambiguous / not unique
        if (!event.isLocationUnique || event.isLocationAmbiguous) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showClarifyDialog = true },
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!event.isLocationUnique) "⚠️ 地址非唯一(導航會跳出選單) • 請確認" else "地點非確切專用地址 • 點此確認",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF92400E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = Color(0xFFD97706),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "修正地點",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(146.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onNavigateToLocation() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Stylized base map placeholder while loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE2E8F0),
                                    Color(0xFFCBD5E1),
                                    Color(0xFFE2E8F0)
                                )
                            )
                        )
                )

                // Real Map Background Image
                val mapUrl = event.getStaticMapUrl()
                if (mapUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mapUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Real map view of ${event.location}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Real map pin indicator anchored near center
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-14).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.95f))
                            .border(2.dp, Color(0xFFEF4444), CircleShape)
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Dark gradient scrim overlay at the bottom for high-contrast readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Bottom Info & Action Bar docked over the map
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.location,
                            color = Color.White,
                            fontSize = 12.5.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // "Clarify / Edit" Button
                    Surface(
                        onClick = { showClarifyDialog = true },
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditLocation,
                                contentDescription = "Edit Location",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "確認地點",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Interactive "Directions" Pill
                    Surface(
                        onClick = onNavigateToLocation,
                        color = Color(0xFF2563EB),
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Navigate",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "導航",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Line Below Location Area: Display the nearest Metro Station
        val nearestMetro = event.nearestMetroStation
        if (!nearestMetro.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF8961E).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Subway,
                        contentDescription = "Metro",
                        tint = Color(0xFFF8961E),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nearest Metro: $nearestMetro",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialog: Clarify / Choose Dedicated Address
    if (showClarifyDialog) {
        var editedLocation by remember(showClarifyDialog, event.location) { mutableStateOf(event.location) }

        AlertDialog(
            onDismissRequest = { showClarifyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditLocation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "確認/修正活動地點",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "活動: ${event.title}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "若行事曆地點不明確，請輸入確切地址或直接點選專屬地標，即時精準更新地圖預覽與最近捷運站：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editedLocation,
                        onValueChange = { editedLocation = it },
                        label = { Text("確切地址或專屬地標名稱") },
                        placeholder = { Text("例如: 台北101大樓, 鼎泰豐忠孝店...") },
                        trailingIcon = {
                            if (editedLocation.isNotEmpty()) {
                                IconButton(onClick = { editedLocation = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = false,
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "📍 台北熱門專屬地標推薦 (點選填入):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val landmarks = listOf(
                        "Taipei 101 Tower (台北101大樓)",
                        "鼎泰豐忠孝店 (Zhongxiao Dunhua)",
                        "內湖科技園區 (Neihu Tech Park)",
                        "台北車站微風廣場 (Taipei Main Station)",
                        "國父紀念館 (Sun Yat-Sen Memorial)",
                        "新光三越信義A11 (Xinyi Shin Kong)",
                        "三民高中 (Sanmin Senior High)",
                        "捷運小南門站 (Xiaonanmen Station)"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        landmarks.forEach { landmark ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { editedLocation = landmark },
                                color = if (editedLocation == landmark)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = landmark,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val loc = editedLocation.trim()
                        if (loc.isNotBlank()) {
                            onUpdateLocation(loc)
                        }
                        showClarifyDialog = false
                    },
                    enabled = editedLocation.isNotBlank()
                ) {
                    Text("確認使用此地點")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClarifyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Dialog: Uniqueness Warning when User Attempts to Navigate to Non-Unique Location
    if (showUniquenessWarningDialog) {
        AlertDialog(
            onDismissRequest = { showUniquenessWarningDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "⚠️ 導航注意：地點非唯一",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "此行事曆地址「${event.location}」可能包含多個相符地點（例如連鎖分店或缺少門牌號碼）。若直接導航，Google 地圖會出現多個選單讓您挑選，容易導向錯誤地點！",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (event.candidateAddresses.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "請點選以下專屬唯一地點直接直達導航：",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            event.candidateAddresses.forEach { candidate ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onUpdateLocation(candidate)
                                            showUniquenessWarningDialog = false
                                            launchDirectNavigationWithTarget(candidate, null, null)
                                        },
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = candidate,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "或點擊「修正地點」輸入完整門牌號碼以確保唯一性。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUniquenessWarningDialog = false
                        showClarifyDialog = true
                    }
                ) {
                    Text("修正確切地點")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUniquenessWarningDialog = false
                        launchDirectNavigation()
                    }
                ) {
                    Text("仍要嘗試導航")
                }
            }
        )
    }
}

