package com.pelodeck

import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.navigation.PeloDeckNavHost
import com.pelodeck.ui.theme.PeloDeckTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onPause() {
        CookieManager.getInstance().flush()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive full-screen mode for dedicated bike display
        enableImmersiveMode()

        setContent {
            val prefRepo = (application as PeloDeckApp).preferencesRepository
            val currentSkin by prefRepo.selectedSkin.collectAsState(initial = ThemeSkin.WINAMP)
            val scope = rememberCoroutineScope()

            PeloDeckTheme(skin = currentSkin) {
                PeloDeckNavHost(
                    currentSkin = currentSkin,
                    onSkinSelected = { skin ->
                        scope.launch {
                            prefRepo.setSelectedSkin(skin)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableImmersiveMode()
        }
    }

    /**
     * Hides system status bar, navigation bar, and enables swipe-to-reveal transient bars
     * to prevent interference with bike telemetry touch targets.
     */
    private fun enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
