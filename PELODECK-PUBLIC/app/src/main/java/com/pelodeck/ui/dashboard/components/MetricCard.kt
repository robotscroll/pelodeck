package com.pelodeck.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pelodeck.ui.theme.PeloDeckTheme

@Composable
fun MetricCard(label: String, value: String, unit: String, progress: Float? = null,
               progressColor: Color? = null, subtitle: String? = null, footer: String? = null, modifier: Modifier = Modifier) {
    val colors = PeloDeckTheme.colors
    val animatedProgress by animateFloatAsState(progress?.coerceIn(0f, 1f) ?: 0f,
        animationSpec = tween(400), label = "MetricProgress")
    BoxWithConstraints(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        .background(colors.surface).border(1.dp, colors.panelBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 7.dp)) {
        val fontScale = LocalDensity.current.fontScale
        val unitWidth = unit.length * 8f + 10f
        val valueSize = minOf(56f, (maxWidth.value - unitWidth) / (value.length.coerceAtLeast(1) * 0.66f) / fontScale).coerceAtLeast(22f)
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label.uppercase(), color = colors.textSecondary, fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(4.dp))
                if (subtitle != null) Text(subtitle, color = colors.accent, fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(value, color = colors.digitalDisplay, fontSize = valueSize.sp,
                    lineHeight = (valueSize * 1.1f).sp, fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold, maxLines = 1, softWrap = false,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(unit.uppercase(), color = colors.secondary, fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp), maxLines = 1)
            }
            if (footer != null) Text(footer, color = colors.textSecondary, fontSize = 10.sp,
                fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (progress != null) {
                LinearProgressIndicator(progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = progressColor ?: colors.primary, trackColor = colors.surfaceVariant)
            }
        }
    }
}
