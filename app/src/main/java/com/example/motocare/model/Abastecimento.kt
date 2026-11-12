package com.example.motocare.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "abastecimentos",
    foreignKeys = [
        ForeignKey(
            entity = Moto::class,
            parentColumns = ["id"],
            childColumns = ["motoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("motoId")]
)
    data class Abastecimento(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        val motoId: Long,
        val data: String,
        val quilometragem: Int,
        val litros: Double,
        val valorPorLitro: Double,
        val valorTotal: Double
    )