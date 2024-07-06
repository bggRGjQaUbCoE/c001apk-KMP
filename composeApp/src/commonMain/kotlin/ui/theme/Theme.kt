package ui.theme

import androidx.annotation.FloatRange
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import constant.Constants.seedColors
import logic.datastore.ThemeType

/**
 * Created by bggRGjQaUbCoE on 2024/6/29
 */

enum class ColorSchemeMode {
    LIGHT,
    DARK,
    BLACK
}

@Composable
fun cardBg() = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)

@Composable
fun C001apkKMPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeType: ThemeType = ThemeType.Default,
    seedColor: String? = null,
    materialYou: Boolean = true,
    pureBlack: Boolean = false,
    fontScale: Float = 1.00f,
    contentScale: Float = 1.00f,
    content: @Composable () -> Unit
) {

    val colorSchemeMode =
        when (darkTheme) {
            true -> when (pureBlack) {
                true -> ColorSchemeMode.BLACK
                false -> ColorSchemeMode.DARK
            }

            false -> ColorSchemeMode.LIGHT
        }

    val color = Color(
        seedColors.getOrNull(ThemeType.entries.indexOf(themeType))
            ?: "FF$seedColor".toLongOrNull(16) ?: seedColors[0]
    )
    val colorScheme = when (colorSchemeMode) {
        ColorSchemeMode.LIGHT ->
            rememberDynamicColorScheme(color, false)

        ColorSchemeMode.DARK ->
            rememberDynamicColorScheme(color, true)

        ColorSchemeMode.BLACK ->
            rememberDynamicColorScheme(color, true).toAmoled()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
                LocalDensity provides Density(
                    LocalDensity.current.density * contentScale,
                    LocalDensity.current.fontScale * fontScale,
                )
            ) {
                content()
            }
        }
    )

}

fun Color.darken(fraction: Float = 0.5f): Color =
    Color(toArgb().blend(Color.Black.toArgb(), fraction))

fun Int.blend(
    color: Int,
    @FloatRange(from = 0.0, to = 1.0) fraction: Float = 0.5f,
): Int = blendARGB(this, color, fraction)

fun blendARGB(color1: Int, color2: Int, fraction: Float): Int {
    // Extract ARGB components from color1
    val alpha1 = (color1 shr 24) and 0xFF
    val red1 = (color1 shr 16) and 0xFF
    val green1 = (color1 shr 8) and 0xFF
    val blue1 = color1 and 0xFF

    // Extract ARGB components from color2
    val alpha2 = (color2 shr 24) and 0xFF
    val red2 = (color2 shr 16) and 0xFF
    val green2 = (color2 shr 8) and 0xFF
    val blue2 = color2 and 0xFF

    // Calculate blended alpha
    val alphaBlended = (alpha1 * (1 - fraction) + alpha2 * fraction).toInt()

    // Blend red, green, and blue components
    val redBlended = (red1 * (1 - fraction) + red2 * fraction).toInt()
    val greenBlended = (green1 * (1 - fraction) + green2 * fraction).toInt()
    val blueBlended = (blue1 * (1 - fraction) + blue2 * fraction).toInt()

    // Combine blended components back into a single ARGB value
    return (alphaBlended shl 24) or (redBlended shl 16) or (greenBlended shl 8) or blueBlended
}

fun ColorScheme.toAmoled(): ColorScheme {
    return copy(
        primary = primary.darken(0.3f),
        onPrimary = onPrimary.darken(0.3f),
        primaryContainer = primaryContainer.darken(0.3f),
        onPrimaryContainer = onPrimaryContainer.darken(0.3f),
        inversePrimary = inversePrimary.darken(0.3f),
        secondary = secondary.darken(0.3f),
        onSecondary = onSecondary.darken(0.3f),
        secondaryContainer = secondaryContainer.darken(0.3f),
        onSecondaryContainer = onSecondaryContainer.darken(0.3f),
        tertiary = tertiary.darken(0.3f),
        onTertiary = onTertiary.darken(0.3f),
        tertiaryContainer = tertiaryContainer.darken(0.3f),
        onTertiaryContainer = onTertiaryContainer.darken(0.2f),
        background = Color.Black,
        onBackground = onBackground.darken(0.15f),
        surface = Color.Black,
        onSurface = onSurface.darken(0.15f),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface.darken(),
        inverseOnSurface = inverseOnSurface.darken(0.2f),
        outline = outline.darken(0.2f),
        outlineVariant = outlineVariant.darken(0.2f),
        surfaceContainer = surfaceContainer.darken(),
        surfaceContainerHigh = surfaceContainerHigh.darken(),
        surfaceContainerHighest = surfaceContainerHighest.darken(0.4f),
    )
}