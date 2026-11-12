package com.example.motocare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.motocare.MotoDetailActivity
import com.example.motocare.MotoFormActivity
import com.example.motocare.R
import com.example.motocare.adapter.MotoAdapter
import com.example.motocare.database.MotoCareDatabase
import com.example.motocare.databinding.FragmentMotoListBinding
import com.example.motocare.session.SessionManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MotoListFragment : Fragment(R.layout.fragment_moto_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val usuarioId = SessionManager(requireContext()).exigirUsuario(requireActivity()) ?: return
        val binding = FragmentMotoListBinding.bind(view)
        val motoDao = MotoCareDatabase.getInstance(requireContext()).motoDao()
        val adapter = MotoAdapter { moto ->
            startActivity(Intent(requireContext(), MotoDetailActivity::class.java)
                .putExtra(MotoFormActivity.EXTRA_MOTO_ID, moto.id))
        }
        binding.rvMotos.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMotos.adapter = adapter
        binding.fabNovaMoto.setOnClickListener {
            startActivity(Intent(requireContext(), MotoFormActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observa o banco somente enquanto a tela está visível.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.progress.isVisible = true
                motoDao.observarTodas(usuarioId).catch {
                    binding.progress.isVisible = false
                    binding.tvEmpty.isVisible = true
                    binding.tvEmpty.setText(R.string.motos_load_error)
                    binding.rvMotos.isVisible = false
                }.collect { motos ->
                    binding.progress.isVisible = false
                    binding.tvEmpty.isVisible = motos.isEmpty()
                    binding.tvEmpty.setText(R.string.empty_motos)
                    binding.rvMotos.isVisible = motos.isNotEmpty()
                    adapter.submitList(motos)
                }
            }
        }
    }
}
