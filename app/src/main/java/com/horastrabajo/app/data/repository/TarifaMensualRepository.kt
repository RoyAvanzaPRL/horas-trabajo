package com.horastrabajo.app.data.repository

import com.horastrabajo.app.data.local.dao.TarifaMensualDao
import com.horastrabajo.app.domain.model.Dinero
import com.horastrabajo.app.domain.model.TarifaMensual

interface TarifaMensualRepository {
    /**
     * La tarifa vigente para [anio]/[mes]: la explícita de ese mes si existe, o si no,
     * heredada de la más reciente en un mes anterior. Null si nunca se fijó ninguna.
     */
    suspend fun obtenerTarifaVigente(trabajoId: Long, anio: Int, mes: Int): TarifaMensual?

    suspend fun fijarTarifaDelMes(trabajoId: Long, anio: Int, mes: Int, precioPorHora: Dinero)

    /** Todas las tarifas de un trabajo. Solo para backup/export. */
    suspend fun obtenerTodasDelTrabajo(trabajoId: Long): List<TarifaMensual>
    suspend fun guardarVarias(tarifas: List<TarifaMensual>)
}

class TarifaMensualRepositoryImpl(private val dao: TarifaMensualDao) : TarifaMensualRepository {

    override suspend fun obtenerTarifaVigente(trabajoId: Long, anio: Int, mes: Int): TarifaMensual? =
        dao.getVigenteEnOAntesDe(trabajoId, anio, mes)?.toDomain()

    override suspend fun fijarTarifaDelMes(trabajoId: Long, anio: Int, mes: Int, precioPorHora: Dinero) {
        dao.upsert(
            TarifaMensual(trabajoId = trabajoId, anio = anio, mes = mes, precioPorHora = precioPorHora).toEntity()
        )
    }

    override suspend fun obtenerTodasDelTrabajo(trabajoId: Long): List<TarifaMensual> =
        dao.getAllByTrabajo(trabajoId).map { it.toDomain() }

    override suspend fun guardarVarias(tarifas: List<TarifaMensual>) =
        dao.insertAll(tarifas.map { it.toEntity() })
}
