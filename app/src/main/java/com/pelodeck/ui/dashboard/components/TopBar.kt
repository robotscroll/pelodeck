package com.pelodeck.ui.dashboard.components

import android.content.Intent
import android.provider.Settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Pets
import com.pelodeck.WhiskersActivity
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pelodeck.data.local.entity.RiderProfile
import com.pelodeck.domain.model.BikeTelemetry
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.theme.PeloDeckTheme
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun TopBar(
    activeProfile: RiderProfile?,
    currentSkin: ThemeSkin,
    telemetry: BikeTelemetry,
    onSkinSelected: (ThemeSkin) -> Unit,
    onClassSearch: (String) -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PeloDeckTheme.colors
    val context = LocalContext.current
    var clockTime by remember { mutableStateOf("") }
    LaunchedEffect(context) {
        while (true) {
            clockTime = android.text.format.DateFormat.getTimeFormat(context).format(Date())
            delay(1000)
        }
    }
    var skinMenuExpanded by remember { mutableStateOf(false) }
    var appsOpen by remember { mutableStateOf(false) }
    var classOpen by remember { mutableStateOf(false) }
    if (classOpen) ClassSelectDialog(onDismiss = { classOpen = false }, onSearch = { query ->
        classOpen = false
        onClassSearch(query)
    })
    if (appsOpen) {
        val manager = context.packageManager
        val apps = remember {
            manager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                .filter { it.activityInfo.packageName != context.packageName }
                .sortedBy { it.loadLabel(manager).toString().lowercase() }
        }
        AlertDialog(onDismissRequest = { appsOpen = false }, title = { Text("Apps") },
            text = {
                LazyColumn {
                    items(apps) { app ->
                        TextButton(onClick = {
                            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                                .setClassName(app.activityInfo.packageName, app.activityInfo.name)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(intent) }
                            appsOpen = false
                        }) { Text(app.loadLabel(manager).toString()) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = {
                runCatching { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
                appsOpen = false
            }) { Text("Choose home app") } },
            dismissButton = { TextButton(onClick = { appsOpen = false }) { Text("Close") } })
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.barBackground)
            .border(width = 1.dp, color = colors.panelBorder)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Logo & Tag
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (telemetry.isConnected) colors.primary else colors.secondary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PELODECK",
                color = colors.primary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 2.sp
            )
        }

        Text(
            text = clockTime,
            color = colors.primary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        // Right side: Profile, Skin Selector, Diagnostics, History
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { context.startActivity(Intent(context, WhiskersActivity::class.java)) }) {
                Icon(Icons.Default.Pets, contentDescription = "Whiskers and the Ball", tint = colors.primary)
            }
            IconButton(onClick = { appsOpen = true }) {
                Icon(Icons.Default.Apps, contentDescription = "Apps", tint = colors.primary)
            }
            IconButton(onClick = {
                runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) }
            }) {
                Icon(Icons.Default.Settings, contentDescription = "System settings", tint = colors.primary)
            }
            // Profile Pill Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.panelBorder, RoundedCornerShape(16.dp))
                    .clickable { onOpenProfiles() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activeProfile?.name ?: "Rider",
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = { classOpen = true }) {
                Text("CLASS SELECT", color = colors.primary, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
            }

            // Skin Selector Dropdown
            Box {
                IconButton(
                    onClick = { skinMenuExpanded = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Theme Skin",
                        tint = colors.accent
                    )
                }

                DropdownMenu(
                    expanded = skinMenuExpanded,
                    onDismissRequest = { skinMenuExpanded = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    ThemeSkin.entries.forEach { skin ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = skin.displayName,
                                        color = if (skin == currentSkin) colors.primary else colors.textPrimary,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (skin == currentSkin) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = skin.description,
                                        color = colors.textSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            onClick = {
                                onSkinSelected(skin)
                                skinMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Workout History
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Ride History",
                    tint = colors.textPrimary
                )
            }


        }
    }
}
