package ru.arhipov.expensetracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String,
    val date: Date,
    val description: String? = null
)