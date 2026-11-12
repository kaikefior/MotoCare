package com.example.motocare.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.motocare.model.Moto
import androidx.room.AutoMigration
import com.example.motocare.model.Abastecimento
import com.example.motocare.model.Manutencao

@Database(
    entities = [Moto::class, Abastecimento::class, Manutencao::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class MotoCareDatabase : RoomDatabase() {
    abstract fun motoDao(): MotoDao
    abstract fun abastecimentosDao(): AbastecimentosDao
    abstract fun manutencaoDao(): ManutencaoDao

    companion object {
        @Volatile private var instance: MotoCareDatabase? = null

        fun getInstance(context: Context): MotoCareDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MotoCareDatabase::class.java,
                    "motocare_motos.db"
                ).build().also { instance = it }
            }
    }
}
