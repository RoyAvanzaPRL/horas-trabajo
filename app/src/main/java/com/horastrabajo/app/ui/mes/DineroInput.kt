package com.horastrabajo.app.ui.mes

import com.horastrabajo.app.domain.model.Dinero
import kotlin.math.roundToLong

/** Convierte un texto tipo "8,50" o "8.50" a céntimos. Null si no es un número válido. */
fun textoADinero(texto: String): Dinero? {
    val normalizado = texto.trim().replace(',', '.')
    val valor = normalizado.toDoubleOrNull() ?: return null
    return Dinero((valor * 100).roundToLong())
}

fun dineroATexto(dinero: Dinero): String {
    val euros = dinero.centimos / 100.0
    return String.format(java.util.Locale.getDefault(), "%.2f", euros)
}
