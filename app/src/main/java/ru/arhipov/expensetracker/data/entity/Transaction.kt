package ru.arhipov.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val date: Date,
    val description: String? = null
)
// продумать значения Double Id запись в базу с ограничением.