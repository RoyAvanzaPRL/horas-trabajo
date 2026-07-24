package com.horastrabajo.app.ui.trabajos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.data.export.compartirArchivo
import com.horastrabajo.app.data.export.directorioExports
import com.horastrabajo.app.ui.AppViewModelProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajosScreen(
    onTrabajoSeleccionado: (Long) -> Unit,
    viewModel: TrabajosViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val trabajos by viewModel.trabajos.collectAsState()
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }
    val contexto = LocalContext.current

    val selectorImportacion = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val contenido = contexto.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        if (contenido != null) viewModel.importarBackup(contenido)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Trabajos") },
                actions = {
                    IconButton(onClick = { mostrarMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Exportar backup (JSON)") },
                            onClick = {
                                mostrarMenu = false
                                viewModel.exportarBackup { json ->
                                    val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                    val archivo = File(directorioExports(contexto), "backup_$marca.json")
                                    archivo.writeText(json)
                                    compartirArchivo(contexto, archivo, "application/json")
                                }
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Importar backup (JSON)") },
                            onClick = {
                                mostrarMenu = false
                                selectorImportacion.launch(arrayOf("application/json"))
                            },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNuevo = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir trabajo")
            }
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(trabajos, key = { it.id }) { trabajo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    ListItem(
                        headlineContent = { Text(trabajo.nombre) },
                        supportingContent = { Text("Símbolo: ${trabajo.simboloMoneda}") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(
                        onClick = { onTrabajoSeleccionado(trabajo.id) },
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    ) {
                        Text("Ver horas")
                    }
                }
            }
        }
    }

    if (mostrarDialogoNuevo) {
        NuevoTrabajoDialog(
            onConfirmar = { nombre, simbolo ->
                viewModel.crearTrabajo(nombre, simbolo)
                mostrarDialogoNuevo = false
            },
            onCancelar = { mostrarDialogoNuevo = false },
        )
    }
}

@Composable
private fun NuevoTrabajoDialog(onConfirmar: (String, String) -> Unit, onCancelar: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var simbolo by remember { mutableStateOf("€") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nuevo trabajo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (ej. Bar Pepe, Repartos)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = simbolo,
                    onValueChange = { if (it.length <= 3) simbolo = it },
                    label = { Text("Símbolo de moneda") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(nombre, simbolo) }) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        },
    )
}
