package com.horastrabajo.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.horastrabajo.app.HorasTrabajoApp
import com.horastrabajo.app.ui.entrada.EntradaFormViewModel
import com.horastrabajo.app.ui.mes.MesViewModel
import com.horastrabajo.app.ui.resumen.ResumenViewModel
import com.horastrabajo.app.ui.trabajos.TrabajosViewModel

/** Da acceso a la Application (y por tanto a los repositorios) dentro de un `initializer { }`. */
fun CreationExtras.horasTrabajoApp(): HorasTrabajoApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HorasTrabajoApp

/** Factory manual (sin Hilt, por simplicidad) que construye cada ViewModel a partir de los repositorios. */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            TrabajosViewModel(horasTrabajoApp().trabajoRepository, horasTrabajoApp().jsonBackupManager)
        }
        initializer {
            MesViewModel(
                trabajoRepository = horasTrabajoApp().trabajoRepository,
                entradaHorasRepository = horasTrabajoApp().entradaHorasRepository,
                tarifaMensualRepository = horasTrabajoApp().tarifaMensualRepository,
                dineroExtraRepository = horasTrabajoApp().dineroExtraRepository,
            )
        }
        initializer {
            EntradaFormViewModel(horasTrabajoApp().entradaHorasRepository)
        }
        initializer {
            ResumenViewModel(
                trabajoRepository = horasTrabajoApp().trabajoRepository,
                entradaHorasRepository = horasTrabajoApp().entradaHorasRepository,
                tarifaMensualRepository = horasTrabajoApp().tarifaMensualRepository,
                dineroExtraRepository = horasTrabajoApp().dineroExtraRepository,
            )
        }
    }
}
