package com.pelodeck.ui.dashboard.components

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pelodeck.data.repository.ActiveRideState
import com.pelodeck.domain.model.BikeTelemetry
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun FullscreenRideOverlay(video: View, telemetry: BikeTelemetry, ride: ActiveRideState, onExit: () -> Unit) {
    val context = LocalContext.current
    var clock by remember { mutableStateOf("") }
    LaunchedEffect(context) {
        while (true) {
            clock = android.text.format.DateFormat.getTimeFormat(context).format(Date())
            delay(1000)
        }
    }
    DisposableEffect(video) {
        onDispose { (video.parent as? ViewGroup)?.removeView(video) }
    }
    val elapsed = ride.elapsedSeconds.coerceAtLeast(0)
    val duration = if (elapsed >= 3600) String.format("%d:%02d:%02d", elapsed / 3600, (elapsed / 60) % 60, elapsed % 60)
        else String.format("%02d:%02d", elapsed / 60, elapsed % 60)
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { ctx ->
            FrameLayout(ctx).apply {
                (video.parent as? ViewGroup)?.removeView(video)
                addView(video, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }, modifier = Modifier.fillMaxSize())
        IconButton(onClick = onExit, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).background(Color.Black.copy(alpha = .65f))) {
            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit fullscreen", tint = Color.White)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = .78f))) {
            if (ride.isSimulated) Text("SIMULATED RIDE DATA", color = Color(0xFFFFC46B), fontSize = 10.sp, modifier = Modifier.padding(start = 12.dp, top = 3.dp))
            Row(Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                RibbonMetric("CADENCE", telemetry.cadenceRpm?.let { String.format("%.0f", it) } ?: "--", "RPM", Modifier.weight(1f))
                RibbonMetric("RESISTANCE", telemetry.resistancePercent?.let { String.format("%.0f", it) } ?: "--", "%", Modifier.weight(1f))
                RibbonMetric("OUTPUT", telemetry.powerWatts?.let { String.format("%.0f", it) } ?: "--", "W", Modifier.weight(1f))
                RibbonMetric("DURATION", duration, "", Modifier.weight(1f))
                RibbonMetric("CLOCK", clock, "", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RibbonMetric(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color(0xFFB7C3C7), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Color.White, fontSize = 22.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            if (unit.isNotEmpty()) Text(" $unit", color = Color(0xFF8DE7E3), fontSize = 10.sp, modifier = Modifier.padding(bottom = 3.dp), maxLines = 1)
        }
        Canvas(Modifier.fillMaxWidth(.65f).height(7.dp)) {
            for (i in 0..10) {
                val x = size.width * i / 10f
                val start = if (i % 5 == 0) 0f else size.height * .45f
                drawLine(Color(0xFF89AAA8), Offset(x, start), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            }
        }
    }
}
