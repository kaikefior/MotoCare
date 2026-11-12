package com.example.motocare.validation

import java.util.Locale

enum class CampoMoto { MARCA, MODELO, ANO, QUILOMETRAGEM, PLACA }
enum class ErroMoto { OBRIGATORIO, ANO_INVALIDO, KM_INVALIDA, PLACA_INVALIDA }

object MotoValidator {
    private val formatoPlaca = Regex("[A-Z]{3}([0-9]{4}|[0-9][A-Z][0-9]{2})")

    fun normalizarPlaca(placa: String): String =
        placa.trim().uppercase(Locale.ROOT).replace("-", "").replace(" ", "")

    fun validar(
        marca: String,
        modelo: String,
        ano: Int?,
        quilometragem: Int?,
        placa: String,
        anoAtual: Int
    ): Map<CampoMoto, ErroMoto> = buildMap {
        if (marca.isBlank()) put(CampoMoto.MARCA, ErroMoto.OBRIGATORIO)
        if (modelo.isBlank()) put(CampoMoto.MODELO, ErroMoto.OBRIGATORIO)
        if (ano == null || ano !in 1900..(anoAtual + 1)) put(CampoMoto.ANO, ErroMoto.ANO_INVALIDO)
        if (quilometragem == null || quilometragem < 0) put(CampoMoto.QUILOMETRAGEM, ErroMoto.KM_INVALIDA)
        val normalizada = normalizarPlaca(placa)
        if (normalizada.isNotEmpty() && !formatoPlaca.matches(normalizada)) {
            put(CampoMoto.PLACA, ErroMoto.PLACA_INVALIDA)
        }
    }
}
