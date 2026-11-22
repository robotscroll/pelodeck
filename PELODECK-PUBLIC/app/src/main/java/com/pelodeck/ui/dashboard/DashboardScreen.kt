package com.pelodeck.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.dashboard.components.FullscreenRideOverlay
import com.pelodeck.ui.dashboard.components.BottomBar
import com.pelodeck.ui.dashboard.components.CenterBrowserPanel
import com.pelodeck.ui.dashboard.components.LeftTelemetryPanel
import com.pelodeck.ui.dashboard.components.RightTelemetryPanel
import com.pelodeck.ui.dashboard.components.TopBar
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun DashboardScreen(
    currentSkin: ThemeSkin,
    onSkinSelected: (ThemeSkin) -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val activeRide by viewModel.activeRide.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val preferredUnits by viewModel.preferredUnits.collectAsState()

    var fullscreenView by remember { mutableStateOf<View?>(null) }
    var exitFullscreen by remember { mutableStateOf<(() -> Unit)?>(null) }
    var classRequest by remember { mutableStateOf<Pair<Int, String>?>(null) }
    val colors = PeloDeckTheme.colors

    // Ensure ride is automatically started whenever we enter PeloDeck
    LaunchedEffect(Unit) {
        viewModel.ensureRideStarted()
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top System / Status / Profile Bar
        TopBar(
            activeProfile = activeProfile,
            currentSkin = currentSkin,
            telemetry = telemetry,
            onSkinSelected = onSkinSelected,
            onClassSearch = { query -> classRequest = ((classRequest?.first ?: 0) + 1) to query },
            onOpenProfiles = onNavigateToProfiles,
            onOpenHistory = onNavigateToHistory,
            onOpenDiagnostics = onNavigateToDiagnostics
        )

        // Three-Column Landscape Cockpit
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Telemetry Column (Cadence, Power, Distance)
            LeftTelemetryPanel(
                telemetry = telemetry,
                preferredUnits = preferredUnits,
                modifier = Modifier
                    .weight(0.19f)
                    .fillMaxSize()
            )

            // Center Column (Embedded Chromium Browser + Fullscreen HTML5 Video)
            CenterBrowserPanel(
                bookmarks = bookmarks,
                classRequest = classRequest,
                onFullscreenChanged = { view, exit -> fullscreenView = view; exitFullscreen = exit },
                onAddBookmark = { title, url -> viewModel.addBookmark(title, url) },
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxSize()
            )

            // Right Telemetry Column (Resistance, Est. Calories, Duration)
            RightTelemetryPanel(
                telemetry = telemetry,
                activeRide = activeRide,
                modifier = Modifier
                    .weight(0.19f)
                    .fillMaxSize()
            )
        }

        // Bottom Controls Bar
        BottomBar(
            activeRide = activeRide,
            onStartRide = { viewModel.startRide() },
            onPauseRide = { viewModel.pauseRide() },
            onResumeRide = { viewModel.resumeRide() },
            onFinishRide = { viewModel.finishRide() },
            onOpenHistory = onNavigateToHistory,
            onSwitchRider = onNavigateToProfiles
        )
    }
    fullscreenView?.let { video ->
        FullscreenRideOverlay(video = video, telemetry = telemetry, ride = activeRide, onExit = { exitFullscreen?.invoke() })
    }
}
}
