package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.MainViewModel
import com.example.ui.MoreSubSection
import com.example.ui.NavigationTab
import com.example.ui.components.AppLockScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoneyScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.util.AppStrings

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            MyApplicationTheme(themeMode = settings.themeMode) {
                AppRootContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppRootContent(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val pinError by viewModel.pinError.collectAsState()

    val currentTab by viewModel.currentTab.collectAsState()
    val moreSection by viewModel.moreSection.collectAsState()

    val tasks by viewModel.tasks.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val shoppingItems by viewModel.shoppingItems.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val financialSummary by viewModel.financialSummary.collectAsState()
    val backupMessage by viewModel.backupMessage.collectAsState()

    if (isAppLocked) {
        AppLockScreen(
            language = settings.language,
            pinError = pinError,
            onUnlock = { pin -> viewModel.unlockWithPin(pin) },
            onClearError = { viewModel.clearPinError() }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val tabs = listOf(
                        Triple(NavigationTab.HOME, "home", Pair(Icons.Filled.Home, Icons.Outlined.Home)),
                        Triple(NavigationTab.TASKS, "tasks", Pair(Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)),
                        Triple(NavigationTab.MONEY, "money", Pair(Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney)),
                        Triple(NavigationTab.NOTES, "notes", Pair(Icons.AutoMirrored.Filled.Note, Icons.AutoMirrored.Outlined.Note)),
                        Triple(NavigationTab.MORE, "more", Pair(Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz))
                    )

                    tabs.forEach { (tab, key, iconPair) ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) iconPair.first else iconPair.second,
                                    contentDescription = AppStrings.get(key, settings.language)
                                )
                            },
                            label = { Text(AppStrings.get(key, settings.language)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_item_$key")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { tab ->
                when (tab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            tasks = tasks,
                            expenses = expenses,
                            notes = notes,
                            reminders = reminders,
                            settings = settings,
                            financialSummary = financialSummary,
                            onNavigateTab = { target -> viewModel.selectTab(target) },
                            onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                            onAddTaskClick = { viewModel.selectTab(NavigationTab.TASKS) },
                            onAddExpenseClick = { viewModel.selectTab(NavigationTab.MONEY) },
                            onAddNoteClick = { viewModel.selectTab(NavigationTab.NOTES) }
                        )
                    }
                    NavigationTab.TASKS -> {
                        TasksScreen(
                            tasks = tasks,
                            language = settings.language,
                            onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                            onDeleteTask = { task -> viewModel.deleteTask(task) },
                            onSaveTask = { title, desc, cat, prio, due, rem ->
                                viewModel.addTask(title, desc, cat, prio, due, rem)
                            },
                            onUpdateTask = { task -> viewModel.updateTask(task) }
                        )
                    }
                    NavigationTab.MONEY -> {
                        MoneyScreen(
                            expenses = expenses,
                            settings = settings,
                            financialSummary = financialSummary,
                            onAddTransaction = { title, amt, inc, cat, desc ->
                                viewModel.addExpense(title, amt, inc, cat, desc)
                            },
                            onDeleteExpense = { exp -> viewModel.deleteExpense(exp) }
                        )
                    }
                    NavigationTab.NOTES -> {
                        NotesScreen(
                            notes = notes,
                            language = settings.language,
                            onAddNote = { title, content, isPinned ->
                                viewModel.addNote(title, content, isPinned)
                            },
                            onUpdateNote = { note -> viewModel.updateNote(note) },
                            onTogglePin = { note -> viewModel.toggleNotePin(note) },
                            onDeleteNote = { note -> viewModel.deleteNote(note) }
                        )
                    }
                    NavigationTab.MORE -> {
                        MoreScreen(
                            currentSection = moreSection,
                            shoppingItems = shoppingItems,
                            reminders = reminders,
                            settings = settings,
                            backupMessage = backupMessage,
                            onSelectSubSection = { section -> viewModel.selectMoreSubSection(section) },
                            onAddShoppingItem = { name, qty, list ->
                                viewModel.addShoppingItem(name, qty, list)
                            },
                            onToggleShoppingPurchased = { item -> viewModel.toggleShoppingPurchased(item) },
                            onDeleteShoppingItem = { item -> viewModel.deleteShoppingItem(item) },
                            onClearPurchasedShopping = { viewModel.clearPurchasedShopping() },
                            onAddReminder = { title, date, time, repeat, note ->
                                viewModel.addReminder(title, date, time, repeat, note)
                            },
                            onToggleReminderDone = { rem -> viewModel.toggleReminderDone(rem) },
                            onDeleteReminder = { rem -> viewModel.deleteReminder(rem) },
                            onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                            onSetCurrency = { sym, name -> viewModel.setCurrency(sym, name) },
                            onSetLanguage = { lang -> viewModel.setLanguage(lang) },
                            onSetAppLock = { enabled, pin -> viewModel.setAppLock(enabled, pin) },
                            onLockAppNow = { viewModel.lockAppNow() },
                            onExportBackup = { callback -> viewModel.exportBackup(callback) },
                            onImportBackup = { json, callback -> viewModel.importBackup(json, callback) },
                            onClearBackupMessage = { viewModel.clearBackupMessage() }
                        )
                    }
                }
            }
        }
    }
}
