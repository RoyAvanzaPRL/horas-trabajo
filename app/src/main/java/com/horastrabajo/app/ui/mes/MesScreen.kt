package com.horastrabajo.app.ui.mes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.domain.ResumenDia
import com.horastrabajo.app.domain.model.Dinero
import com.horastrabajo.app.domain.model.DineroExtra
import com.horastrabajo.app.ui.AppViewModelProvider
import com.horastrabajo.app.ui.entrada.EntradaFormSheet
import com.horastrabajo.app.ui.entrada.EntradaFormViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MesScreen(
    trabajoId: Long,
    anio: Int,
    mes: Int,
    onCambiarMes: (Int, Int) -> Unit,
    onVerResumen: () -> Unit,
    onVolver: () -> Unit,
    mesViewModel: MesViewModel = viewModel(factory = AppViewModelProvider.Factory),
    entradaFormViewModel: EntradaFormViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(trabajoId, anio, mes) { mesViewModel.cargar(trabajoId, anio, mes) }

    val resumen by mesViewModel.resumen.collectAsState()
    val yearMonth = YearMonth.of(anio, mes)
    val simbolo = resumen?.trabajo?.simboloMoneda ?: "€"

    var diaSeleccionado by remember { mutableStateOf<LocalDate?>(null) }
    var editandoTarifa by remember { mutableStateOf(false) }
    var agregandoDineroExtra by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${resumen?.trabajo?.nombre ?: ""} · ${nombreMes(mes)} $anio")
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = {
                    val anterior = yearMonth.minusMonths(1)
                    onCambiarMes(anterior.year, anterior.monthValue)
                }) { Text("< Mes anterior") }
                TextButton(onClick = {
                    val siguiente = yearMonth.plusMonths(1)
                    onCambiarMes(siguiente.year, siguiente.monthValue)
                }) { Text("Mes siguiente >") }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { editandoTarifa = true },
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val tarifaTexto = resumen?.precioPorHora?.let { "${dineroATexto(it)} $simbolo/hora" } ?: "sin fijar"
                Text("Tarifa este mes: $tarifaTexto")
                Text("Editar")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items((1..yearMonth.lengthOfMonth()).toList()) { numeroDia ->
                    val fecha = yearMonth.atDay(numeroDia)
                    val diaResumen = resumen?.dias?.firstOrNull { it.fecha == fecha }
                    DiaRow(
                        fecha = fecha,
                        diaResumen = diaResumen,
                        simbolo = simbolo,
                        onClick = { diaSeleccionado = fecha },
                    )
                    val semana = resumen?.semanas?.firstOrNull { it.ultimoDiaVisible == fecha }
                    if (semana != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            val etiqueta = if (semana.completa) "Total semana" else "Total semana (parcial)"
                            Text(etiqueta, fontWeight = FontWeight.Bold)
                            Text(
                                "%.1fh · %s".format(semana.horas, semana.dinero.formateado(simbolo)),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Dinero extra este mes")
                        TextButton(onClick = { agregandoDineroExtra = true }) { Text("+ Añadir") }
                    }
                    resumen?.dineroExtra?.forEach { extra ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${extra.fecha.dayOfMonth}/${extra.fecha.monthValue} · ${extra.descripcion}")
                            Text(extra.monto.formateado(simbolo))
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Total horas: %.1fh".format(resumen?.totalHoras ?: 0.0))
                            Text("Total dinero: ${(resumen?.totalDinero ?: Dinero.CERO).formateado(simbolo)}")
                        }
                        TextButton(onClick = onVerResumen) { Text("Ver resumen / exportar") }
                    }
                }
            }
        }
    }

    diaSeleccionado?.let { fecha ->
        LaunchedEffect(fecha) { entradaFormViewModel.cargar(trabajoId, fecha) }
        val entradasDelDia by entradaFormViewModel.entradasDelDia.collectAsState()
        ModalBottomSheet(onDismissRequest = { diaSeleccionado = null }) {
            EntradaFormSheet(
                fecha = fecha,
                trabajoId = trabajoId,
                entradasExistentes = entradasDelDia,
                onGuardar = { entradaFormViewModel.guardar(it) },
                onEliminar = { entradaFormViewModel.eliminar(it) },
            )
        }
    }

    if (editandoTarifa) {
        TarifaDialog(
            precioActual = resumen?.precioPorHora,
            simbolo = simbolo,
            onConfirmar = {
                mesViewModel.fijarTarifaDelMes(it)
                editandoTarifa = false
            },
            onCancelar = { editandoTarifa = false },
        )
    }

    if (agregandoDineroExtra) {
        DineroExtraDialog(
            fechaPorDefecto = LocalDate.now().let { if (YearMonth.from(it) == yearMonth) it else yearMonth.atDay(1) },
            onConfirmar = { fecha, monto, descripcion ->
                mesViewModel.agregarDineroExtra(
                    DineroExtra(trabajoId = trabajoId, fecha = fecha, monto = monto, descripcion = descripcion)
                )
                agregandoDineroExtra = false
            },
            onCancelar = { agregandoDineroExtra = false },
        )
    }
}

@Composable
private fun DiaRow(fecha: LocalDate, diaResumen: ResumenDia?, simbolo: String, onClick: () -> Unit) {
    val esFinDeSemana = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (esFinDeSemana) androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        Text("${fecha.dayOfMonth} ($nombreDia)")
        if (diaResumen != null && diaResumen.horas > 0) {
            Text("%.1fh · %s".format(diaResumen.horas, diaResumen.dinero.formateado(simbolo)))
        } else {
            Text("—")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarifaDialog(precioActual: Dinero?, simbolo: String, onConfirmar: (Dinero) -> Unit, onCancelar: () -> Unit) {
    var texto by remember { mutableStateOf(precioActual?.let { dineroATexto(it) } ?: "") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Tarifa por hora de este mes") },
        text = {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Precio por hora ($simbolo)") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { textoADinero(texto)?.let(onConfirmar) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DineroExtraDialog(
    fechaPorDefecto: LocalDate,
    onConfirmar: (LocalDate, Dinero, String) -> Unit,
    onCancelar: () -> Unit,
) {
    var fecha by remember { mutableStateOf(fechaPorDefecto) }
    var texto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Dinero extra") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FechaPickerField("Fecha", fecha) { fecha = it }
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Cantidad (puede ser negativa)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (ej. reparto)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                textoADinero(texto)?.let { onConfirmar(fecha, it, descripcion.trim()) }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } },
    )
}

private fun nombreMes(mes: Int): String =
    java.time.Month.of(mes).getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }
