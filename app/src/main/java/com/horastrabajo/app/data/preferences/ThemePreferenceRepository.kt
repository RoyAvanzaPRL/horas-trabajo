package com.horastrabajo.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

enum class TemaPreferido {
    SISTEMA,
    CLARO,
    OSCURO,
}

private val Context.ajustesDataStore by preferencesDataStore(name = "ajustes")
private val CLAVE_TEMA = stringPreferencesKey("tema_preferido")

class ThemePreferenceRepository(private val context: Context) {

    val temaPreferido = context.ajustesDataStore.data.map { prefs ->
        prefs[CLAVE_TEMA]?.let { runCatching { TemaPreferido.valueOf(it) }.getOrNull() } ?: TemaPreferido.SISTEMA
    }

    suspend fun guardarTema(tema: TemaPreferido) {
        context.ajustesDataStore.edit { prefs -> prefs[CLAVE_TEMA] = tema.name }
    }
}
