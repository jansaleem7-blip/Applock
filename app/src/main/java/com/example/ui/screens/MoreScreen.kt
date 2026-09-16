package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ReminderItem
import com.example.data.model.ShoppingItem
import com.example.data.repository.AppLanguage
import com.example.data.repository.AppThemeMode
import com.example.data.repository.UserSettings
import com.example.ui.MoreSubSection
import com.example.ui.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    currentSection: MoreSubSection,
    shoppingItems: List<ShoppingItem>,
    reminders: List<ReminderItem>,
    settings: UserSettings,
    backupMessage: String?,
    onSelectSubSection: (MoreSubSection) -> Unit,
    onAddShoppingItem: (name: String, quantity: String, listName: String) -> Unit,
    onToggleShoppingPurchased: (ShoppingItem) -> Unit,
    onDeleteShoppingItem: (ShoppingItem) -> Unit,
    onClearPurchasedShopping: () -> Unit,
    onAddReminder: (title: String, dateMillis: Long, timeString: String, repeat: String, note: String) -> Unit,
    onToggleReminderDone: (ReminderItem) -> Unit,
    onDeleteReminder: (ReminderItem) -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetCurrency: (symbol: String, name: String) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetAppLock: (Boolean, String?) -> Unit,
    onLockAppNow: () -> Unit,
    onExportBackup: ((String) -> Unit) -> Unit,
    onImportBackup: (String, (Boolean) -> Unit) -> Unit,
    onClearBackupMessage: () -> Unit
) {
    when (currentSection) {
        MoreSubSection.SHOPPING -> {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(AppStrings.get("shopping", settings.language)) },
                    navigationIcon = {
                        IconButton(onClick = { onSelectSubSection(MoreSubSection.MENU) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                ShoppingScreen(
                    items = shoppingItems,
                    language = settings.language,
                    onAddItem = onAddShoppingItem,
                    onTogglePurchased = onToggleShoppingPurchased,
                    onDeleteItem = onDeleteShoppingItem,
                    onClearPurchased = onClearPurchasedShopping
                )
            }
        }
        MoreSubSection.REMINDERS -> {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(AppStrings.get("reminders", settings.language)) },
                    navigationIcon = {
                        IconButton(onClick = { onSelectSubSection(MoreSubSection.MENU) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                RemindersScreen(
                    reminders = reminders,
                    language = settings.language,
                    onAddReminder = onAddReminder,
                    onToggleDone = onToggleReminderDone,
                    onDeleteReminder = onDeleteReminder
                )
            }
        }
        MoreSubSection.SETTINGS -> {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(AppStrings.get("settings", settings.language)) },
                    navigationIcon = {
                        IconButton(onClick = { onSelectSubSection(MoreSubSection.MENU) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
                SettingsScreen(
                    settings = settings,
                    backupMessage = backupMessage,
                    onSetThemeMode = onSetThemeMode,
                    onSetCurrency = onSetCurrency,
                    onSetLanguage = onSetLanguage,
                    onSetAppLock = onSetAppLock,
                    onExportBackup = onExportBackup,
                    onImportBackup = onImportBackup,
                    onClearBackupMessage = onClearBackupMessage
                )
            }
        }
        MoreSubSection.MENU -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("more_menu_screen"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = AppStrings.get("more", settings.language),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Shopping List Hub Card
                MoreHubCard(
                    title = AppStrings.get("shopping", settings.language),
                    subtitle = "${shoppingItems.count { !it.isPurchased }} items to buy",
                    icon = Icons.Default.ShoppingBag,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { onSelectSubSection(MoreSubSection.SHOPPING) }
                )

                // Reminders Hub Card
                MoreHubCard(
                    title = AppStrings.get("reminders", settings.language),
                    subtitle = "${reminders.count { !it.isDone }} active reminders",
                    icon = Icons.Default.Notifications,
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = { onSelectSubSection(MoreSubSection.REMINDERS) }
                )

                // Settings Hub Card
                MoreHubCard(
                    title = AppStrings.get("settings", settings.language),
                    subtitle = "App lock, theme, currency, language, backup",
                    icon = Icons.Default.Settings,
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = { onSelectSubSection(MoreSubSection.SETTINGS) }
                )

                // Quick Lock App Action if enabled
                if (settings.isAppLockEnabled && settings.hasPinSet) {
                    MoreHubCard(
                        title = "Lock App Now",
                        subtitle = "Immediately lock with PIN",
                        icon = Icons.Default.Lock,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = onLockAppNow
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("more_hub_${title.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
