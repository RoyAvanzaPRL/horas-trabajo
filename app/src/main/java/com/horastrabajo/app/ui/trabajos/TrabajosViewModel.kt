package com.horastrabajo.app.ui.trabajos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horastrabajo.app.data.repository.TrabajoRepository
import com.horastrabajo.app.domain.model.Trabajo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrabajosViewModel(private val trabajoRepository: TrabajoRepository) : ViewModel() {

    val trabajos: StateFlow<List<Trabajo>> = trabajoRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun crearTrabajo(nombre: String, nombreUsuario: String, simboloMoneda: String) {
        if (nombre.isBlank() || nombreUsuario.isBlank()) return
        viewModelScope.launch {
            trabajoRepository.guardar(
                Trabajo(
                    nombre = nombre.trim(),
                    nombreUsuario = nombreUsuario.trim(),
                    simboloMoneda = simboloMoneda.trim().ifBlank { "€" },
                )
            )
        }
    }

    fun actualizarTrabajo(trabajo: Trabajo, nombre: String, nombreUsuario: String, simboloMoneda: String) {
        if (nombre.isBlank() || nombreUsuario.isBlank()) return
        viewModelScope.launch {
            trabajoRepository.guardar(
                trabajo.copy(
                    nombre = nombre.trim(),
                    nombreUsuario = nombreUsuario.trim(),
                    simboloMoneda = simboloMoneda.trim().ifBlank { "€" },
                )
            )
        }
    }

    fun eliminarTrabajo(trabajo: Trabajo) {
        viewModelScope.launch { trabajoRepository.eliminar(trabajo) }
    }
}
