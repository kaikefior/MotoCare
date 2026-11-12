package com.example.motocare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.databinding.ActivityMotoDetailBinding
import com.example.motocare.databinding.ItemAbastecimentoBinding
import com.example.motocare.model.Abastecimento
import com.example.motocare.model.Manutencao
import com.example.motocare.databinding.ItemManutencaoBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import com.google.android.material.color.MaterialColors
import java.util.Locale
import com.example.motocare.database.MotoDao
import com.example.motocare.database.MotoNaoEncontradaException
import com.example.motocare.session.SessionManager
import com.example.motocare.ui.aplicarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MotoDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMotoDetailBinding
    private lateinit var motoDao: MotoDao
    private var usuarioId = 0L
    private var motoId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usuarioId = SessionManager(this).exigirUsuario(this) ?: return
        motoId = intent.getLongExtra(MotoFormActivity.EXTRA_MOTO_ID, 0L)
        enableEdgeToEdge()
        binding = ActivityMotoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        aplicarInsets(binding.root)
        motoDao = MotoCareDatabase.getInstance(this).motoDao()
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnEditarMoto.setOnClickListener {
            startActivity(Intent(this, MotoFormActivity::class.java)
                .putExtra(MotoFormActivity.EXTRA_MOTO_ID, motoId))
        }
        binding.btnExcluirMoto.setOnClickListener { confirmarExclusao() }
        binding.btnNovoAbastecimento.setOnClickListener { abrirAbastecimento() }
        binding.btnNovaManutencao.setOnClickListener { abrirManutencao() }
        binding.btnEditarMoto.isEnabled = false
        binding.btnExcluirMoto.isEnabled = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    motoDao.observarPorId(motoId, usuarioId),
                    MotoCareDatabase.getInstance(this@MotoDetailActivity)
                        .abastecimentosDao().observarPorMoto(motoId),
                    MotoCareDatabase.getInstance(this@MotoDetailActivity)
                        .manutencaoDao().observarPorMoto(motoId)
                ) { moto, historico, manutencoes -> Triple(moto, historico, manutencoes) }.catch { e ->
                    Log.e("MotoCare", "Falha ao consultar moto", e)
                    Toast.makeText(this@MotoDetailActivity, R.string.moto_load_error, Toast.LENGTH_LONG).show()
                    finish()
                }.collect { (moto, historico, manutencoes) ->
                    if (moto == null) {
                        finish()
                    } else {
                        binding.progress.isVisible = false
                        binding.tvMotoNome.text = getString(R.string.moto_name, moto.marca, moto.modelo)
                        binding.tvMotoAno.text = getString(R.string.moto_year, moto.ano)
                        binding.tvMotoKm.text = getString(R.string.moto_km, moto.quilometragemAtual)
                        binding.tvMotoPlaca.text = moto.placa.ifBlank { getString(R.string.no_plate) }
                        binding.btnNovoAbastecimento.isEnabled = true
                        binding.btnNovaManutencao.isEnabled = true
                        mostrarHistorico(historico)
                        mostrarManutencoes(manutencoes)
                        mostrarPrevisoes(manutencoes, moto.quilometragemAtual)
                        val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
                        val combustivel = historico.sumOf { it.valorTotal }
                        val servicos = manutencoes.sumOf { it.custo }
                        binding.tvResumoGastos.text = getString(R.string.expenses_summary,
                            moeda.format(combustivel), moeda.format(servicos), moeda.format(combustivel + servicos))
                        binding.btnEditarMoto.isEnabled = true
                        binding.btnExcluirMoto.isEnabled = true
                    }
                }
            }
        }
    }

    private fun abrirAbastecimento(id: Long = 0L) {
        startActivity(Intent(this, AbastecimentoFormActivity::class.java)
            .putExtra(MotoFormActivity.EXTRA_MOTO_ID, motoId)
            .putExtra(AbastecimentoFormActivity.EXTRA_ABASTECIMENTO_ID, id))
    }

    private fun mostrarHistorico(historico: List<Abastecimento>) {
        val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        binding.tvResumoCombustivel.text = getString(R.string.fuel_summary,
            moeda.format(historico.sumOf { it.valorTotal }), historico.sumOf { it.litros })
        binding.tvHistoricoVazio.isVisible = historico.isEmpty()
        binding.containerAbastecimentos.removeAllViews()
        historico.forEach { item ->
            val card = ItemAbastecimentoBinding.inflate(layoutInflater, binding.containerAbastecimentos, false)
            card.tvDataKm.text = getString(R.string.fuel_item_date,
                item.data.split("-").reversed().joinToString("/"), item.quilometragem)
            card.tvVolumePreco.text = getString(R.string.fuel_item_volume, item.litros, item.valorPorLitro)
            card.tvTotal.text = moeda.format(item.valorTotal)
            card.root.setOnClickListener { abrirAbastecimento(item.id) }
            binding.containerAbastecimentos.addView(card.root)
        }
    }

    private fun abrirManutencao(id: Long = 0L) {
        startActivity(Intent(this, ManutencaoFormActivity::class.java)
            .putExtra(MotoFormActivity.EXTRA_MOTO_ID, motoId)
            .putExtra(ManutencaoFormActivity.EXTRA_MANUTENCAO_ID, id))
    }

    private fun mostrarManutencoes(historico: List<Manutencao>) {
        val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        binding.tvResumoManutencoes.text = getString(R.string.maintenance_summary,
            historico.size, moeda.format(historico.sumOf { it.custo }))
        binding.tvManutencoesVazio.isVisible = historico.isEmpty()
        binding.containerManutencoes.removeAllViews()
        historico.forEach { item ->
            val card = ItemManutencaoBinding.inflate(layoutInflater, binding.containerManutencoes, false)
            card.tvServico.text = item.servico
            card.tvDataKm.text = getString(R.string.fuel_item_date,
                item.data.split("-").reversed().joinToString("/"), item.quilometragem)
            card.tvObservacao.text = item.observacao
            card.tvObservacao.isVisible = item.observacao.isNotBlank()
            card.tvCusto.text = moeda.format(item.custo)
            card.tvPrevisao.isVisible = item.proximaData != null || item.proximaQuilometragem != null
            card.tvPrevisao.text = if (item.previsaoConcluida) getString(R.string.schedule_finished)
                else prazoPrevisto(item)
            card.root.setOnClickListener { abrirManutencao(item.id) }
            binding.containerManutencoes.addView(card.root)
        }
    }

    private fun prazoPrevisto(item: Manutencao): String = listOfNotNull(
        item.proximaData?.let { getString(R.string.schedule_date, it.split("-").reversed().joinToString("/")) },
        item.proximaQuilometragem?.let { getString(R.string.schedule_km, it) }
    ).joinToString(" • ")

    private fun situacaoPrevisao(item: Manutencao, kmAtual: Int, hoje: String, limite: String): Int {
        val data = item.proximaData
        val km = item.proximaQuilometragem
        return when {
            (data != null && data < hoje) || (km != null && kmAtual > km) -> R.string.schedule_overdue
            (data != null && data <= limite) || (km != null && km - kmAtual <= 500) -> R.string.schedule_near
            else -> R.string.schedule_on_time
        }
    }

    private fun mostrarPrevisoes(historico: List<Manutencao>, kmAtual: Int) {
        val calendario = Calendar.getInstance()
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val hoje = formato.format(calendario.time)
        calendario.add(Calendar.DAY_OF_YEAR, 7)
        val limite = formato.format(calendario.time)
        val pendentes = historico.filter {
            !it.previsaoConcluida && (it.proximaData != null || it.proximaQuilometragem != null)
        }.sortedBy {
            when (situacaoPrevisao(it, kmAtual, hoje, limite)) {
                R.string.schedule_overdue -> 0
                R.string.schedule_near -> 1
                else -> 2
            }
        }
        binding.tvPrevisoesVazio.isVisible = pendentes.isEmpty()
        binding.containerPrevisoes.removeAllViews()
        pendentes.forEach { item ->
            val card = ItemManutencaoBinding.inflate(layoutInflater, binding.containerPrevisoes, false)
            val situacao = situacaoPrevisao(item, kmAtual, hoje, limite)
            card.tvServico.text = item.servico
            card.tvDataKm.text = prazoPrevisto(item)
            card.tvCusto.isVisible = false
            card.tvPrevisao.isVisible = true
            card.tvPrevisao.setText(situacao)
            val cor = when (situacao) {
                R.string.schedule_overdue -> androidx.appcompat.R.attr.colorError
                R.string.schedule_near -> com.google.android.material.R.attr.colorTertiary
                else -> androidx.appcompat.R.attr.colorPrimary
            }
            card.tvPrevisao.setTextColor(MaterialColors.getColor(card.root, cor))
            card.root.setOnClickListener { abrirManutencao(item.id) }
            binding.containerPrevisoes.addView(card.root)
        }
    }

    private fun confirmarExclusao() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_moto_title)
            .setMessage(R.string.delete_moto_message)
            .setNegativeButton(R.string.btn_cancelar, null)
            .setPositiveButton(R.string.btn_excluir) { _, _ -> excluir() }
            .show()
    }

    private fun excluir() {
        binding.btnExcluirMoto.isEnabled = false
        binding.btnEditarMoto.isEnabled = false
        lifecycleScope.launch {
            try {
                if (motoDao.excluir(motoId, usuarioId) != 1) throw MotoNaoEncontradaException()
                Toast.makeText(this@MotoDetailActivity, R.string.moto_deleted, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("MotoCare", "Falha ao excluir moto", e)
                Toast.makeText(this@MotoDetailActivity, R.string.database_error, Toast.LENGTH_LONG).show()
                binding.btnExcluirMoto.isEnabled = true
                binding.btnEditarMoto.isEnabled = true
            }
        }
    }
}
