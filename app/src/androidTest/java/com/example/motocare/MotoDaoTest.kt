package com.example.motocare

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.model.Moto
import com.example.motocare.database.MotoNaoEncontradaException
import com.example.motocare.database.MotoDao
import com.example.motocare.database.PlacaDuplicadaException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MotoDaoTest {
    private lateinit var database: MotoCareDatabase
    private lateinit var dao: MotoDao
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun abrir() {
        database = Room.inMemoryDatabaseBuilder(context, MotoCareDatabase::class.java).build()
        dao = database.motoDao()
    }

    @After fun fechar() { database.close() }

    @Test fun crudRespeitaOProprietario() = runBlocking<Unit> {
        val id = dao.salvar(Moto(usuarioId = 1, marca = "Honda", modelo = "CG", ano = 2024, quilometragemAtual = 100))
        assertEquals(1, dao.observarTodas(1).first().size)
        assertTrue(dao.observarTodas(2).first().isEmpty())
        assertNull(dao.buscarPorId(id, 2))
        val moto = requireNotNull(dao.buscarPorId(id, 1))
        dao.salvar(moto.copy(quilometragemAtual = 250))
        assertEquals(250, requireNotNull(dao.buscarPorId(id, 1)).quilometragemAtual)
        try {
            dao.salvar(moto.copy(usuarioId = 2))
            fail("Outra conta não pode editar a moto")
        } catch (_: MotoNaoEncontradaException) { }
        assertEquals(0, dao.excluir(id, 2))
        dao.excluir(id, 1)
        assertNull(dao.buscarPorId(id, 1))
    }

    @Test fun impedePlacaDuplicadaNaMesmaConta() = runBlocking<Unit> {
        val moto = Moto(usuarioId = 1, marca = "Honda", modelo = "CG", ano = 2024, quilometragemAtual = 0, placa = "abc-1234")
        val id = dao.salvar(moto)
        assertEquals("ABC1234", requireNotNull(dao.buscarPorId(id, 1)).placa)
        dao.salvar(moto.copy(id = id, placa = "ABC1234"))
        try {
            dao.salvar(moto.copy(placa = "ABC1234"))
            fail("Deve rejeitar a placa duplicada")
        } catch (_: PlacaDuplicadaException) { }
        assertTrue(dao.salvar(moto.copy(usuarioId = 2)) > 0)
        dao.salvar(moto.copy(placa = ""))
        dao.salvar(moto.copy(placa = ""))
    }

    @Test fun preservaCadastroAoReabrirBanco() = runBlocking<Unit> {
        val nome = "motos-test-" + UUID.randomUUID() + ".db"
        var persisted: MotoCareDatabase? = null
        try {
            persisted = Room.databaseBuilder(context, MotoCareDatabase::class.java, nome).build()
            val id = persisted.motoDao().salvar(
                Moto(usuarioId = 1, marca = "Yamaha", modelo = "Fazer", ano = 2024, quilometragemAtual = 300)
            )
            persisted.close()
            persisted = Room.databaseBuilder(context, MotoCareDatabase::class.java, nome).build()
            assertEquals("Fazer", persisted.motoDao().buscarPorId(id, 1)?.modelo)
        } finally {
            persisted?.close()
            context.deleteDatabase(nome)
        }
    }
}
