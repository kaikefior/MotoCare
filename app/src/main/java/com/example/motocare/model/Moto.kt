package com.example.motocare.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Cada objeto desta classe corresponde a uma linha da tabela motos.
@Entity(tableName = "motos", indices = [Index("usuarioId")])
data class Moto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioId: Long,
    val marca: String,
    val modelo: String,
    val ano: Int,
    val quilometragemAtual: Int,
    val placa: String = ""
)
