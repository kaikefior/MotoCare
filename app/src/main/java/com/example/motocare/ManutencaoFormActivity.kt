package com.example.motocare

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.databinding.ActivityManutencaoFormBinding
import com.example.motocare.model.Manutencao
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.aplicarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManutencaoFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManutencaoFormBinding
    private lateinit var banco: MotoCareDatabase
    private var usuarioId = 0L
    private var motoId = 0L
    private var registroId = 0L
    private var dataRegistro = ""
    private var proximaData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioId = SessionManager(this).exigirUsuario(this) ?: return
        motoId = intent.getLongExtra(MotoFormActivity.EXTRA_MOTO_ID, 0L)
        registroId = intent.getLongExtra(EXTRA_MANUTENCAO_ID, 0L)
        banco = MotoCareDatabase.getInstance(this)
        dataRegistro = savedInstanceState?.getString("data_registro")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        proximaData = savedInstanceState?.getString("proxima_data")
        enableEdgeToEdge()
        binding = ActivityManutencaoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        binding.toolbar.setTitle(if (registroId == 0L) R.string.new_maintenance else R.string.edit_maintenance)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.etData.setText(dataRegistro.split("-").reversed().joinToString("/"))
        binding.etData.setOnClickListener { escolherData() }
        binding.etProximaData.setOnClickListener { escolherProximaData() }
        binding.tilProximaData.setEndIconOnClickListener {
            proximaData = null
            mostrarPrevisao()
        }
        binding.etProximaKm.doAfterTextChanged { mostrarPrevisao() }
        mostrarPrevisao()
        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnExcluir.isVisible = registroId != 0L
        binding.btnExcluir.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_maintenance_title)
                .setMessage(R.string.delete_maintenance_message)
                .setNegativeButton(R.string.btn_cancelar, null)
                .setPositiveButton(R.string.btn_excluir) { _, _ -> excluir() }
                .show()
        }
        carregar(preencher = savedInstanceState == null)
    }

    private fun carregar(preencher: Boolean) {
        ocupado(true)
        lifecycleScope.launch {
            try {
                val moto = banco.motoDao().buscarPorId(motoId, usuarioId)
                checkNotNull(moto)
                binding.tvMoto.text = getString(R.string.moto_name, moto.marca, moto.modelo)
                if (registroId != 0L) {
                    val item = banco.manutencaoDao().buscarPorId(registroId, motoId)
                    checkNotNull(item)
                    if (preencher) {
                        dataRegistro = item.data
                        binding.etData.setText(item.data.split("-").reversed().joinToString("/"))
                        binding.etKm.setText(item.quilometragem.toString())
                        binding.etServico.setText(item.servico)
                        binding.etCusto.setText(item.custo.toString())
                        binding.etObservacao.setText(item.observacao)
                        proximaData = item.proximaData
                        binding.etProximaKm.setText(item.proximaQuilometragem?.toString().orEmpty())
                        binding.cbPrevisaoConcluida.isChecked = item.previsaoConcluida
                        mostrarPrevisao()
                    }
                } else if (preencher) {
                    binding.etKm.setText(moto.quilometragemAtual.toString())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarErro(e)
                finish()
            } finally {
                ocupado(false)
            }
        }
    }

    private fun escolherData() {
        val partes = dataRegistro.split("-").map { it.toInt() }
        DatePickerDialog(this, { _, ano, mes, dia ->
            dataRegistro = String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia)
            binding.etData.setText(dataRegistro.split("-").reversed().joinToString("/"))
        }, partes[0], partes[1] - 1, partes[2]).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun escolherProximaData() {
        val partes = (proximaData ?: dataRegistro).split("-").map { it.toInt() }
        DatePickerDialog(this, { _, ano, mes, dia ->
            proximaData = String.format(Locale.US, "%04d-%02d-%02d", ano, mes + 1, dia)
            mostrarPrevisao()
        }, partes[0], partes[1] - 1, partes[2]).show()
    }

    private fun mostrarPrevisao() {
        binding.etProximaData.setText(proximaData?.split("-")?.reversed()?.joinToString("/").orEmpty())
        binding.tilProximaData.isEndIconVisible = proximaData != null
        binding.cbPrevisaoConcluida.isVisible = proximaData != null || !binding.etProximaKm.text.isNullOrBlank()
    }

    private fun salvar() {
        val km = binding.etKm.text.toString().trim().toIntOrNull()
        val servico = binding.etServico.text.toString().trim()
        val custo = binding.etCusto.text.toString().trim().replace(',', '.').toDoubleOrNull()
            ?.takeIf { it.isFinite() && it >= 0 }
        val observacao = binding.etObservacao.text.toString().trim()
        val proximaKmTexto = binding.etProximaKm.text.toString().trim()
        val proximaKm = proximaKmTexto.toIntOrNull()
        val dataValida = proximaData?.let { it > dataRegistro } ?: true
        val kmValida = proximaKmTexto.isEmpty() || (proximaKm != null && km != null && proximaKm > km)
        binding.tilKm.error = if (km == null || km < 0) getString(R.string.invalid_moto_km) else null
        binding.tilServico.error = if (servico.isBlank()) getString(R.string.required_field) else null
        binding.tilCusto.error = if (custo == null) getString(R.string.maintenance_cost_error) else null
        binding.tilProximaData.error = if (!dataValida) getString(R.string.schedule_date_error) else null
        binding.tilProximaKm.error = if (!kmValida) getString(R.string.schedule_km_error) else null
        if (km == null || km < 0 || servico.isBlank() || custo == null || !dataValida || !kmValida) return
        val item = Manutencao(
            registroId, motoId, dataRegistro, km, servico, custo, observacao,
            proximaData = proximaData,
            proximaQuilometragem = proximaKm,
            previsaoConcluida = (proximaData != null || proximaKm != null) && binding.cbPrevisaoConcluida.isChecked
        )
        ocupado(true)
        lifecycleScope.launch {
            try {
                // Reconfere o proprietário e salva junto com a quilometragem.
                banco.withTransaction {
                    checkNotNull(banco.motoDao().buscarPorId(motoId, usuarioId))
                    if (registroId == 0L) {
                        check(banco.manutencaoDao().inserir(item) > 0)
                    } else {
                        checkNotNull(banco.manutencaoDao().buscarPorId(registroId, motoId))
                        check(banco.manutencaoDao().atualizar(item) == 1)
                    }
                    check(banco.motoDao().registrarQuilometragem(motoId, usuarioId, km) == 1)
                }
                Toast.makeText(this@ManutencaoFormActivity, R.string.maintenance_saved, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarErro(e)
            } finally {
                ocupado(false)
            }
        }
    }

    private fun excluir() {
        ocupado(true)
        lifecycleScope.launch {
            try {
                banco.withTransaction {
                    checkNotNull(banco.motoDao().buscarPorId(motoId, usuarioId))
                    val item = banco.manutencaoDao().buscarPorId(registroId, motoId)
                    checkNotNull(item)
                    check(banco.manutencaoDao().excluir(item) == 1)
                }
                Toast.makeText(this@ManutencaoFormActivity, R.string.maintenance_deleted, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mostrarErro(e)
            } finally {
                ocupado(false)
            }
        }
    }

    private fun ocupado(ativo: Boolean) {
        binding.btnSalvar.isEnabled = !ativo
        binding.btnExcluir.isEnabled = !ativo
        binding.cbPrevisaoConcluida.isEnabled = !ativo
        binding.tilProximaData.isEnabled = !ativo
        listOf(binding.etData, binding.etKm, binding.etServico, binding.etCusto, binding.etObservacao,
            binding.etProximaData, binding.etProximaKm).forEach { it.isEnabled = !ativo }
    }

    private fun mostrarErro(e: Exception) {
        Log.e("MotoCare", "Falha na manutenção", e)
        Toast.makeText(this, R.string.database_error, Toast.LENGTH_LONG).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("data_registro", dataRegistro)
        outState.putString("proxima_data", proximaData)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val EXTRA_MANUTENCAO_ID = "MANUTENCAO_ID"
    }
}
