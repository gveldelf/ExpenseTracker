package ru.arhipov.expensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ru.arhipov.expensetracker.data.database.AppDatabase
import ru.arhipov.expensetracker.data.repository.TransactionRepository
import ru.arhipov.expensetracker.databinding.ActivityTransactionsBinding
import ru.arhipov.expensetracker.ui.adapters.TransactionsAdapter
import ru.arhipov.expensetracker.ui.viewmodel.TransactionViewModel
import android.content.Intent

class TransactionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var adapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val database = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(database.transactionDao())
        viewModel = TransactionViewModel(repository)

        adapter = TransactionsAdapter { transaction ->
            val intent = Intent(this, EditTransactionActivity::class.java).apply {
                putExtra("transactionId", transaction.id)
            }
            startActivity(intent)
        }

        binding.recyclerViewTransactions.adapter = adapter
        binding.recyclerViewTransactions.layoutManager = LinearLayoutManager(this)

        viewModel.allTransactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }
    }
}