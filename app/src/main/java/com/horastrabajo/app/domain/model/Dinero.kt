package com.horastrabajo.app.domain.model

import java.util.Locale
import kotlin.math.roundToLong

/**
 * Cantidad de dinero en céntimos (unidad entera) para evitar errores de redondeo
 * de coma flotante. [centimos] puede ser negativo (usado por el dinero extra).
 */
@JvmInline
value class Dinero(val centimos: Long) : Comparable<Dinero> {

    operator fun plus(otro: Dinero): Dinero = Dinero(centimos + otro.centimos)
    operator fun minus(otro: Dinero): Dinero = Dinero(centimos - otro.centimos)
    operator fun unaryMinus(): Dinero = Dinero(-centimos)

    override fun compareTo(other: Dinero): Int = centimos.compareTo(other.centimos)

    fun formateado(simbolo: String): String {
        val entero = centimos / 100
        val decimales = kotlin.math.abs(centimos % 100)
        return String.format(Locale.getDefault(), "%d,%02d %s", entero, decimales, simbolo)
    }

    companion object {
        val CERO = Dinero(0)

        /** Horas trabajadas (como fracción, ej. 7.5) al precio por hora dado, redondeado al céntimo. */
        fun porHoras(horas: Double, precioPorHora: Dinero): Dinero =
            Dinero((horas * precioPorHora.centimos).roundToLong())
    }
}

fun List<Dinero>.suma(): Dinero = fold(Dinero.CERO) { acc, d -> acc + d }
