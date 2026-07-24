package com.horastrabajo.app.data.export

import com.horastrabajo.app.domain.ResumenMensual
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
private val FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM")

enum class EstiloFila { TITULO, NORMAL, NEGRITA }

data class FilaResumen(val izquierda: String, val derecha: String = "", val estilo: EstiloFila = EstiloFila.NORMAL)

/** Construye las filas de texto del resumen, compartidas entre el exportador de PDF y el de JPEG. */
fun construirFilasResumen(resumen: ResumenMensual): List<FilaResumen> {
    val simbolo = resumen.trabajo.simboloMoneda
    val nombreMes = java.time.Month.of(resumen.mes).getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }
    val filas = mutableListOf<FilaResumen>()

    filas += FilaResumen("${resumen.trabajo.nombre} — $nombreMes ${resumen.anio}", estilo = EstiloFila.TITULO)
    val tarifaTexto = resumen.precioPorHora?.let { "${it.formateado(simbolo)}/hora" } ?: "sin fijar"
    filas += FilaResumen("Tarifa: $tarifaTexto")
    filas += FilaResumen("")

    for (dia in resumen.dias) {
        for (entrada in dia.entradas) {
            val sufijo = if (entrada.esDiaSiguiente) " (+1 día)" else ""
            val notas = entrada.notas?.takeIf { it.isNotBlank() }?.let { " — $it" } ?: ""
            filas += FilaResumen(
                "${dia.fecha.format(FORMATO_DIA)}  ${entrada.horaEntrada.format(FORMATO_HORA)}-" +
                    "${entrada.horaSalida.format(FORMATO_HORA)}$sufijo$notas",
            )
        }
        filas += FilaResumen(
            "  Subtotal ${dia.fecha.format(FORMATO_DIA)}",
            "%.1fh · %s".format(dia.horas, dia.dinero.formateado(simbolo)),
            EstiloFila.NEGRITA,
        )
        val semana = resumen.semanas.firstOrNull { it.ultimoDiaVisible == dia.fecha }
        if (semana != null) {
            val etiqueta = if (semana.completa) "Total semana" else "Total semana (parcial)"
            filas += FilaResumen(
                etiqueta,
                "%.1fh · %s".format(semana.horas, semana.dinero.formateado(simbolo)),
                EstiloFila.NEGRITA,
            )
        }
    }

    filas += FilaResumen("")
    filas += FilaResumen(
        "TOTAL HORAS DEL MES",
        "%.1fh · %s".format(resumen.totalHoras, resumen.totalDineroHoras.formateado(simbolo)),
        EstiloFila.NEGRITA,
    )

    if (resumen.dineroExtra.isNotEmpty()) {
        filas += FilaResumen("")
        filas += FilaResumen("Dinero extra", estilo = EstiloFila.TITULO)
        for (extra in resumen.dineroExtra) {
            filas += FilaResumen("${extra.fecha.format(FORMATO_DIA)}  ${extra.descripcion}", extra.monto.formateado(simbolo))
        }
        filas += FilaResumen("Total dinero extra", resumen.totalDineroExtra.formateado(simbolo), EstiloFila.NEGRITA)
    }

    filas += FilaResumen("")
    filas += FilaResumen("TOTAL A COBRAR", resumen.totalDinero.formateado(simbolo), EstiloFila.TITULO)

    return filas
}
