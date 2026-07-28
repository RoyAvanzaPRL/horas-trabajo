package com.horastrabajo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.horastrabajo.app.data.local.entity.EntradaHorasEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface EntradaHorasDao {

    @Query(
        "SELECT * FROM entrada_horas WHERE trabajoId = :trabajoId AND fecha BETWEEN :desde AND :hasta " +
            "ORDER BY fecha ASC, horaEntrada ASC"
    )
    fun observeByTrabajoYRango(trabajoId: Long, desde: LocalDate, hasta: LocalDate): Flow<List<EntradaHorasEntity>>

    @Query("SELECT * FROM entrada_horas WHERE trabajoId = :trabajoId AND fecha = :fecha ORDER BY horaEntrada ASC")
    fun observeByTrabajoYFecha(trabajoId: Long, fecha: LocalDate): Flow<List<EntradaHorasEntity>>

    @Query("SELECT * FROM entrada_horas ORDER BY trabajoId ASC, fecha ASC, horaEntrada ASC")
    fun observeAll(): Flow<List<EntradaHorasEntity>>

    /** Todas las entradas de un trabajo, sin límite de fecha. Solo para backup/export. */
    @Query("SELECT * FROM entrada_horas WHERE trabajoId = :trabajoId")
    suspend fun getAllByTrabajo(trabajoId: Long): List<EntradaHorasEntity>

    @Insert
    suspend fun insert(entrada: EntradaHorasEntity): Long

    @Insert
    suspend fun insertAll(entradas: List<EntradaHorasEntity>)

    @Update
    suspend fun update(entrada: EntradaHorasEntity)

    @Delete
    suspend fun delete(entrada: EntradaHorasEntity)
}
