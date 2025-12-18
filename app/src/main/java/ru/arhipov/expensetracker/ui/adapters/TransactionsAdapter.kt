package ru.arhipov.expensetracker.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.arhipov.expensetracker.databinding.ItemTransactionBinding
import ru.arhipov.expensetracker.data.entity.Transaction
import ru.arhipov.expensetracker.util.CurrencyUtil
import ru.arhipov.expensetracker.R
import android.graphics.Color
import java.text.SimpleDateFormat
import java.util.Locale


class TransactionsAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.VH>(DIFF) {

    class VH(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction, onClick: (Transaction) -> Unit) {
            val context = b.root.context
            
            // 1. Amount and Type (color-coded)
            val formattedAmount = CurrencyUtil.formatForDisplay(context, tx.amount)
            b.tvAmount.text = formattedAmount
            
            val isIncome = tx.type == "income"
            val color = if (isIncome) Color.parseColor("#4CAF50") else Color.parseColor("#F44336") // Green for income, Red for expense
            b.tvAmount.setTextColor(color)
            
            // 2. Category
            val categoryKey = tx.category
            val resId = context.resources.getIdentifier("category_$categoryKey", "string", context.packageName)
            val categoryLabel = if (resId != 0) context.getString(resId) else categoryKey
            b.tvCategory.text = categoryLabel
            
            // 3. Date
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            b.tvDate.text = dateFormat.format(tx.date)
            
            // 4. Description
            b.tvDescription.text = tx.description ?: context.getString(R.string.no_description)
            
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
