package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ExpenseItem
import com.example.data.model.NoteItem
import com.example.data.model.ReminderItem
import com.example.data.model.ShoppingItem
import com.example.data.model.TaskItem
import com.example.data.repository.AppLanguage
import com.example.data.repository.AppThemeMode
import com.example.data.repository.DailyRepository
import com.example.data.repository.UserSettings
import com.example.ui.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    HOME, TASKS, MONEY, NOTES, MORE
}

enum class MoreSubSection {
    MENU, SHOPPING, REMINDERS, SETTINGS
}

data class FinancialSummary(
    val todaySpending: Double = 0.0,
    val weekSpending: Double = 0.0,
    val monthSpending: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val remainingBalance: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DailyRepository(application.applicationContext)

    // Data streams
    val tasks: StateFlow<List<TaskItem>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseItem>> = repository.expensesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteItem>> = repository.notesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.shoppingFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderItem>> = repository.remindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettings> = repository.settingsState

    // Financial Summary
    val financialSummary: StateFlow<FinancialSummary> = expenses.combine(settings) { list, _ ->
        var today = 0.0
        var week = 0.0
        var month = 0.0
        var inc = 0.0
        var exp = 0.0

        for (item in list) {
            if (item.isIncome) {
                inc += item.amount
            } else {
                exp += item.amount
                if (DateUtils.isToday(item.dateMillis)) {
                    today += item.amount
                }
                if (DateUtils.isThisWeek(item.dateMillis)) {
                    week += item.amount
                }
                if (DateUtils.isThisMonth(item.dateMillis)) {
                    month += item.amount
                }
            }
        }
        FinancialSummary(
            todaySpending = today,
            weekSpending = week,
            monthSpending = month,
            totalIncome = inc,
            totalExpenses = exp,
            remainingBalance = inc - exp
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // UI state
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _moreSection = MutableStateFlow(MoreSubSection.MENU)
    val moreSection: StateFlow<MoreSubSection> = _moreSection.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // App Lock PIN Verification state
    private val _isAppLocked = MutableStateFlow(settings.value.isAppLockEnabled && settings.value.hasPinSet)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    // Backup state
    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectMoreSubSection(subSection: MoreSubSection) {
        _moreSection.value = subSection
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun unlockWithPin(pin: String): Boolean {
        val ok = repository.verifyPin(pin)
        if (ok) {
            _isAppLocked.value = false
            _pinError.value = null
        } else {
            _pinError.value = "wrong_pin"
        }
        return ok
    }

    fun clearPinError() {
        _pinError.value = null
    }

    fun lockAppNow() {
        if (settings.value.isAppLockEnabled) {
            _isAppLocked.value = true
        }
    }

    // Settings actions
    fun setThemeMode(mode: AppThemeMode) = repository.setThemeMode(mode)
    fun setCurrency(symbol: String, name: String) = repository.setCurrency(symbol, name)
    fun setLanguage(lang: AppLanguage) = repository.setLanguage(lang)
    fun setAppLock(enabled: Boolean, pin: String? = null) {
        repository.setAppLock(enabled, pin)
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    // Task operations
    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDateMillis: Long,
        reminderTime: String
    ) = viewModelScope.launch {
        repository.insertTask(
            TaskItem(
                title = title.trim(),
                description = description.trim(),
                category = category,
                priority = priority,
                dueDateMillis = dueDateMillis,
                reminderTime = reminderTime
            )
        )
    }

    fun updateTask(task: TaskItem) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun toggleTaskCompletion(task: TaskItem) = viewModelScope.launch {
        repository.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    fun deleteTask(task: TaskItem) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    // Expense operations
    fun addExpense(
        title: String,
        amount: Double,
        isIncome: Boolean,
        category: String,
        description: String,
        dateMillis: Long = System.currentTimeMillis()
    ) = viewModelScope.launch {
        repository.insertExpense(
            ExpenseItem(
                title = title.trim(),
                amount = amount,
                isIncome = isIncome,
                category = category,
                description = description.trim(),
                dateMillis = dateMillis
            )
        )
    }

    fun deleteExpense(expense: ExpenseItem) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // Note operations
    fun addNote(title: String, content: String, isPinned: Boolean = false, colorTag: Int = 0) =
        viewModelScope.launch {
            repository.insertNote(
                NoteItem(
                    title = title.trim(),
                    content = content.trim(),
                    isPinned = isPinned,
                    colorTag = colorTag
                )
            )
        }

    fun updateNote(note: NoteItem) = viewModelScope.launch {
        repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    fun toggleNotePin(note: NoteItem) = viewModelScope.launch {
        repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
    }

    fun deleteNote(note: NoteItem) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    // Shopping operations
    fun addShoppingItem(name: String, quantity: String, listName: String = "General") =
        viewModelScope.launch {
            repository.insertShoppingItem(
                ShoppingItem(
                    name = name.trim(),
                    quantity = quantity.trim().ifEmpty { "1" },
                    listName = listName
                )
            )
        }

    fun toggleShoppingPurchased(item: ShoppingItem) = viewModelScope.launch {
        repository.updateShoppingItem(item.copy(isPurchased = !item.isPurchased))
    }

    fun deleteShoppingItem(item: ShoppingItem) = viewModelScope.launch {
        repository.deleteShoppingItem(item)
    }

    fun clearPurchasedShopping() = viewModelScope.launch {
        repository.clearPurchasedShopping()
    }

    // Reminder operations
    fun addReminder(title: String, dateMillis: Long, timeString: String, repeat: String, note: String) =
        viewModelScope.launch {
            repository.insertReminder(
                ReminderItem(
                    title = title.trim(),
                    dateMillis = dateMillis,
                    timeString = timeString,
                    repeat = repeat,
                    note = note.trim()
                )
            )
        }

    fun toggleReminderDone(reminder: ReminderItem) = viewModelScope.launch {
        repository.updateReminder(reminder.copy(isDone = !reminder.isDone))
    }

    fun deleteReminder(reminder: ReminderItem) = viewModelScope.launch {
        repository.deleteReminder(reminder)
    }

    // Backup & Restore
    fun exportBackup(onResult: (String) -> Unit) = viewModelScope.launch {
        val json = repository.exportBackupJson()
        _backupMessage.value = "backup_exported"
        onResult(json)
    }

    fun importBackup(json: String, onComplete: (Boolean) -> Unit) = viewModelScope.launch {
        val success = repository.importBackupJson(json)
        _backupMessage.value = if (success) "backup_imported" else "backup_error"
        onComplete(success)
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }
}
