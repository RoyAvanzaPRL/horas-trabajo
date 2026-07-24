package com.horastrabajo.app.ui.trabajos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.domain.model.Trabajo
import com.horastrabajo.app.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrabajosScreen(
    onTrabajoSeleccionado: (Long) -> Unit,
    onAbrirAjustes: () -> Unit,
    viewModel: TrabajosViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val trabajos by viewModel.trabajos.collectAsState()
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var trabajoEditando by remember { mutableStateOf<Trabajo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Trabajos") },
                actions = {
                    IconButton(onClick = onAbrirAjustes) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
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
                        supportingContent = { Text("${trabajo.nombreUsuario} · Símbolo: ${trabajo.simboloMoneda}") },
                        trailingContent = {
                            IconButton(onClick = { trabajoEditando = trabajo }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar trabajo")
                            }
                        },
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
        TrabajoFormDialog(
            titulo = "Nuevo trabajo",
            nombreInicial = "",
            nombreUsuarioInicial = "",
            simboloInicial = "€",
            onConfirmar = { nombre, nombreUsuario, simbolo ->
                viewModel.crearTrabajo(nombre, nombreUsuario, simbolo)
                mostrarDialogoNuevo = false
            },
            onCancelar = { mostrarDialogoNuevo = false },
        )
    }

    trabajoEditando?.let { trabajo ->
        TrabajoFormDialog(
            titulo = "Editar trabajo",
            nombreInicial = trabajo.nombre,
            nombreUsuarioInicial = trabajo.nombreUsuario,
            simboloInicial = trabajo.simboloMoneda,
            onConfirmar = { nombre, nombreUsuario, simbolo ->
                viewModel.actualizarTrabajo(trabajo, nombre, nombreUsuario, simbolo)
                trabajoEditando = null
            },
            onCancelar = { trabajoEditando = null },
        )
    }
}

@Composable
private fun TrabajoFormDialog(
    titulo: String,
    nombreInicial: String,
    nombreUsuarioInicial: String,
    simboloInicial: String,
    onConfirmar: (String, String, String) -> Unit,
    onCancelar: () -> Unit,
) {
    var nombre by remember { mutableStateOf(nombreInicial) }
    var nombreUsuario by remember { mutableStateOf(nombreUsuarioInicial) }
    var simbolo by remember { mutableStateOf(simboloInicial) }
    val esValido = nombre.isNotBlank() && nombreUsuario.isNotBlank()

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (ej. Bar Pepe, Repartos)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { nombreUsuario = it },
                    label = { Text("Tu nombre (aparece en el resumen exportado)") },
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
            TextButton(
                enabled = esValido,
                onClick = { onConfirmar(nombre, nombreUsuario, simbolo) },
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        },
    )
}
