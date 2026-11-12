package com.example.motocare.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.motocare.model.Manutencao
import kotlinx.coroutines.flow.Flow

@Dao
interface ManutencaoDao {
    @Insert
    suspend fun inserir(manutencao: Manutencao): Long

    @Query("SELECT * FROM manutencoes WHERE motoId = :motoId ORDER BY data DESC, id DESC")
    fun observarPorMoto(motoId: Long): Flow<List<Manutencao>>

    @Query("SELECT * FROM manutencoes WHERE id = :id AND motoId = :motoId")
    suspend fun buscarPorId(id: Long, motoId: Long): Manutencao?

    @Update
    suspend fun atualizar(manutencao: Manutencao): Int

    @Delete
    suspend fun excluir(manutencao: Manutencao): Int
}
