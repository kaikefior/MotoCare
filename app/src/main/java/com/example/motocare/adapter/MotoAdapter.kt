package com.example.motocare.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.motocare.R
import com.example.motocare.databinding.ItemMotoBinding
import com.example.motocare.model.Moto

class MotoAdapter(private val onClick: (Moto) -> Unit) :
    ListAdapter<Moto, MotoAdapter.MotoViewHolder>(Diff) {

    class MotoViewHolder(val binding: ItemMotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MotoViewHolder(ItemMotoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MotoViewHolder, position: Int) {
        val moto = getItem(position)
        with(holder.binding) {
            tvMotoNome.text = root.context.getString(R.string.moto_name, moto.marca, moto.modelo)
            tvMotoAno.text = root.context.getString(R.string.moto_year, moto.ano)
            tvMotoKm.text = root.context.getString(R.string.moto_km, moto.quilometragemAtual)
            tvMotoPlaca.text = moto.placa.ifBlank { root.context.getString(R.string.no_plate) }
            root.setOnClickListener { onClick(moto) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Moto>() {
        override fun areItemsTheSame(oldItem: Moto, newItem: Moto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Moto, newItem: Moto) = oldItem == newItem
    }
}
