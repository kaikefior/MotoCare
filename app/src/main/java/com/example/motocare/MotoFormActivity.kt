package com.example.motocare

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.databinding.ActivityMotoFormBinding
import com.example.motocare.model.Moto
import com.example.motocare.database.MotoNaoEncontradaException
import com.example.motocare.database.MotoDao
import com.example.motocare.database.PlacaDuplicadaException
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.aplicarInsets
import com.example.motocare.validation.CampoMoto
import com.example.motocare.validation.ErroMoto
import com.example.motocare.validation.MotoValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Calendar

class MotoFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMotoFormBinding
    private lateinit var motoDao: MotoDao
    private var usuarioId = 0L
    private var motoId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioId = SessionManager(this).exigirUsuario(this) ?: return
        motoId = intent.getLongExtra(EXTRA_MOTO_ID, 0L)
        enableEdgeToEdge()
        binding = ActivityMotoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        motoDao = MotoCareDatabase.getInstance(this).motoDao()
        binding.toolbar.setTitle(if (motoId == 0L) R.string.new_moto else R.string.edit_moto)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSalvarMoto.setOnClickListener { salvar() }
        if (motoId != 0L) carregar(preencher = savedInstanceState == null)
    }

    private fun carregar(preencher: Boolean) {
        ocupado(true)
        lifecycleScope.launch {
            try {
                val moto = motoDao.buscarPorId(motoId, usuarioId) ?: throw MotoNaoEncontradaException()
                if (preencher) {
                    binding.etMarca.setText(moto.marca)
                    binding.etModelo.setText(moto.modelo)
                    binding.etAno.setText(moto.ano.toString())
                    binding.etQuilometragem.setText(moto.quilometragemAtual.toString())
                    binding.etPlaca.setText(moto.placa)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MotoCare", "Falha ao carregar moto", e)
                Toast.makeText(this@MotoFormActivity, R.string.moto_load_error, Toast.LENGTH_LONG).show()
                finish()
            } finally {
                ocupado(false)
            }
        }
    }

    private fun salvar() {
        val marca = binding.etMarca.text.toString().trim()
        val modelo = binding.etModelo.text.toString().trim()
        val ano = binding.etAno.text.toString().trim().toIntOrNull()
        val km = binding.etQuilometragem.text.toString().trim().toIntOrNull()
        val placa = MotoValidator.normalizarPlaca(binding.etPlaca.text.toString())
        val campos = mapOf(
            CampoMoto.MARCA to binding.tilMarca, CampoMoto.MODELO to binding.tilModelo,
            CampoMoto.ANO to binding.tilAno, CampoMoto.QUILOMETRAGEM to binding.tilQuilometragem,
            CampoMoto.PLACA to binding.tilPlaca
        )
        campos.values.forEach { it.error = null }
        val erros = MotoValidator.validar(marca, modelo, ano, km, placa,
            Calendar.getInstance().get(Calendar.YEAR))
        erros.forEach { (campo, erro) ->
            campos.getValue(campo).error = getString(when (erro) {
                ErroMoto.OBRIGATORIO -> R.string.required_field
                ErroMoto.ANO_INVALIDO -> R.string.invalid_moto_year
                ErroMoto.KM_INVALIDA -> R.string.invalid_moto_km
                ErroMoto.PLACA_INVALIDA -> R.string.invalid_moto_plate
            })
        }
        if (erros.isNotEmpty()) return

        val moto = Moto(motoId, usuarioId, marca, modelo, requireNotNull(ano), requireNotNull(km), placa)
        ocupado(true)
        lifecycleScope.launch {
            try {
                motoDao.salvar(moto)
                Toast.makeText(this@MotoFormActivity, R.string.moto_saved, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: PlacaDuplicadaException) {
                binding.tilPlaca.error = getString(R.string.duplicate_moto_plate)
            } catch (e: MotoNaoEncontradaException) {
                Toast.makeText(this@MotoFormActivity, R.string.moto_load_error, Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Log.e("MotoCare", "Falha ao salvar moto", e)
                Toast.makeText(this@MotoFormActivity, R.string.database_error, Toast.LENGTH_LONG).show()
            } finally {
                ocupado(false)
            }
        }
    }

    private fun ocupado(ativo: Boolean) {
        binding.progress.isVisible = ativo
        binding.btnSalvarMoto.isEnabled = !ativo
        listOf(binding.etMarca, binding.etModelo, binding.etAno, binding.etQuilometragem, binding.etPlaca)
            .forEach { it.isEnabled = !ativo }
    }

    companion object {
        const val EXTRA_MOTO_ID = "MOTO_ID"
    }
}
