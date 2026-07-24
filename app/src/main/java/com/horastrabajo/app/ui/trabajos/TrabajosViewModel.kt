package com.horastrabajo.app.ui.trabajos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horastrabajo.app.data.export.JsonBackupManager
import com.horastrabajo.app.data.repository.TrabajoRepository
import com.horastrabajo.app.domain.model.Trabajo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrabajosViewModel(
    private val trabajoRepository: TrabajoRepository,
    private val jsonBackupManager: JsonBackupManager,
) : ViewModel() {

    val trabajos: StateFlow<List<Trabajo>> = trabajoRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun crearTrabajo(nombre: String, simboloMoneda: String) {
        if (nombre.isBlank()) return
        viewModelScope.launch {
            trabajoRepository.guardar(Trabajo(nombre = nombre.trim(), simboloMoneda = simboloMoneda.trim().ifBlank { "€" }))
        }
    }

    fun eliminarTrabajo(trabajo: Trabajo) {
        viewModelScope.launch { trabajoRepository.eliminar(trabajo) }
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
