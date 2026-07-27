package com.horastrabajo.app.ui.entrada

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.horastrabajo.app.R
import com.horastrabajo.app.domain.model.EntradaHoras
import com.horastrabajo.app.ui.components.CampoDinero
import com.horastrabajo.app.ui.components.TextoError
import com.horastrabajo.app.ui.mes.dineroATexto
import com.horastrabajo.app.ui.mes.textoADinero
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun EntradaFormSheet(
    fecha: LocalDate,
    trabajoId: Long,
    simbolo: String,
    entradasExistentes: List<EntradaHoras>,
    onGuardar: (EntradaHoras) -> Unit,
    onEliminar: (EntradaHoras) -> Unit,
) {
    var forzarFormulario by remember(fecha, trabajoId) { mutableStateOf(false) }
    val mostrarFormulario = forzarFormulario || entradasExistentes.isEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoTurnoEliminado = stringResource(R.string.entrada_turno_eliminado)
    val textoDeshacer = stringResource(R.string.accion_deshacer)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(
                    R.string.entrada_titulo,
                    "${fecha.dayOfMonth}/${fecha.monthValue}/${fecha.year}",
                )
            )

            entradasExistentes.forEach { entrada ->
                EntradaExistenteRow(
                    entrada = entrada,
                    simbolo = simbolo,
                    onEliminar = {
                        onEliminar(entrada)
                        scope.launch {
                            val resultado = snackbarHostState.showSnackbar(
                                message = textoTurnoEliminado,
                                actionLabel = textoDeshacer,
                                duration = SnackbarDuration.Short,
                            )
                            if (resultado == SnackbarResult.ActionPerformed) {
                                onGuardar(entrada.copy(id = 0))
                            }
                        }
                    },
                )
                HorizontalDivider()
            }

            if (mostrarFormulario) {
                NuevaEntradaForm(
                    fecha = fecha,
                    trabajoId = trabajoId,
                    simbolo = simbolo,
                    entradasExistentes = entradasExistentes,
                    onGuardar = { entrada ->
                        onGuardar(entrada)
                        forzarFormulario = false
                    },
                    onCancelar = if (entradasExistentes.isNotEmpty()) {
                        { forzarFormulario = false }
                    } else {
                        null
                    },
                )
            } else {
                TextButton(onClick = { forzarFormulario = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.entrada_anadir_turno))
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun EntradaExistenteRow(entrada: EntradaHoras, simbolo: String, onEliminar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            val rango = stringResource(
                R.string.entrada_rango_horas,
                entrada.horaEntrada.format(FORMATO_HORA),
                entrada.horaSalida.format(FORMATO_HORA),
            )
            val sufijo = if (entrada.esDiaSiguiente) {
                " " + stringResource(R.string.entrada_sufijo_dia_siguiente)
            } else {
                ""
            }
            Text("$rango$sufijo")
            entrada.notas?.takeIf { it.isNotBlank() }?.let { Text(it) }
            entrada.precioPorHoraCustom?.let {
                Text(
                    stringResource(R.string.entrada_tarifa_turno, dineroATexto(it), simbolo),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.accion_eliminar))
        }
    }
}

private fun seSolapan(a: EntradaHoras, b: EntradaHoras): Boolean =
    a.inicio < b.fin && b.inicio < a.fin

@Composable
private fun NuevaEntradaForm(
    fecha: LocalDate,
    trabajoId: Long,
    simbolo: String,
    entradasExistentes: List<EntradaHoras>,
    onGuardar: (EntradaHoras) -> Unit,
    onCancelar: (() -> Unit)?,
) {
    var horaEntrada by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var horaSalida by remember { mutableStateOf(LocalTime.of(17, 0)) }
    var esDiaSiguiente by remember { mutableStateOf(false) }
    var mostrarNotas by remember { mutableStateOf(false) }
    var notas by remember { mutableStateOf("") }
    var usarTarifaCustom by remember { mutableStateOf(false) }
    var tarifaCustomTexto by remember { mutableStateOf("") }
    var mostrarInfoNocturno by remember { mutableStateOf(false) }
    var mostrarErrores by remember { mutableStateOf(false) }

    val candidato = EntradaHoras(
        trabajoId = trabajoId,
        fecha = fecha,
        horaEntrada = horaEntrada,
        horaSalida = horaSalida,
        esDiaSiguiente = esDiaSiguiente,
    )
    val seSolapa = entradasExistentes.any { seSolapan(candidato, it) }
    val horarioInvalido = !esDiaSiguiente && horaSalida <= horaEntrada
    val tarifaCustomValida = !usarTarifaCustom || textoADinero(tarifaCustomTexto) != null
    val formularioValido = !horarioInvalido && !seSolapa && tarifaCustomValida

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HoraPickerField(stringResource(R.string.entrada_hora_entrada), horaEntrada) { horaEntrada = it }
        HoraPickerField(stringResource(R.string.entrada_hora_salida), horaSalida) { horaSalida = it }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = esDiaSiguiente, onValueChange = { esDiaSiguiente = it }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = esDiaSiguiente, onCheckedChange = null)
            Text(stringResource(R.string.entrada_turno_nocturno), modifier = Modifier.weight(1f))
            IconButton(onClick = { mostrarInfoNocturno = true }) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = stringResource(R.string.entrada_que_es_nocturno),
                )
            }
        }
        if (horarioInvalido) {
            TextoError(stringResource(R.string.entrada_error_horario))
        }
        if (seSolapa) {
            TextoError(stringResource(R.string.entrada_error_solapa))
        }
        if (mostrarNotas) {
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text(stringResource(R.string.entrada_campo_nota)) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            TextButton(onClick = { mostrarNotas = true }) { Text(stringResource(R.string.entrada_anadir_nota)) }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = usarTarifaCustom, onValueChange = { usarTarifaCustom = it }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = usarTarifaCustom, onCheckedChange = null)
            Text(stringResource(R.string.entrada_tarifa_distinta))
        }
        if (usarTarifaCustom) {
            CampoDinero(
                label = stringResource(R.string.tarifa_campo_precio, simbolo),
                texto = tarifaCustomTexto,
                onTextoChange = { tarifaCustomTexto = it },
                mostrarErrores = mostrarErrores,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    if (formularioValido) {
                        onGuardar(
                            candidato.copy(
                                notas = notas.takeIf { mostrarNotas && it.isNotBlank() },
                                precioPorHoraCustom = if (usarTarifaCustom) textoADinero(tarifaCustomTexto) else null,
                            )
                        )
                        horaEntrada = LocalTime.of(9, 0)
                        horaSalida = LocalTime.of(17, 0)
                        esDiaSiguiente = false
                        mostrarNotas = false
                        notas = ""
                        usarTarifaCustom = false
                        tarifaCustomTexto = ""
                        mostrarErrores = false
                    } else {
                        mostrarErrores = true
                    }
                },
            ) { Text(stringResource(R.string.accion_guardar)) }
            if (onCancelar != null) {
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.accion_cancelar)) }
            }
        }
    }

    if (mostrarInfoNocturno) {
        AlertDialog(
            onDismissRequest = { mostrarInfoNocturno = false },
            title = { Text(stringResource(R.string.entrada_turno_nocturno)) },
            text = { Text(stringResource(R.string.entrada_nocturno_explicacion)) },
            confirmButton = {
                TextButton(onClick = { mostrarInfoNocturno = false }) {
                    Text(stringResource(R.string.accion_entendido))
                }
            },
        )
    }
}
