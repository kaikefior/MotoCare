package com.example.motocare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.motocare.model.Moto
import com.example.motocare.validation.MotoValidator
import kotlinx.coroutines.flow.Flow

class PlacaDuplicadaException : IllegalArgumentException()
class MotoNaoEncontradaException : IllegalStateException()

// O Room gera as implementações das funções abstratas a partir das anotações.
@Dao
abstract class MotoDao {
    @Query("SELECT * FROM motos WHERE usuarioId = :usuarioId ORDER BY marca COLLATE NOCASE, modelo COLLATE NOCASE, id DESC")
    abstract fun observarTodas(usuarioId: Long): Flow<List<Moto>>

    @Query("SELECT * FROM motos WHERE id = :id AND usuarioId = :usuarioId")
    abstract fun observarPorId(id: Long, usuarioId: Long): Flow<Moto?>

    @Query("SELECT * FROM motos WHERE id = :id AND usuarioId = :usuarioId")
    abstract suspend fun buscarPorId(id: Long, usuarioId: Long): Moto?

    @Query("SELECT COUNT(*) FROM motos WHERE usuarioId = :usuarioId AND placa = :placa AND id != :ignorarId")
    abstract suspend fun contarPlaca(usuarioId: Long, placa: String, ignorarId: Long): Int

    @Insert
    abstract suspend fun inserir(moto: Moto): Long

    @Query("""UPDATE motos SET marca = :marca, modelo = :modelo, ano = :ano,
        quilometragemAtual = :km, placa = :placa
        WHERE id = :id AND usuarioId = :usuarioId""")
    abstract suspend fun atualizar(id: Long, usuarioId: Long, marca: String, modelo: String, ano: Int, km: Int, placa: String): Int

    @Query("DELETE FROM motos WHERE id = :id AND usuarioId = :usuarioId")
    abstract suspend fun excluir(id: Long, usuarioId: Long): Int

    @Query("UPDATE motos SET quilometragemAtual = MAX(quilometragemAtual, :km) WHERE id = :id AND usuarioId = :usuarioId")
    abstract suspend fun registrarQuilometragem(id: Long, usuarioId: Long, km: Int): Int

    // A conferência da placa e a gravação acontecem juntas, em uma transação.
    @Transaction
    open suspend fun salvar(moto: Moto): Long {
        require(moto.usuarioId > 0)
        val item = moto.copy(
            marca = moto.marca.trim(),
            modelo = moto.modelo.trim(),
            placa = MotoValidator.normalizarPlaca(moto.placa)
        )
        if (item.placa.isNotEmpty() && contarPlaca(item.usuarioId, item.placa, item.id) > 0) {
            throw PlacaDuplicadaException()
        }
        if (item.id == 0L) return inserir(item)
        val alteradas = atualizar(item.id, item.usuarioId, item.marca, item.modelo,
            item.ano, item.quilometragemAtual, item.placa)
        if (alteradas != 1) throw MotoNaoEncontradaException()
        return item.id
    }
}
