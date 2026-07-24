package com.horastrabajo.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.horastrabajo.app.ui.mes.MesScreen
import com.horastrabajo.app.ui.resumen.ResumenScreen
import com.horastrabajo.app.ui.trabajos.TrabajosScreen
import java.time.YearMonth

private const val RUTA_TRABAJOS = "trabajos"
private const val RUTA_MES = "mes/{trabajoId}/{anio}/{mes}"
private const val RUTA_RESUMEN = "resumen/{trabajoId}/{anio}/{mes}"

private fun rutaMes(trabajoId: Long, anio: Int, mes: Int) = "mes/$trabajoId/$anio/$mes"
private fun rutaResumen(trabajoId: Long, anio: Int, mes: Int) = "resumen/$trabajoId/$anio/$mes"

@Composable
fun HorasTrabajoNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = RUTA_TRABAJOS) {
        composable(RUTA_TRABAJOS) {
            TrabajosScreen(
                onTrabajoSeleccionado = { trabajoId ->
                    val hoy = YearMonth.now()
                    navController.navigate(rutaMes(trabajoId, hoy.year, hoy.monthValue))
                },
            )
        }
        composable(RUTA_MES) { backStackEntry ->
            val trabajoId = backStackEntry.arguments?.getString("trabajoId")?.toLongOrNull() ?: return@composable
            val anio = backStackEntry.arguments?.getString("anio")?.toIntOrNull() ?: return@composable
            val mes = backStackEntry.arguments?.getString("mes")?.toIntOrNull() ?: return@composable
            MesScreen(
                trabajoId = trabajoId,
                anio = anio,
                mes = mes,
                onCambiarMes = { nuevoAnio, nuevoMes ->
                    navController.navigate(rutaMes(trabajoId, nuevoAnio, nuevoMes)) {
                        popUpTo(RUTA_TRABAJOS)
                    }
                },
                onVerResumen = { navController.navigate(rutaResumen(trabajoId, anio, mes)) },
                onVolver = { navController.popBackStack() },
            )
        }
        composable(RUTA_RESUMEN) { backStackEntry ->
            val trabajoId = backStackEntry.arguments?.getString("trabajoId")?.toLongOrNull() ?: return@composable
            val anio = backStackEntry.arguments?.getString("anio")?.toIntOrNull() ?: return@composable
            val mes = backStackEntry.arguments?.getString("mes")?.toIntOrNull() ?: return@composable
            ResumenScreen(
                trabajoId = trabajoId,
                anio = anio,
                mes = mes,
                onVolver = { navController.popBackStack() },
            )
        }
    }
}
