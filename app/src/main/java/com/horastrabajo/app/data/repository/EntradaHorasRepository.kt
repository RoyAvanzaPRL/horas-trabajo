package com.horastrabajo.app.data.repository

import com.horastrabajo.app.data.local.dao.EntradaHorasDao
import com.horastrabajo.app.domain.model.EntradaHoras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

interface EntradaHorasRepository {
    /**
     * Entradas cuyo turno EMPIEZA en [anio]/[mes]. Un turno que cruza de mes se asigna
     * entero al mes en que empezó (decisión de diseño), así que filtrar por [fecha] del
     * mes es suficiente y correcto.
     */
    fun observePorMes(trabajoId: Long, anio: Int, mes: Int): Flow<List<EntradaHoras>>
    fun observePorFecha(trabajoId: Long, fecha: LocalDate): Flow<List<EntradaHoras>>
    suspend fun guardar(entrada: EntradaHoras): Long
    suspend fun eliminar(entrada: EntradaHoras)

    /** Todas las entradas de un trabajo, sin límite de fecha. Solo para backup/export. */
    suspend fun obtenerTodasDelTrabajo(trabajoId: Long): List<EntradaHoras>
    suspend fun guardarVarias(entradas: List<EntradaHoras>)
}

class EntradaHorasRepositoryImpl(private val dao: EntradaHorasDao) : EntradaHorasRepository {

    override fun observePorMes(trabajoId: Long, anio: Int, mes: Int): Flow<List<EntradaHoras>> {
        val yearMonth = YearMonth.of(anio, mes)
        return dao.observeByTrabajoYRango(trabajoId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
            .map { entidades -> entidades.map { it.toDomain() } }
    }

    override fun observePorFecha(trabajoId: Long, fecha: LocalDate): Flow<List<EntradaHoras>> =
        dao.observeByTrabajoYFecha(trabajoId, fecha).map { entidades -> entidades.map { it.toDomain() } }

    override suspend fun guardar(entrada: EntradaHoras): Long =
        if (entrada.id == 0L) dao.insert(entrada.toEntity()) else {
            dao.update(entrada.toEntity())
            entrada.id
        }

    override suspend fun eliminar(entrada: EntradaHoras) = dao.delete(entrada.toEntity())

    override suspend fun obtenerTodasDelTrabajo(trabajoId: Long): List<EntradaHoras> =
        dao.getAllByTrabajo(trabajoId).map { it.toDomain() }

    override suspend fun guardarVarias(entradas: List<EntradaHoras>) =
        dao.insertAll(entradas.map { it.toEntity() })
}
