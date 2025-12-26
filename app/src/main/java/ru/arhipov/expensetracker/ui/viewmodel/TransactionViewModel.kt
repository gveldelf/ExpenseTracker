package ru.arhipov.expensetracker.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.arhipov.expensetracker.data.entity.Transaction
import ru.arhipov.expensetracker.data.entity.ExpenseByCategory
import ru.arhipov.expensetracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions

    fun insertTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun updateTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    suspend fun getBalance(): Double = repository.getBalance()

    suspend fun getExpensesByCategory(): List<ExpenseByCategory> = repository.getExpensesByCategory()

    suspend fun getTransaction(uid: String): Transaction? {
        return repository.getTransaction(uid)
    }

    fun getTransactionsByDateRange(start: Date, end: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsByDateRange(start, end)
    }
}