package com.example.motocare

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.databinding.ActivityAbastecimentoFormBinding
import com.example.motocare.model.Abastecimento
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.aplicarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AbastecimentoFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAbastecimentoFormBinding
    private lateinit var banco: MotoCareDatabase
    private var usuarioId = 0L
    private var motoId = 0L
    private var registroId = 0L
    private var dataRegistro = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioId = SessionManager(this).exigirUsuario(this) ?: return
        motoId = intent.getLongExtra(MotoFormActivity.EXTRA_MOTO_ID, 0L)
        registroId = intent.getLongExtra(EXTRA_ABASTECIMENTO_ID, 0L)
        banco = MotoCareDatabase.getInstance(this)
        dataRegistro = savedInstanceState?.getString("data_registro")
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        enableEdgeToEdge()
        binding = ActivityAbastecimentoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        binding.toolbar.setTitle(if (registroId == 0L) R.string.new_fuel else R.string.edit_fuel)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.etData.setText(dataRegistro.split("-").reversed().joinToString("/"))
        binding.etData.setOnClickListener { escolherData() }
        binding.etLitros.doAfterTextChanged { calcularTotal() }
        binding.etPreco.doAfterTextChanged { calcularTotal() }
        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnExcluir.isVisible = registroId != 0L
        binding.btnExcluir.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_fuel_title)
                .setMessage(R.string.delete_fuel_message)
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
                    val item = banco.abastecimentosDao().buscarPorId(registroId, motoId)
                    checkNotNull(item)
                    if (preencher) {
                        dataRegistro = item.data
                        binding.etData.setText(item.data.split("-").reversed().joinToString("/"))
                        binding.etKm.setText(item.quilometragem.toString())
                        binding.etLitros.setText(item.litros.toString())
                        binding.etPreco.setText(item.valorPorLitro.toString())
                    }
                } else if (preencher) {
                    binding.etKm.setText(moto.quilometragemAtual.toString())
                }
                calcularTotal()
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

    // Aceita vírgula ou ponto como separador decimal.
    private fun numero(texto: CharSequence?): Double? =
        texto?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull()
            ?.takeIf { it.isFinite() && it > 0 }

    private fun calcularTotal() {
        val litros = numero(binding.etLitros.text)
        val preco = numero(binding.etPreco.text)
        val total = if (litros != null && preco != null) litros * preco else null
        binding.etTotal.setText(if (total != null && total.isFinite())
            String.format(Locale.forLanguageTag("pt-BR"), "%.2f", total) else "")
    }

    private fun salvar() {
        val km = binding.etKm.text.toString().trim().toIntOrNull()
        val litros = numero(binding.etLitros.text)
        val preco = numero(binding.etPreco.text)
        val total = numero(binding.etTotal.text)
        binding.tilKm.error = if (km == null || km < 0) getString(R.string.invalid_moto_km) else null
        binding.tilLitros.error = if (litros == null) getString(R.string.positive_number) else null
        binding.tilPreco.error = if (preco == null) getString(R.string.positive_number) else null
        binding.tilTotal.error = if (total == null) getString(R.string.positive_number) else null
        if (km == null || km < 0 || litros == null || preco == null || total == null) return
        val item = Abastecimento(registroId, motoId, dataRegistro, km, litros, preco, total)
        ocupado(true)
        lifecycleScope.launch {
            try {
                // Reconfere o proprietário e salva junto com a quilometragem.
                banco.withTransaction {
                    checkNotNull(banco.motoDao().buscarPorId(motoId, usuarioId))
                    if (registroId == 0L) {
                        check(banco.abastecimentosDao().inserir(item) > 0)
                    } else {
                        checkNotNull(banco.abastecimentosDao().buscarPorId(registroId, motoId))
                        check(banco.abastecimentosDao().atualizar(item) == 1)
                    }
                    check(banco.motoDao().registrarQuilometragem(motoId, usuarioId, km) == 1)
                }
                Toast.makeText(this@AbastecimentoFormActivity, R.string.fuel_saved, Toast.LENGTH_SHORT).show()
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
                    val item = banco.abastecimentosDao().buscarPorId(registroId, motoId)
                    checkNotNull(item)
                    check(banco.abastecimentosDao().excluir(item) == 1)
                }
                Toast.makeText(this@AbastecimentoFormActivity, R.string.fuel_deleted, Toast.LENGTH_SHORT).show()
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
        listOf(binding.etData, binding.etKm, binding.etLitros, binding.etPreco).forEach { it.isEnabled = !ativo }
    }

    private fun mostrarErro(e: Exception) {
        Log.e("MotoCare", "Falha no abastecimento", e)
        Toast.makeText(this, R.string.database_error, Toast.LENGTH_LONG).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("data_registro", dataRegistro)
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val EXTRA_ABASTECIMENTO_ID = "ABASTECIMENTO_ID"
    }
}
