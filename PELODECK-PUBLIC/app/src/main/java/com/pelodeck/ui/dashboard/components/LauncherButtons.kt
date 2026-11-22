package com.pelodeck.ui.dashboard.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.pelodeck.ui.theme.PeloDeckTheme
import com.pelodeck.WhiskersActivity

@Composable
fun LauncherButtons() {
    val context = LocalContext.current
    val colors = PeloDeckTheme.colors
    var showApps by remember { mutableStateOf(false) }
    TextButton(onClick = { context.startActivity(Intent(context, WhiskersActivity::class.java)) }) {
        Icon(Icons.Default.Pets, "Whiskers and the Ball", tint = colors.primary)
        Text("WHISKERS", color = colors.primary)
    }
    IconButton(onClick = { showApps = true }) { Icon(Icons.Default.Apps, "Apps", tint = colors.primary) }
    IconButton(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_SETTINGS)) } }) {
        Icon(Icons.Default.Settings, "System settings", tint = colors.primary)
    }
    if (showApps) {
        val pm = context.packageManager
        val apps = remember { pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .sortedBy { it.loadLabel(pm).toString().lowercase() } }
        AlertDialog(onDismissRequest = { showApps = false }, title = { Text("Apps") },
            text = { LazyColumn { items(apps) { app ->
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(app.activityInfo.packageName, app.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(intent) }
                    showApps = false
                }) { Text(app.loadLabel(pm).toString()) }
            } } },
            confirmButton = { TextButton(onClick = {
                runCatching { context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
                showApps = false
            }) { Text("Choose home app") } },
            dismissButton = { TextButton(onClick = { showApps = false }) { Text("Close") } })
    }
}
