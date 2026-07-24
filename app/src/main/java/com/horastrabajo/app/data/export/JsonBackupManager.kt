package com.horastrabajo.app.data.export

import com.horastrabajo.app.data.export.dto.BackupDto
import com.horastrabajo.app.data.export.dto.DineroExtraDto
import com.horastrabajo.app.data.export.dto.EntradaHorasDto
import com.horastrabajo.app.data.export.dto.TarifaMensualDto
import com.horastrabajo.app.data.export.dto.TrabajoDto
import com.horastrabajo.app.data.repository.DineroExtraRepository
import com.horastrabajo.app.data.repository.EntradaHorasRepository
import com.horastrabajo.app.data.repository.TarifaMensualRepository
import com.horastrabajo.app.data.repository.TrabajoRepository
import com.horastrabajo.app.domain.model.Dinero
import com.horastrabajo.app.domain.model.DineroExtra
import com.horastrabajo.app.domain.model.EntradaHoras
import com.horastrabajo.app.domain.model.TarifaMensual
import com.horastrabajo.app.domain.model.Trabajo
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime

/**
 * Backup completo de TODOS los trabajos en JSON (formato de import/export real,
 * para no perder datos al cambiar de móvil). Importar siempre crea trabajos NUEVOS
 * (no intenta fusionar con trabajos existentes del mismo nombre).
 */
class JsonBackupManager(
    private val trabajoRepository: TrabajoRepository,
    private val entradaHorasRepository: EntradaHorasRepository,
    private val tarifaMensualRepository: TarifaMensualRepository,
    private val dineroExtraRepository: DineroExtraRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportarTodo(): String {
        val trabajos = trabajoRepository.observeAll().first()
        val trabajosDto = trabajos.map { trabajo ->
            TrabajoDto(
                id = trabajo.id,
                nombre = trabajo.nombre,
                nombreUsuario = trabajo.nombreUsuario,
                simboloMoneda = trabajo.simboloMoneda,
                entradas = entradaHorasRepository.obtenerTodasDelTrabajo(trabajo.id).map { it.toDto() },
                tarifas = tarifaMensualRepository.obtenerTodasDelTrabajo(trabajo.id).map {
                    TarifaMensualDto(
                        id = it.id,
                        anio = it.anio,
                        mes = it.mes,
                        precioPorHoraCentimos = it.precioPorHora.centimos,
                    )
                },
                dineroExtra = dineroExtraRepository.obtenerTodoDelTrabajo(trabajo.id).map { it.toDto() },
            )
        }
        return json.encodeToString(BackupDto.serializer(), BackupDto(trabajos = trabajosDto))
    }

    suspend fun importarTodo(contenidoJson: String) {
        val backup = json.decodeFromString(BackupDto.serializer(), contenidoJson)
        for (trabajoDto in backup.trabajos) {
            val nuevoTrabajoId = trabajoRepository.guardar(
                Trabajo(
                    nombre = trabajoDto.nombre,
                    nombreUsuario = trabajoDto.nombreUsuario,
                    simboloMoneda = trabajoDto.simboloMoneda,
                )
            )
            entradaHorasRepository.guardarVarias(trabajoDto.entradas.map { it.toDomain(nuevoTrabajoId) })
            tarifaMensualRepository.guardarVarias(
                trabajoDto.tarifas.map { dto ->
                    TarifaMensual(
                        trabajoId = nuevoTrabajoId,
                        anio = dto.anio,
                        mes = dto.mes,
                        precioPorHora = Dinero(dto.precioPorHoraCentimos),
                    )
                }
            )
            dineroExtraRepository.guardarVarios(trabajoDto.dineroExtra.map { it.toDomain(nuevoTrabajoId) })
        }
    }
}

private fun EntradaHoras.toDto() = EntradaHorasDto(
    id = id,
    fechaIso = fecha.toString(),
    horaEntradaIso = horaEntrada.toString(),
    horaSalidaIso = horaSalida.toString(),
    esDiaSiguiente = esDiaSiguiente,
    notas = notas,
)

private fun EntradaHorasDto.toDomain(trabajoId: Long) = EntradaHoras(
    trabajoId = trabajoId,
    fecha = LocalDate.parse(fechaIso),
    horaEntrada = LocalTime.parse(horaEntradaIso),
    horaSalida = LocalTime.parse(horaSalidaIso),
    esDiaSiguiente = esDiaSiguiente,
    notas = notas,
)

private fun DineroExtra.toDto() = DineroExtraDto(
    id = id,
    fechaIso = fecha.toString(),
    montoCentimos = monto.centimos,
    descripcion = descripcion,
)

private fun DineroExtraDto.toDomain(trabajoId: Long) = DineroExtra(
    trabajoId = trabajoId,
    fecha = LocalDate.parse(fechaIso),
    monto = Dinero(montoCentimos),
    descripcion = descripcion,
)
