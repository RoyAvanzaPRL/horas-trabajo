package com.horastrabajo.app.ui.entrada

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val FORMATO_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoraPickerField(etiqueta: String, hora: LocalTime, onHoraSeleccionada: (LocalTime) -> Unit) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { mostrarDialogo = true }) {
        Text("$etiqueta: ${hora.format(FORMATO_HORA)}")
    }

    if (mostrarDialogo) {
        val estado = rememberTimePickerState(initialHour = hora.hour, initialMinute = hora.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            text = { TimePicker(state = estado) },
            confirmButton = {
                TextButton(onClick = {
                    onHoraSeleccionada(LocalTime.of(estado.hour, estado.minute))
                    mostrarDialogo = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            },
            modifier = androidx.compose.ui.Modifier.padding(16.dp),
        )
    }
}
