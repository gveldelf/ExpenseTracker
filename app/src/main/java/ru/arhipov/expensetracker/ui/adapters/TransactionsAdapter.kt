package ru.arhipov.expensetracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.arhipov.expensetracker.databinding.ItemTransactionBinding
import ru.arhipov.expensetracker.data.entity.Transaction
import ru.arhipov.expensetracker.util.CurrencyUtil


class TransactionsAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.VH>(DIFF) {

    class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction, onClick: (Transaction) -> Unit) {
            b.tvDescription.text = tx.description ?: ""
            b.tvAmount.text = CurrencyUtil.formatForDisplay(b.root.context, tx.amount)
            b.root.setOnClickListener { onClick(tx) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(old: Transaction, newItem: Transaction) = old.id == newItem.id
            override fun areContentsTheSame(old: Transaction, newItem: Transaction) = old == newItem
        }
    }
}
