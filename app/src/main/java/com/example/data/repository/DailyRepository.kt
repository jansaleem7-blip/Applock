package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.model.ExpenseItem
import com.example.data.model.NoteItem
import com.example.data.model.ReminderItem
import com.example.data.model.ShoppingItem
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    PASHTO("ps", "Pashto", "پښتو"),
    DARI("fa", "Dari", "دری")
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val currencySymbol: String = "؋",
    val currencyName: String = "AFN",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val isAppLockEnabled: Boolean = false,
    val hasPinSet: Boolean = false
)

class DailyRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("my_daily_prefs", Context.MODE_PRIVATE)

    // DAOs
    val tasksFlow: Flow<List<TaskItem>> = db.taskDao().getAllTasks()
    val expensesFlow: Flow<List<ExpenseItem>> = db.expenseDao().getAllExpenses()
    val notesFlow: Flow<List<NoteItem>> = db.noteDao().getAllNotes()
    val shoppingFlow: Flow<List<ShoppingItem>> = db.shoppingDao().getAllShoppingItems()
    val remindersFlow: Flow<List<ReminderItem>> = db.reminderDao().getAllReminders()

    // Settings StateFlow
    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<UserSettings> = _settingsState.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeStr = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val theme = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val currSym = prefs.getString("currency_symbol", "؋") ?: "؋"
        val currName = prefs.getString("currency_name", "AFN") ?: "AFN"
        val langCode = prefs.getString("language_code", "en") ?: "en"
        val lang = AppLanguage.entries.find { it.code == langCode } ?: AppLanguage.ENGLISH
        val lockEnabled = prefs.getBoolean("app_lock_enabled", false)
        val pinHash = prefs.getString("pin_hash", "") ?: ""

        return UserSettings(
            themeMode = theme,
            currencySymbol = currSym,
            currencyName = currName,
            language = lang,
            isAppLockEnabled = lockEnabled,
            hasPinSet = pinHash.isNotEmpty()
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _settingsState.value = _settingsState.value.copy(themeMode = mode)
    }

    fun setCurrency(symbol: String, name: String) {
        prefs.edit().putString("currency_symbol", symbol).putString("currency_name", name).apply()
        _settingsState.value = _settingsState.value.copy(currencySymbol = symbol, currencyName = name)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language_code", language.code).apply()
        _settingsState.value = _settingsState.value.copy(language = language)
    }

    fun setAppLock(enabled: Boolean, pin: String? = null) {
        val editor = prefs.edit()
        editor.putBoolean("app_lock_enabled", enabled)
        if (!pin.isNullOrBlank()) {
            val hash = hashPin(pin)
            editor.putString("pin_hash", hash)
        }
        editor.apply()
        _settingsState.value = loadSettings()
    }

    fun verifyPin(enteredPin: String): Boolean {
        val storedHash = prefs.getString("pin_hash", "") ?: ""
        if (storedHash.isEmpty()) return true
        return hashPin(enteredPin) == storedHash
    }

    internal fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("SALT_MY_DAILY_${pin}_2026".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Task CRUD
    suspend fun insertTask(task: TaskItem): Long = db.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskItem) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskItem) = db.taskDao().deleteTask(task)
    suspend fun deleteTaskById(taskId: Long) = db.taskDao().deleteTaskById(taskId)

    // Expense CRUD
    suspend fun insertExpense(expense: ExpenseItem): Long = db.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseItem) = db.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseItem) = db.expenseDao().deleteExpense(expense)
    suspend fun deleteExpenseById(id: Long) = db.expenseDao().deleteExpenseById(id)

    // Note CRUD
    suspend fun insertNote(note: NoteItem): Long = db.noteDao().insertNote(note)
    suspend fun updateNote(note: NoteItem) = db.noteDao().updateNote(note)
    suspend fun deleteNote(note: NoteItem) = db.noteDao().deleteNote(note)
    suspend fun deleteNoteById(id: Long) = db.noteDao().deleteNoteById(id)

    // Shopping CRUD
    suspend fun insertShoppingItem(item: ShoppingItem): Long = db.shoppingDao().insertShoppingItem(item)
    suspend fun updateShoppingItem(item: ShoppingItem) = db.shoppingDao().updateShoppingItem(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = db.shoppingDao().deleteShoppingItem(item)
    suspend fun clearPurchasedShopping() = db.shoppingDao().deletePurchasedItems()

    // Reminder CRUD
    suspend fun insertReminder(reminder: ReminderItem): Long = db.reminderDao().insertReminder(reminder)
    suspend fun updateReminder(reminder: ReminderItem) = db.reminderDao().updateReminder(reminder)
    suspend fun deleteReminder(reminder: ReminderItem) = db.reminderDao().deleteReminder(reminder)

    // Backup to JSON
    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "My Daily")
        root.put("exportTimestamp", System.currentTimeMillis())

        // Tasks
        val tasks = db.taskDao().getAllTasksSnapshot()
        val tasksArr = JSONArray()
        tasks.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("description", it.description)
            obj.put("category", it.category)
            obj.put("priority", it.priority)
            obj.put("dueDateMillis", it.dueDateMillis)
            obj.put("reminderTime", it.reminderTime)
            obj.put("isCompleted", it.isCompleted)
            obj.put("createdAt", it.createdAt)
            tasksArr.put(obj)
        }
        root.put("tasks", tasksArr)

        // Expenses
        val expenses = db.expenseDao().getAllExpensesSnapshot()
        val expArr = JSONArray()
        expenses.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("amount", it.amount)
            obj.put("isIncome", it.isIncome)
            obj.put("category", it.category)
            obj.put("dateMillis", it.dateMillis)
            obj.put("description", it.description)
            obj.put("createdAt", it.createdAt)
            expArr.put(obj)
        }
        root.put("expenses", expArr)

        // Notes
        val notes = db.noteDao().getAllNotesSnapshot()
        val notesArr = JSONArray()
        notes.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("content", it.content)
            obj.put("isPinned", it.isPinned)
            obj.put("colorTag", it.colorTag)
            obj.put("createdAt", it.createdAt)
            obj.put("updatedAt", it.updatedAt)
            notesArr.put(obj)
        }
        root.put("notes", notesArr)

        // Shopping
        val shopping = db.shoppingDao().getAllShoppingSnapshot()
        val shopArr = JSONArray()
        shopping.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("quantity", it.quantity)
            obj.put("listName", it.listName)
            obj.put("isPurchased", it.isPurchased)
            obj.put("createdAt", it.createdAt)
            shopArr.put(obj)
        }
        root.put("shopping", shopArr)

        // Reminders
        val reminders = db.reminderDao().getAllRemindersSnapshot()
        val remArr = JSONArray()
        reminders.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("dateMillis", it.dateMillis)
            obj.put("timeString", it.timeString)
            obj.put("repeat", it.repeat)
            obj.put("note", it.note)
            obj.put("isDone", it.isDone)
            obj.put("createdAt", it.createdAt)
            remArr.put(obj)
        }
        root.put("reminders", remArr)

        return root.toString(2)
    }

    // Restore from JSON
    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("tasks") && !root.has("expenses") && !root.has("notes")) {
                return false
            }

            if (root.has("tasks")) {
                val arr = root.getJSONArray("tasks")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    insertTask(
                        TaskItem(
                            title = o.optString("title", "Untitled"),
                            description = o.optString("description", ""),
                            category = o.optString("category", "Personal"),
                            priority = o.optString("priority", "Medium"),
                            dueDateMillis = o.optLong("dueDateMillis", System.currentTimeMillis()),
                            reminderTime = o.optString("reminderTime", ""),
                            isCompleted = o.optBoolean("isCompleted", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("expenses")) {
                val arr = root.getJSONArray("expenses")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    insertExpense(
                        ExpenseItem(
                            title = o.optString("title", "Expense"),
                            amount = o.optDouble("amount", 0.0),
                            isIncome = o.optBoolean("isIncome", false),
                            category = o.optString("category", "Other"),
                            dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                            description = o.optString("description", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("notes")) {
                val arr = root.getJSONArray("notes")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    insertNote(
                        NoteItem(
                            title = o.optString("title", ""),
                            content = o.optString("content", ""),
                            isPinned = o.optBoolean("isPinned", false),
                            colorTag = o.optInt("colorTag", 0),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("shopping")) {
                val arr = root.getJSONArray("shopping")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    insertShoppingItem(
                        ShoppingItem(
                            name = o.optString("name", ""),
                            quantity = o.optString("quantity", "1"),
                            listName = o.optString("listName", "General"),
                            isPurchased = o.optBoolean("isPurchased", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("reminders")) {
                val arr = root.getJSONArray("reminders")
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    insertReminder(
                        ReminderItem(
                            title = o.optString("title", ""),
                            dateMillis = o.optLong("dateMillis", System.currentTimeMillis()),
                            timeString = o.optString("timeString", "09:00"),
                            repeat = o.optString("repeat", "Never"),
                            note = o.optString("note", ""),
                            isDone = o.optBoolean("isDone", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
