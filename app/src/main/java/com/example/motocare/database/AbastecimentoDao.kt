package com.example.motocare.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.motocare.model.Abastecimento
import kotlinx.coroutines.flow.Flow

@Dao
interface AbastecimentosDao {
    
    @Insert
    suspend fun inserir(abastecimento: Abastecimento): Long
    
    @Query("""
        SELECT * FROM abastecimentos
        WHERE motoId = :motoId
        ORDER BY data DESC, id DESC
    """)
    
    fun observarPorMoto(motoId: Long): Flow<List<Abastecimento>>
    
     @Query("""
        SELECT * FROM abastecimentos
        WHERE id = :id AND motoId = :motoId
    """)
    
    suspend fun buscarPorId(
        id: Long,
        motoId: Long
    ): Abastecimento?
        
    @Update
    suspend fun atualizar(abastecimento: Abastecimento): Int
    
    @Delete
    suspend fun excluir(abastecimento: Abastecimento): Int
}
    