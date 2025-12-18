package ru.arhipov.expensetracker.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.arhipov.expensetracker.data.dao.TransactionDao
import ru.arhipov.expensetracker.data.entity.ExpenseByCategory
import ru.arhipov.expensetracker.data.entity.Transaction
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransaction(id: Long): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    suspend fun getBalance(): Double = withContext(Dispatchers.IO) {
        val income = transactionDao.getTotalIncome() ?: 0.0
        val expense = transactionDao.getTotalExpense() ?: 0.0
        income - expense
    }

    suspend fun getExpensesByCategory(): List<ExpenseByCategory> = withContext(Dispatchers.IO) {
        transactionDao.getExpensesByCategory()
    }

    fun getTransactionsByDateRange(start: Date, end: Date): LiveData<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(start, end)
}
