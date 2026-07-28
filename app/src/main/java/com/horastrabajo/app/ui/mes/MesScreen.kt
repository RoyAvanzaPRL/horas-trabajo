package com.horastrabajo.app.ui.mes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.R
import com.horastrabajo.app.domain.ResumenDia
import com.horastrabajo.app.domain.ResumenSemana
import com.horastrabajo.app.domain.model.Dinero
import com.horastrabajo.app.domain.model.DineroExtra
import com.horastrabajo.app.domain.model.formatearHoras
import com.horastrabajo.app.ui.AppViewModelProvider
import com.horastrabajo.app.ui.components.CampoDinero
import com.horastrabajo.app.ui.components.CifraDestacada
import com.horastrabajo.app.ui.components.localeActual
import com.horastrabajo.app.ui.entrada.EntradaFormSheet
import com.horastrabajo.app.ui.entrada.EntradaFormViewModel
import kotlinx.coroutines.launch
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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val locale = localeActual()
    val textoDineroExtraEliminado = stringResource(R.string.mes_dinero_extra_eliminado)
    val textoDeshacer = stringResource(R.string.accion_deshacer)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.mes_titulo,
                            resumen?.trabajo?.nombre ?: "",
                            nombreMes(mes, locale),
                            anio,
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.accion_volver),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onVerResumen) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.mes_ver_resumen),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val resumenActual = resumen
        if (resumenActual == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding)) {
            Card(
                onClick = { editandoTarifa = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            stringResource(R.string.mes_tarifa_etiqueta),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        val tarifaTexto = resumenActual.precioPorHora
                            ?.let { stringResource(R.string.mes_tarifa_valor, dineroATexto(it), simbolo) }
                            ?: stringResource(R.string.mes_tarifa_sin_fijar)
                        Text(tarifaTexto, style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            stringResource(R.string.accion_editar),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items((1..yearMonth.lengthOfMonth()).toList()) { numeroDia ->
                    val fecha = yearMonth.atDay(numeroDia)
                    val diaResumen = resumenActual.dias.firstOrNull { it.fecha == fecha }
                    val semana = resumenActual.semanas.firstOrNull { it.ultimoDiaVisible == fecha }
                    DiaRow(
                        fecha = fecha,
                        diaResumen = diaResumen,
                        resumenSemana = semana,
                        simbolo = simbolo,
                        locale = locale,
                        onClick = { diaSeleccionado = fecha },
                    )
                    HorizontalDivider()
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(stringResource(R.string.mes_dinero_extra_titulo))
                        TextButton(onClick = { agregandoDineroExtra = true }) {
                            Text(stringResource(R.string.accion_anadir))
                        }
                    }
                    resumenActual.dineroExtra.forEach { extra ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(
                                    R.string.mes_dinero_extra_fila,
                                    extra.fecha.dayOfMonth,
                                    extra.fecha.monthValue,
                                    extra.descripcion,
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(extra.monto.formateado(simbolo))
                                IconButton(onClick = {
                                    mesViewModel.eliminarDineroExtra(extra)
                                    scope.launch {
                                        val resultado = snackbarHostState.showSnackbar(
                                            message = textoDineroExtraEliminado,
                                            actionLabel = textoDeshacer,
                                            duration = SnackbarDuration.Short,
                                        )
                                        if (resultado == SnackbarResult.ActionPerformed) {
                                            mesViewModel.agregarDineroExtra(extra.copy(id = 0))
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = stringResource(R.string.mes_eliminar_dinero_extra),
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        CifraDestacada(
                            stringResource(
                                R.string.mes_total_dinero,
                                resumenActual.totalDinero.formateado(simbolo),
                            )
                        )
                        Text(
                            stringResource(R.string.mes_total_horas, formatearHoras(resumenActual.totalHoras)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
        }
    }

    diaSeleccionado?.let { fecha ->
        LaunchedEffect(fecha) { entradaFormViewModel.cargar(trabajoId, fecha) }
        val entradasDelDia by entradaFormViewModel.entradasDelDia.collectAsState()
        ModalBottomSheet(
            onDismissRequest = { diaSeleccionado = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            EntradaFormSheet(
                fecha = fecha,
                trabajoId = trabajoId,
                simbolo = simbolo,
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
        ModalBottomSheet(
            onDismissRequest = { agregandoDineroExtra = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            DineroExtraFormContent(
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
}

@Composable
private fun DiaRow(
    fecha: LocalDate,
    diaResumen: ResumenDia?,
    resumenSemana: ResumenSemana?,
    simbolo: String,
    locale: Locale,
    onClick: () -> Unit,
) {
    val esHoy = fecha == LocalDate.now()
    val esFinDeSemana = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY
    val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
    val colorContenido = if (esHoy) MaterialTheme.colorScheme.onPrimary else Color.Unspecified
    ListItem(
        headlineContent = {
            Text(
                stringResource(R.string.mes_dia_encabezado, fecha.dayOfMonth, nombreDia),
                fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
                color = colorContenido,
            )
        },
        supportingContent = resumenSemana?.let { semana ->
            {
                val etiqueta = stringResource(
                    if (semana.completa) R.string.mes_total_semana else R.string.mes_total_semana_parcial
                )
                Text(
                    stringResource(
                        R.string.mes_semana_resumen,
                        etiqueta,
                        formatearHoras(semana.horas),
                        semana.dinero.formateado(simbolo),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colorContenido,
                )
            }
        },
        trailingContent = {
            if (diaResumen != null && diaResumen.horas > 0) {
                val tieneTurnoNocturno = diaResumen.entradas.any { it.esDiaSiguiente }
                val tieneTarifaCustom = diaResumen.entradas.any { it.precioPorHoraCustom != null }
                val numeroTurnos = diaResumen.entradas.size
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tieneTurnoNocturno) {
                        MarcadorDia(
                            simbolo = "🌙",
                            descripcion = stringResource(R.string.marcador_nocturno),
                            color = colorContenido,
                        )
                    }
                    if (tieneTarifaCustom) {
                        MarcadorDia(
                            simbolo = "✱",
                            descripcion = stringResource(R.string.marcador_tarifa_distinta),
                            color = colorContenido,
                        )
                    }
                    if (numeroTurnos > 1) {
                        MarcadorDia(
                            simbolo = "×$numeroTurnos",
                            descripcion = stringResource(R.string.marcador_varios_turnos, numeroTurnos),
                            color = colorContenido,
                        )
                    }
                    Text(
                        stringResource(
                            R.string.mes_dia_valor,
                            formatearHoras(diaResumen.horas),
                            diaResumen.dinero.formateado(simbolo),
                        ),
                        color = colorContenido,
                    )
                }
            } else {
                Text("—", color = colorContenido)
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = when {
                esHoy -> MaterialTheme.colorScheme.primary
                esFinDeSemana -> MaterialTheme.colorScheme.surfaceVariant
                else -> Color.Transparent
            },
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}

/**
 * Marcador puramente informativo (sin interacción propia) para señalar algo del día:
 * turno nocturno, tarifa distinta o más de un turno. Cada condición usa su propio
 * símbolo; [descripcion] sustituye la lectura literal del símbolo para TalkBack.
 */
@Composable
private fun MarcadorDia(simbolo: String, descripcion: String, color: Color = Color.Unspecified) {
    Text(
        text = simbolo,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        modifier = Modifier
            .padding(end = 4.dp)
            .clearAndSetSemantics { contentDescription = descripcion },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarifaDialog(precioActual: Dinero?, simbolo: String, onConfirmar: (Dinero) -> Unit, onCancelar: () -> Unit) {
    var texto by remember { mutableStateOf(precioActual?.let { dineroATexto(it) } ?: "") }
    var mostrarErrores by remember { mutableStateOf(false) }
    val dineroValido = textoADinero(texto)
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(R.string.tarifa_dialogo_titulo)) },
        text = {
            CampoDinero(
                label = stringResource(R.string.tarifa_campo_precio, simbolo),
                texto = texto,
                onTextoChange = { texto = it },
                mostrarErrores = mostrarErrores,
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (dineroValido != null) onConfirmar(dineroValido) else mostrarErrores = true
            }) { Text(stringResource(R.string.accion_guardar)) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.accion_cancelar)) }
        },
    )
}

/**
 * Contenido de "Dinero extra" alojado en un [ModalBottomSheet] — 3 campos, mismo
 * criterio que [TrabajoFormSheetContent][com.horastrabajo.app.ui.trabajos] y
 * [com.horastrabajo.app.ui.entrada.EntradaFormSheet]: ya no en AlertDialog.
 */
@Composable
private fun DineroExtraFormContent(
    fechaPorDefecto: LocalDate,
    onConfirmar: (LocalDate, Dinero, String) -> Unit,
    onCancelar: () -> Unit,
) {
    var fecha by remember { mutableStateOf(fechaPorDefecto) }
    var texto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var mostrarErrores by remember { mutableStateOf(false) }
    val dineroValido = textoADinero(texto)

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.dinero_extra_titulo), style = MaterialTheme.typography.titleLarge)
        FechaPickerField(stringResource(R.string.campo_fecha), fecha) { fecha = it }
        CampoDinero(
            label = stringResource(R.string.dinero_extra_campo_cantidad),
            texto = texto,
            onTextoChange = { texto = it },
            mostrarErrores = mostrarErrores,
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text(stringResource(R.string.dinero_extra_campo_descripcion)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                if (dineroValido != null) onConfirmar(fecha, dineroValido, descripcion.trim()) else mostrarErrores = true
            }) { Text(stringResource(R.string.accion_guardar)) }
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.accion_cancelar)) }
        }
    }
}

private fun nombreMes(mes: Int, locale: Locale): String =
    java.time.Month.of(mes).getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase(locale) }
