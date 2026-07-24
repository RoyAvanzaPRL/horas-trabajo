package com.horastrabajo.app.ui.resumen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.horastrabajo.app.data.export.EstiloFila
import com.horastrabajo.app.data.export.JpegExporter
import com.horastrabajo.app.data.export.PdfExporter
import com.horastrabajo.app.data.export.compartirArchivo
import com.horastrabajo.app.data.export.construirFilasResumen
import com.horastrabajo.app.data.export.directorioExports
import com.horastrabajo.app.ui.AppViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumenScreen(
    trabajoId: Long,
    anio: Int,
    mes: Int,
    onVolver: () -> Unit,
    viewModel: ResumenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    LaunchedEffect(trabajoId, anio, mes) { viewModel.cargar(trabajoId, anio, mes) }

    val resumen by viewModel.resumen.collectAsState()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        val resumenActual = resumen
        if (resumenActual == null) {
            Row(modifier = Modifier.fillMaxSize().padding(padding), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val filas = construirFilasResumen(resumenActual)

        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = {
                    scope.launch {
                        val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        val archivo = File(directorioExports(contexto), "resumen_$marca.pdf")
                        withContext(Dispatchers.IO) { PdfExporter.exportar(resumenActual, archivo) }
                        compartirArchivo(contexto, archivo, "application/pdf")
                    }
                }) { Text("Exportar PDF") }
                TextButton(onClick = {
                    scope.launch {
                        val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                        val archivo = File(directorioExports(contexto), "resumen_$marca.jpg")
                        withContext(Dispatchers.IO) { JpegExporter.exportar(resumenActual, archivo) }
                        compartirArchivo(contexto, archivo, "image/jpeg")
                    }
                }) { Text("Exportar JPEG") }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filas) { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val peso = if (fila.estilo == EstiloFila.NORMAL) FontWeight.Normal else FontWeight.Bold
                        Text(fila.izquierda, fontWeight = peso)
                        if (fila.derecha.isNotEmpty()) {
                            Text(fila.derecha, fontWeight = peso)
                        }
                    }
                }
            }
        }
    }
}
