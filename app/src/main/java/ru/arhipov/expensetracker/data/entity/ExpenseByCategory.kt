package ru.arhipov.expensetracker.data.entity

import androidx.room.ColumnInfo

data class ExpenseByCategory(

    @ColumnInfo(name = "category")
    val categoryKey: String,

    @ColumnInfo(name = "total")
    val total: Double
)
