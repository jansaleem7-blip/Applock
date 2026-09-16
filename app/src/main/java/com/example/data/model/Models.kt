package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Personal", // Work, Family, Personal, Shopping, Important, Other
    val priority: String = "Medium",  // Low, Medium, High
    val dueDateMillis: Long = System.currentTimeMillis(),
    val reminderTime: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean = false, // false = expense, true = income
    val category: String = "Food", // Food, Transport, Home, Family, Shopping, Medical, Education, Business, Other
    val dateMillis: Long = System.currentTimeMillis(),
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val colorTag: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String = "1",
    val listName: String = "General", // Grocery, Personal, Home, etc.
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val timeString: String = "09:00",
    val repeat: String = "Never", // Never, Daily, Weekly, Monthly
    val note: String = "",
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
