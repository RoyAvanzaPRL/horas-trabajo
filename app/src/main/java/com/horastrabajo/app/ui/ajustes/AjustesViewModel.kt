package com.horastrabajo.app.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horastrabajo.app.data.export.JsonBackupManager
import com.horastrabajo.app.data.preferences.TemaPreferido
import com.horastrabajo.app.data.preferences.ThemePreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val jsonBackupManager: JsonBackupManager,
) : ViewModel() {

    val temaPreferido: StateFlow<TemaPreferido> = themePreferenceRepository.temaPreferido
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemaPreferido.SISTEMA)

    fun cambiarTema(tema: TemaPreferido) {
        viewModelScope.launch { themePreferenceRepository.guardarTema(tema) }
    }

    /** Backup completo (todos los trabajos) en JSON. */
    fun exportarBackup(onListo: (String) -> Unit) {
        viewModelScope.launch { onListo(jsonBackupManager.exportarTodo()) }
    }

    fun importarBackup(contenidoJson: String, onCompletado: () -> Unit = {}) {
        viewModelScope.launch {
            jsonBackupManager.importarTodo(contenidoJson)
            onCompletado()
        }
    }
}
