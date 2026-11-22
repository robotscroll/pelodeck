package com.pelodeck.ui.launcher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.provider.Settings
import android.widget.ImageView
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pelodeck.WhiskersActivity
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun LauncherScreen(onOpenPeloDeck: () -> Unit) {
    val context = LocalContext.current
    val colors = PeloDeckTheme.colors
    var error by remember { mutableStateOf<String?>(null) }
    var apps by remember { mutableStateOf(emptyList<ResolveInfo>()) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                apps = context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                    .filter { it.activityInfo.packageName != context.packageName }
                    .sortedBy { it.loadLabel(context.packageManager).toString().lowercase() }
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    fun launch(intent: Intent) {
        error = if (runCatching { context.startActivity(intent) }.isSuccess) null else "Could not open this app."
    }
    Column(Modifier.fillMaxSize().background(colors.background).padding(horizontal = 28.dp, vertical = 22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("PELODECK", color = colors.primary, fontSize = 28.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("HOME", color = colors.textSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { launch(Intent(Settings.ACTION_HOME_SETTINGS)) }, border = androidx.compose.foundation.BorderStroke(1.dp, colors.panelBorder), colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)) { Text("CHOOSE HOME APP") }
                Button(onClick = { launch(Intent(Settings.ACTION_SETTINGS)) }, colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background)) { Text("SYSTEM SETTINGS") }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (error != null) { Text(error!!, color = colors.secondary); Spacer(Modifier.height(8.dp)) }
        LazyVerticalGrid(columns = GridCells.Adaptive(190.dp), modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                LauncherTile("PeloDeck", onOpenPeloDeck) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsBike, null, tint = colors.primary, modifier = Modifier.size(48.dp))
                }
            }
            item {
                LauncherTile("Whiskers & the Ball", { launch(Intent(context, WhiskersActivity::class.java)) }) {
                    Icon(Icons.Default.Pets, null, tint = colors.primary, modifier = Modifier.size(48.dp))
                }
            }
            items(apps, key = { "${it.activityInfo.packageName}/${it.activityInfo.name}" }) { app: ResolveInfo ->
                LauncherTile(app.loadLabel(context.packageManager).toString(), {
                    launch(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                        .setClassName(app.activityInfo.packageName, app.activityInfo.name)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }) {
                    AndroidView(factory = { ctx -> ImageView(ctx).apply { setImageDrawable(app.loadIcon(ctx.packageManager)) } },
                        modifier = Modifier.size(48.dp))
                }
            }
        }
    }
}

@Composable
private fun LauncherTile(label: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    val colors = PeloDeckTheme.colors
    Column(Modifier.fillMaxWidth().height(155.dp).clip(RoundedCornerShape(14.dp))
        .background(colors.surface).border(1.dp, colors.panelBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(Modifier.height(12.dp))
        Text(label, color = colors.textPrimary, fontWeight = FontWeight.SemiBold,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
