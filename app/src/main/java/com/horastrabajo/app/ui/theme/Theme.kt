package com.horastrabajo.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Verde40,
    primaryContainer = PrimarioContenedorClaro,
    onPrimaryContainer = OnPrimarioContenedorClaro,
    secondary = VerdeGris40,
    secondaryContainer = SecundarioContenedorClaro,
    onSecondaryContainer = OnSecundarioContenedorClaro,
    background = FondoClaro,
    surface = SuperficieClara,
    surfaceVariant = SuperficieVarianteClara,
)

private val DarkColors = darkColorScheme(
    primary = Verde80,
    primaryContainer = PrimarioContenedorOscuro,
    onPrimaryContainer = OnPrimarioContenedorOscuro,
    secondary = VerdeGris80,
    secondaryContainer = SecundarioContenedorOscuro,
    onSecondaryContainer = OnSecundarioContenedorOscuro,
    background = FondoOscuro,
    surface = SuperficieOscura,
    surfaceVariant = SuperficieVarianteOscura,
)

@Composable
fun HorasTrabajoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
