package com.horastrabajo.app.ui.entrada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.horastrabajo.app.domain.model.EntradaHoras
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun EntradaFormSheet(
    fecha: LocalDate,
    trabajoId: Long,
    entradasExistentes: List<EntradaHoras>,
    onGuardar: (EntradaHoras) -> Unit,
    onEliminar: (EntradaHoras) -> Unit,
) {
    var mostrandoFormularioNuevo by remember(fecha, trabajoId) { mutableStateOf(entradasExistentes.isEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Horas del ${fecha.dayOfMonth}/${fecha.monthValue}/${fecha.year}")

        entradasExistentes.forEach { entrada ->
            EntradaExistenteRow(entrada = entrada, onEliminar = { onEliminar(entrada) })
            HorizontalDivider()
        }

        if (mostrandoFormularioNuevo) {
            NuevaEntradaForm(
                fecha = fecha,
                trabajoId = trabajoId,
                onGuardar = { entrada ->
                    onGuardar(entrada)
                    mostrandoFormularioNuevo = false
                },
                onCancelar = if (entradasExistentes.isNotEmpty()) {
                    { mostrandoFormularioNuevo = false }
                } else {
                    null
                },
            )
        } else {
            TextButton(onClick = { mostrandoFormularioNuevo = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Añadir otro turno")
            }
        }
    }
}

@Composable
private fun EntradaExistenteRow(entrada: EntradaHoras, onEliminar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            val sufijo = if (entrada.esDiaSiguiente) " (+1 día)" else ""
            Text("${entrada.horaEntrada.format(FORMATO_HORA)} - ${entrada.horaSalida.format(FORMATO_HORA)}$sufijo")
            entrada.notas?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
        }
    }
}

@Composable
private fun NuevaEntradaForm(
    fecha: LocalDate,
    trabajoId: Long,
    onGuardar: (EntradaHoras) -> Unit,
    onCancelar: (() -> Unit)?,
) {
    var horaEntrada by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var horaSalida by remember { mutableStateOf(LocalTime.of(17, 0)) }
    var esDiaSiguiente by remember { mutableStateOf(false) }
    var mostrarNotas by remember { mutableStateOf(false) }
    var notas by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HoraPickerField("Entrada", horaEntrada) { horaEntrada = it }
            HoraPickerField("Salida", horaSalida) { horaSalida = it }
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = esDiaSiguiente, onCheckedChange = { esDiaSiguiente = it })
            Text("La salida es del día siguiente (turno nocturno)")
        }
        if (mostrarNotas) {
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            TextButton(onClick = { mostrarNotas = true }) { Text("+ Añadir nota") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    onGuardar(
                        EntradaHoras(
                            trabajoId = trabajoId,
                            fecha = fecha,
                            horaEntrada = horaEntrada,
                            horaSalida = horaSalida,
                            esDiaSiguiente = esDiaSiguiente,
                            notas = notas.takeIf { mostrarNotas && it.isNotBlank() },
                        )
                    )
                },
            ) { Text("Guardar") }
            if (onCancelar != null) {
                TextButton(onClick = onCancelar) { Text("Cancelar") }
            }
        }
    }
}
