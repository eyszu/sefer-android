package app.sefer.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.sefer.R

/** The four reading themes. */
enum class SeferPalette(
    val key: String,
    val label: String,
    val isLight: Boolean,
    val bg: Color,
    val surface: Color,
    val container: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val outline: Color,
) {
    LIGHT(
        "light", "Light", true,
        bg = Color(0xFFF8F4EA), surface = Color(0xFFFFFDF8), container = Color(0xFFEDE6D3),
        text = Color(0xFF211D17), muted = Color(0xFF6F675B), accent = Color(0xFF9A6B00),
        outline = Color(0xFFD9CFB6),
    ),
    DARK(
        "dark", "Dark", false,
        bg = Color(0xFF111111), surface = Color(0xFF1B1A18), container = Color(0xFF2A2825),
        text = Color(0xFFE8E3D8), muted = Color(0xFFA39C8E), accent = Color(0xFFE3B341),
        outline = Color(0xFF3A3731),
    ),
    DUSK(
        "dusk", "Dusk", false,
        bg = Color(0xFF1F2336), surface = Color(0xFF292E47), container = Color(0xFF353B5C),
        text = Color(0xFFDDE0F0), muted = Color(0xFF9AA0BE), accent = Color(0xFFE8B86A),
        outline = Color(0xFF454C73),
    ),
    DAWN(
        "dawn", "Dawn", true,
        bg = Color(0xFFFBEBDD), surface = Color(0xFFFFF5EC), container = Color(0xFFF4D9C3),
        text = Color(0xFF3A2A26), muted = Color(0xFF8A6F63), accent = Color(0xFFB5502B),
        outline = Color(0xFFE8C9B0),
    );

    companion object {
        fun fromKey(key: String?): SeferPalette? = entries.firstOrNull { it.key == key }
    }
}

/** The device's own font (default) plus six bundled reading fonts (res/font). */
enum class SeferFont(val key: String, val label: String, val family: FontFamily) {
    SYSTEM("system", "System default", FontFamily.Default),
    LITERATA(
        "literata", "Literata",
        FontFamily(
            Font(R.font.literata_regular, FontWeight.Normal),
            Font(R.font.literata_bold, FontWeight.Bold),
        ),
    ),
    LORA(
        "lora", "Lora",
        FontFamily(
            Font(R.font.lora_regular, FontWeight.Normal),
            Font(R.font.lora_bold, FontWeight.Bold),
        ),
    ),
    MERRIWEATHER(
        "merriweather", "Merriweather",
        FontFamily(
            Font(R.font.merriweather_regular, FontWeight.Normal),
            Font(R.font.merriweather_bold, FontWeight.Bold),
        ),
    ),
    FRANK_RUHL_LIBRE(
        "frank_ruhl_libre", "Frank Ruhl Libre",
        FontFamily(
            Font(R.font.frank_ruhl_libre_regular, FontWeight.Normal),
            Font(R.font.frank_ruhl_libre_bold, FontWeight.Bold),
        ),
    ),
    INTER(
        "inter", "Inter",
        FontFamily(
            Font(R.font.inter_regular, FontWeight.Normal),
            Font(R.font.inter_bold, FontWeight.Bold),
        ),
    ),
    ATKINSON(
        "atkinson", "Atkinson",
        FontFamily(
            Font(R.font.atkinson_regular, FontWeight.Normal),
            Font(R.font.atkinson_bold, FontWeight.Bold),
        ),
    );

    companion object {
        fun fromKey(key: String?): SeferFont = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

private fun SeferPalette.colorScheme(): ColorScheme {
    val base = if (isLight) lightColorScheme() else darkColorScheme()
    val accentContainer = accent.copy(alpha = 0.18f).compositeOver(bg)
    return base.copy(
        primary = accent,
        onPrimary = if (isLight) Color.White else bg,
        primaryContainer = accentContainer,
        onPrimaryContainer = text,
        secondary = accent,
        onSecondary = if (isLight) Color.White else bg,
        secondaryContainer = accentContainer,
        onSecondaryContainer = text,
        background = bg,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = container,
        onSurfaceVariant = muted,
        surfaceTint = accent,
        outline = outline,
        outlineVariant = outline,
        surfaceContainerLowest = bg,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = container,
        surfaceContainerHighest = container,
    )
}

@Composable
fun SeferTheme(palette: SeferPalette, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = palette.colorScheme(), content = content)
}
