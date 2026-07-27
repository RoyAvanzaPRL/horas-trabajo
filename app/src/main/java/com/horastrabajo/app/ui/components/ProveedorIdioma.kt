package com.horastrabajo.app.ui.components

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.horastrabajo.app.data.preferences.IdiomaPreferido
import java.util.Locale

/**
 * Aplica el [idioma] elegido a todo el contenido, sin reiniciar la Activity: sustituye el
 * [LocalContext] por uno con la configuración regional forzada, de modo que `stringResource`
 * resuelve contra los recursos de ese idioma y la UI se recompone al instante.
 *
 * Con [IdiomaPreferido.SISTEMA] se deja el contexto original, que es el que Android ya
 * resolvió según el idioma del dispositivo (cayendo en `values/` — español — si el idioma
 * del sistema no es ninguno de los soportados).
 *
 * El [CompositionLocalProvider] se aplica siempre, también en el caso "sistema", para que
 * cambiar de idioma sea una recomposición y no un cambio de estructura del árbol: así no se
 * pierde el estado de navegación ni el de las pantallas.
 */
@Composable
fun ProveedorIdioma(idioma: IdiomaPreferido, content: @Composable () -> Unit) {
    val contextoBase = LocalContext.current
    val etiqueta = idioma.etiquetaBcp47

    val contextoLocalizado = remember(etiqueta, contextoBase) {
        if (etiqueta == null) {
            contextoBase
        } else {
            val configuracion = Configuration(contextoBase.resources.configuration)
            configuracion.setLocales(LocaleList(Locale.forLanguageTag(etiqueta)))
            contextoBase.createConfigurationContext(configuracion)
        }
    }

    CompositionLocalProvider(
        LocalContext provides contextoLocalizado,
        LocalConfiguration provides contextoLocalizado.resources.configuration,
    ) {
        content()
    }
}

/** Idioma efectivo del contenido actual, para formatear nombres de meses y días. */
@Composable
fun localeActual(): Locale {
    val configuracion = LocalConfiguration.current
    return configuracion.locales[0] ?: Locale.getDefault()
}
