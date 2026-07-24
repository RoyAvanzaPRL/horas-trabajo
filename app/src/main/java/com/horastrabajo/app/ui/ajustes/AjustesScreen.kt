package com.horastrabajo.app.ui.ajustes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.data.export.compartirArchivo
import com.horastrabajo.app.data.export.directorioExports
import com.horastrabajo.app.data.preferences.TemaPreferido
import com.horastrabajo.app.ui.AppViewModelProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onVolver: () -> Unit,
    viewModel: AjustesViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val temaPreferido by viewModel.temaPreferido.collectAsState()
    val contexto = LocalContext.current

    val selectorImportacion = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contenido = contexto.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (contenido != null) viewModel.importarBackup(contenido)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text("Tema", modifier = Modifier.padding(16.dp))
            TemaPreferido.entries.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = temaPreferido == opcion,
                            onClick = { viewModel.cambiarTema(opcion) },
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                ) {
                    RadioButton(selected = temaPreferido == opcion, onClick = { viewModel.cambiarTema(opcion) })
                    Text(
                        text = nombreTema(opcion),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Backup", modifier = Modifier.padding(horizontal = 16.dp))
            TextButton(onClick = {
                viewModel.exportarBackup { json ->
                    val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                    val archivo = File(directorioExports(contexto), "backup_$marca.json")
                    archivo.writeText(json)
                    compartirArchivo(contexto, archivo, "application/json")
                }
            }) { Text("Exportar backup (JSON)") }
            TextButton(onClick = { selectorImportacion.launch(arrayOf("application/json")) }) {
                Text("Importar backup (JSON)")
            }
        }
    }
}

private fun nombreTema(tema: TemaPreferido): String = when (tema) {
    TemaPreferido.SISTEMA -> "Sistema"
    TemaPreferido.CLARO -> "Claro"
    TemaPreferido.OSCURO -> "Oscuro"
}
