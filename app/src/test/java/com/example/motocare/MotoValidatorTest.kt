package com.example.motocare

import com.example.motocare.validation.CampoMoto
import com.example.motocare.validation.MotoValidator
import org.junit.Assert.*
import org.junit.Test

class MotoValidatorTest {
    @Test fun aceitaPlacasAntigaMercosulEOpcional() {
        listOf("", "ABC1234", "ABC1D23", "abc-1234").forEach { placa ->
            assertTrue(MotoValidator.validar("Honda", "CG 160", 2024, 0, placa, 2026).isEmpty())
        }
    }

    @Test fun exigeMarcaEModeloSemAceitarEspacos() {
        val erros = MotoValidator.validar(" ", "", 2024, 100, "", 2026)
        assertEquals(setOf(CampoMoto.MARCA, CampoMoto.MODELO), erros.keys)
    }

    @Test fun rejeitaCamposNumericosAusentesOuInvalidos() {
        val erros = MotoValidator.validar("Honda", "CG", null, null, "", 2026)
        assertTrue(erros.containsKey(CampoMoto.ANO))
        assertTrue(erros.containsKey(CampoMoto.QUILOMETRAGEM))
        assertTrue(MotoValidator.validar("Honda", "CG", 2024, -1, "", 2026)
            .containsKey(CampoMoto.QUILOMETRAGEM))
    }

    @Test fun limitaAnoAoProximoAno() {
        assertTrue(MotoValidator.validar("Honda", "CG", 2027, 0, "", 2026).isEmpty())
        assertTrue(MotoValidator.validar("Honda", "CG", 2028, 0, "", 2026).containsKey(CampoMoto.ANO))
        assertTrue(MotoValidator.validar("Honda", "CG", 1899, 0, "", 2026).containsKey(CampoMoto.ANO))
    }

    @Test fun rejeitaPlacaForaDosFormatosAceitos() {
        listOf("1234567", "ABCDEFG", "ABC12D3", "ABC1D2").forEach {
            assertTrue(MotoValidator.validar("Honda", "CG", 2024, 0, it, 2026).containsKey(CampoMoto.PLACA))
        }
    }

    @Test fun normalizaPlacaAntesDeCompararDuplicidade() {
        assertEquals("ABC1234", MotoValidator.normalizarPlaca(" abc-1234 "))
    }
}
