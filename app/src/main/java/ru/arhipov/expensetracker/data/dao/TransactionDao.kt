package ru.arhipov.expensetracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.arhipov.expensetracker.data.entity.ExpenseByCategory
import ru.arhipov.expensetracker.data.entity.Transaction
import java.util.Date

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE uid = :uid")
    suspend fun getTransactionByUid(uid: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'income'")
    suspend fun getTotalIncome(): Double

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type = 'expense'")
    suspend fun getTotalExpense(): Double

    // Новый метод - получение баланса одним запросом
    @Query("""
        SELECT IFNULL(
            SUM(CASE WHEN type = 'income' THEN amount ELSE -amount END), 
            0
        ) 
        FROM transactions
    """)
    suspend fun getBalance(): Double

    @Query("""
        SELECT 
            category,
            SUM(amount) AS total
        FROM transactions
        WHERE type = 'expense'
        GROUP BY category
    """)
    suspend fun getExpensesByCategory(): List<ExpenseByCategory>

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        startDate: Date,
        endDate: Date
    ): LiveData<List<Transaction>>
}