package com.horastrabajo.app.ui.mes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.horastrabajo.app.data.repository.DineroExtraRepository
import com.horastrabajo.app.data.repository.EntradaHorasRepository
import com.horastrabajo.app.data.repository.TarifaMensualRepository
import com.horastrabajo.app.data.repository.TrabajoRepository
import com.horastrabajo.app.domain.ResumenMensual
import com.horastrabajo.app.domain.ResumenMensualCalculator
import com.horastrabajo.app.domain.model.Dinero
import com.horastrabajo.app.domain.model.DineroExtra
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class Parametros(val trabajoId: Long, val anio: Int, val mes: Int)

class MesViewModel(
    private val trabajoRepository: TrabajoRepository,
    private val entradaHorasRepository: EntradaHorasRepository,
    private val tarifaMensualRepository: TarifaMensualRepository,
    private val dineroExtraRepository: DineroExtraRepository,
) : ViewModel() {

    private val parametros = MutableStateFlow<Parametros?>(null)
    private val tarifaVigente = MutableStateFlow<Dinero?>(null)

    fun cargar(trabajoId: Long, anio: Int, mes: Int) {
        val actuales = parametros.value
        if (actuales?.trabajoId == trabajoId && actuales.anio == anio && actuales.mes == mes) return
        parametros.value = Parametros(trabajoId, anio, mes)
        refrescarTarifa()
    }

    private fun refrescarTarifa() {
        val p = parametros.value ?: return
        viewModelScope.launch {
            tarifaVigente.value = tarifaMensualRepository.obtenerTarifaVigente(p.trabajoId, p.anio, p.mes)?.precioPorHora
        }
    }

    fun fijarTarifaDelMes(precioPorHora: Dinero) {
        val p = parametros.value ?: return
        viewModelScope.launch {
            tarifaMensualRepository.fijarTarifaDelMes(p.trabajoId, p.anio, p.mes, precioPorHora)
            refrescarTarifa()
        }
    }

    fun agregarDineroExtra(dineroExtra: DineroExtra) {
        viewModelScope.launch { dineroExtraRepository.guardar(dineroExtra) }
    }

    fun eliminarDineroExtra(dineroExtra: DineroExtra) {
        viewModelScope.launch { dineroExtraRepository.eliminar(dineroExtra) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val resumen: StateFlow<ResumenMensual?> = combine(
        parametros.filterNotNull(),
        parametros.filterNotNull().flatMapLatest { trabajoRepository.observeById(it.trabajoId) },
        parametros.filterNotNull().flatMapLatest { entradaHorasRepository.observePorMes(it.trabajoId, it.anio, it.mes) },
        parametros.filterNotNull().flatMapLatest { dineroExtraRepository.observePorMes(it.trabajoId, it.anio, it.mes) },
        tarifaVigente,
    ) { p, trabajo, entradas, dineroExtra, tarifa ->
        if (trabajo == null) null else ResumenMensualCalculator.calcular(
            trabajo = trabajo,
            anio = p.anio,
            mes = p.mes,
            precioPorHora = tarifa,
            entradas = entradas,
            dineroExtra = dineroExtra,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
