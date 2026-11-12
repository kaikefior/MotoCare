package com.example.motocare.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "manutencoes",
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
data class Manutencao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val motoId: Long,
    val data: String,
    val quilometragem: Int,
    val servico: String,
    val custo: Double,
    val observacao: String = "",
    val proximaData: String? = null,
    val proximaQuilometragem: Int? = null,
    @ColumnInfo(defaultValue = "0")
    val previsaoConcluida: Boolean = false
)
