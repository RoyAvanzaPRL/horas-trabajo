package com.horastrabajo.app.ui.trabajos

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.R
import com.horastrabajo.app.domain.model.Trabajo
import com.horastrabajo.app.ui.AppViewModelProvider
import com.horastrabajo.app.ui.components.CampoObligatorio

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
    var trabajoAEliminar by remember { mutableStateOf<Trabajo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trabajos_titulo)) },
                actions = {
                    IconButton(onClick = onAbrirAjustes) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.ajustes_titulo))
                    }
                },
            )
        },
        floatingActionButton = {
            if (trabajos.isEmpty()) {
                ExtendedFloatingActionButton(onClick = { mostrarDialogoNuevo = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(stringResource(R.string.trabajo_nuevo), modifier = Modifier.padding(start = 8.dp))
                }
            } else {
                FloatingActionButton(onClick = { mostrarDialogoNuevo = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.trabajo_anadir))
                }
            }
        },
    ) { padding ->
        if (trabajos.isEmpty()) {
            EstadoVacioTrabajos(modifier = Modifier.padding(padding), onCrear = { mostrarDialogoNuevo = true })
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(trabajos, key = { it.id }) { trabajo ->
                    TrabajoCard(
                        trabajo = trabajo,
                        onAbrir = { onTrabajoSeleccionado(trabajo.id) },
                        onEditar = { trabajoEditando = trabajo },
                        onEliminar = { trabajoAEliminar = trabajo },
                    )
                }
            }
        }
    }

    if (mostrarDialogoNuevo) {
        ModalBottomSheet(onDismissRequest = { mostrarDialogoNuevo = false }) {
            TrabajoFormSheetContent(
                titulo = stringResource(R.string.trabajo_nuevo),
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
    }

    trabajoEditando?.let { trabajo ->
        ModalBottomSheet(onDismissRequest = { trabajoEditando = null }) {
            TrabajoFormSheetContent(
                titulo = stringResource(R.string.trabajo_editar),
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

    trabajoAEliminar?.let { trabajo ->
        AlertDialog(
            onDismissRequest = { trabajoAEliminar = null },
            title = { Text(stringResource(R.string.trabajo_eliminar_titulo)) },
            text = { Text(stringResource(R.string.trabajo_eliminar_mensaje, trabajo.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarTrabajo(trabajo)
                    trabajoAEliminar = null
                }) { Text(stringResource(R.string.accion_eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { trabajoAEliminar = null }) { Text(stringResource(R.string.accion_cancelar)) }
            },
        )
    }
}

@Composable
private fun EstadoVacioTrabajos(onCrear: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                stringResource(R.string.trabajos_vacio_titulo),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.trabajos_vacio_mensaje),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onCrear) { Text(stringResource(R.string.trabajos_vacio_boton)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrabajoCard(
    trabajo: Trabajo,
    onAbrir: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Card(
        onClick = onAbrir,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        ListItem(
            headlineContent = { Text(trabajo.nombre) },
            supportingContent = { Text(trabajo.nombreUsuario) },
            trailingContent = {
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.trabajo_mas_opciones, trabajo.nombre),
                        )
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.accion_editar)) },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                onEditar()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.accion_eliminar)) },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                onEliminar()
                            },
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private data class Moneda(val simbolo: String, @StringRes val nombreRes: Int)

private val MONEDAS = listOf(
    Moneda("€", R.string.moneda_euro),
    Moneda("$", R.string.moneda_dolar),
    Moneda("£", R.string.moneda_libra),
    Moneda("¥", R.string.moneda_yen),
    Moneda("Fr", R.string.moneda_franco),
)

/**
 * Contenido del formulario de trabajo (nuevo o editar) alojado en un [ModalBottomSheet] —
 * mismo criterio que [com.horastrabajo.app.ui.entrada.EntradaFormSheet] y
 * [com.horastrabajo.app.ui.mes.MesScreen]: formularios de 3 o más campos van en bottom
 * sheet, no en AlertDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrabajoFormSheetContent(
    titulo: String,
    nombreInicial: String,
    nombreUsuarioInicial: String,
    simboloInicial: String,
    onConfirmar: (String, String, String) -> Unit,
    onCancelar: () -> Unit,
) {
    var nombre by remember { mutableStateOf(nombreInicial) }
    var nombreUsuario by remember { mutableStateOf(nombreUsuarioInicial) }
    var simbolo by remember {
        mutableStateOf(MONEDAS.firstOrNull { it.simbolo == simboloInicial }?.simbolo ?: MONEDAS.first().simbolo)
    }
    var monedaExpandida by remember { mutableStateOf(false) }
    var mostrarErrores by remember { mutableStateOf(false) }
    val esValido = nombre.isNotBlank() && nombreUsuario.isNotBlank()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        CampoObligatorio(
            label = stringResource(R.string.trabajo_campo_nombre),
            texto = nombre,
            onTextoChange = { nombre = it },
            mostrarErrores = mostrarErrores,
        )
        CampoObligatorio(
            label = stringResource(R.string.trabajo_campo_tu_nombre),
            texto = nombreUsuario,
            onTextoChange = { nombreUsuario = it },
            mostrarErrores = mostrarErrores,
        )
        val monedaSeleccionada = MONEDAS.first { it.simbolo == simbolo }
        ExposedDropdownMenuBox(
            expanded = monedaExpandida,
            onExpandedChange = { monedaExpandida = it },
        ) {
            OutlinedTextField(
                value = stringResource(
                    R.string.moneda_opcion,
                    stringResource(monedaSeleccionada.nombreRes),
                    monedaSeleccionada.simbolo,
                ),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.trabajo_campo_moneda)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monedaExpandida) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = monedaExpandida, onDismissRequest = { monedaExpandida = false }) {
                MONEDAS.forEach { moneda ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    R.string.moneda_opcion,
                                    stringResource(moneda.nombreRes),
                                    moneda.simbolo,
                                )
                            )
                        },
                        onClick = {
                            simbolo = moneda.simbolo
                            monedaExpandida = false
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = {
                    if (esValido) onConfirmar(nombre, nombreUsuario, simbolo) else mostrarErrores = true
                },
            ) { Text(stringResource(R.string.accion_guardar)) }
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.accion_cancelar)) }
        }
    }
}
