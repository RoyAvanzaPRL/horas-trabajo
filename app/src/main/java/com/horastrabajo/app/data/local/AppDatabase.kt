package com.horastrabajo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.horastrabajo.app.data.local.dao.DineroExtraDao
import com.horastrabajo.app.data.local.dao.EntradaHorasDao
import com.horastrabajo.app.data.local.dao.TarifaMensualDao
import com.horastrabajo.app.data.local.dao.TrabajoDao
import com.horastrabajo.app.data.local.entity.DineroExtraEntity
import com.horastrabajo.app.data.local.entity.EntradaHorasEntity
import com.horastrabajo.app.data.local.entity.TarifaMensualEntity
import com.horastrabajo.app.data.local.entity.TrabajoEntity

@Database(
    entities = [
        TrabajoEntity::class,
        EntradaHorasEntity::class,
        TarifaMensualEntity::class,
        DineroExtraEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trabajoDao(): TrabajoDao
    abstract fun entradaHorasDao(): EntradaHorasDao
    abstract fun tarifaMensualDao(): TarifaMensualDao
    abstract fun dineroExtraDao(): DineroExtraDao

    companion object {
        private const val DB_NAME = "horas_trabajo.db"

        /** Añade el nombre de la persona por Trabajo (campo nuevo, obligatorio en la app). */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trabajo ADD COLUMN nombreUsuario TEXT NOT NULL DEFAULT ''")
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
