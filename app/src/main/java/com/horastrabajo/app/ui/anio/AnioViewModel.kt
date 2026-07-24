package com.horastrabajo.app.ui.anio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horastrabajo.app.data.repository.TrabajoRepository
import com.horastrabajo.app.domain.model.Trabajo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class AnioViewModel(private val trabajoRepository: TrabajoRepository) : ViewModel() {

    private val trabajoId = MutableStateFlow<Long?>(null)

    fun cargar(id: Long) {
        trabajoId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val trabajo: StateFlow<Trabajo?> = trabajoId.filterNotNull()
        .flatMapLatest { trabajoRepository.observeById(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
