package com.pelodeck.domain.model

enum class ThemeSkin(
    val key: String,
    val displayName: String,
    val description: String
) {
    WINAMP(
        key = "winamp",
        displayName = "Classic Winamp",
        description = "Charcoal brushed metal, amber/green LED readouts, retro media deck"
    ),
    CYBERPUNK(
        key = "cyberpunk",
        displayName = "Cyberpunk",
        description = "High-voltage neon yellow, cyan, and magenta HUD cockpit"
    ),
    MINIMAL(
        key = "minimal",
        displayName = "Minimal",
        description = "Crisp monochrome dark slate with ultra-clean typography"
    ),
    MATRIX(
        key = "matrix",
        displayName = "Matrix",
        description = "Phosphor green glyphs on deep terminal black"
    ),
    CINNAMOROLL(
        key = "cinnamoroll",
        displayName = "Cinnamoroll",
        description = "Sanrio pastel baby blue and fluffy cloud white"
    );

    companion object {
        fun fromKey(key: String?): ThemeSkin {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: WINAMP
        }
    }
}
