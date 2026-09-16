package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseItem
import com.example.data.repository.AppLanguage
import com.example.data.repository.UserSettings
import com.example.ui.FinancialSummary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.RoseAccent
import com.example.ui.util.AppStrings
import com.example.ui.util.DateUtils

@Composable
fun MoneyScreen(
    expenses: List<ExpenseItem>,
    settings: UserSettings,
    financialSummary: FinancialSummary,
    onAddTransaction: (title: String, amount: Double, isIncome: Boolean, category: String, desc: String) -> Unit,
    onDeleteExpense: (ExpenseItem) -> Unit
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Expense, 2 = Income
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(expenses, selectedFilter) {
        when (selectedFilter) {
            1 -> expenses.filter { !it.isIncome }
            2 -> expenses.filter { it.isIncome }
            else -> expenses
        }
    }

    // Category spending breakdown for visual summary
    val categoryBreakdown = remember(expenses) {
        val expenseOnly = expenses.filter { !it.isIncome }
        val totalExp = expenseOnly.sumOf { it.amount }.coerceAtLeast(1.0)
        expenseOnly
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)
            .map { (cat, amt) -> Triple(cat, amt, (amt / totalExp).toFloat()) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("money_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Balance Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("balance_hero_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppStrings.get("balance", settings.language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${settings.currencySymbol} %.2f".format(financialSummary.remainingBalance),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Income and Expense Dual Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Total Income
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSecondary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Income",
                                        tint = EmeraldSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = AppStrings.get("income", settings.language),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "${settings.currencySymbol} %.0f".format(financialSummary.totalIncome),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldSecondary
                                        )
                                    )
                                }
                            }

                            // Total Expenses
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(RoseAccent.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Expense",
                                        tint = RoseAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = AppStrings.get("expense", settings.language),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "${settings.currencySymbol} %.0f".format(financialSummary.totalExpenses),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = RoseAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spending Period Breakdown Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PeriodSpendingChip(
                        title = AppStrings.get("today_spending", settings.language),
                        amount = financialSummary.todaySpending,
                        currency = settings.currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                    PeriodSpendingChip(
                        title = AppStrings.get("week_spending", settings.language),
                        amount = financialSummary.weekSpending,
                        currency = settings.currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                    PeriodSpendingChip(
                        title = AppStrings.get("month_spending", settings.language),
                        amount = financialSummary.monthSpending,
                        currency = settings.currencySymbol,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Visual Category Spending Summary
            if (categoryBreakdown.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Top Expense Categories",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            categoryBreakdown.forEach { (cat, amt, progress) ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = "${settings.currencySymbol} %.0f (%.0f%%)".format(amt, progress * 100),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedFilter,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedFilter == 0,
                        onClick = { selectedFilter = 0 },
                        text = { Text(AppStrings.get("view_all", settings.language)) }
                    )
                    Tab(
                        selected = selectedFilter == 1,
                        onClick = { selectedFilter = 1 },
                        text = { Text(AppStrings.get("expense", settings.language)) }
                    )
                    Tab(
                        selected = selectedFilter == 2,
                        onClick = { selectedFilter = 2 },
                        text = { Text(AppStrings.get("income", settings.language)) }
                    )
                }
            }

            // Transactions List
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions recorded yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    TransactionItemCard(
                        item = item,
                        currency = settings.currencySymbol,
                        onDelete = { onDeleteExpense(item) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Add Transaction FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_money_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        var isIncome by remember { mutableStateOf(false) }
        var title by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf(if (isIncome) "Business" else "Food") }
        var desc by remember { mutableStateOf("") }

        val categories = if (isIncome) {
            listOf("Salary", "Business", "Gift", "Investment", "Other")
        } else {
            listOf("Food", "Transport", "Home", "Family", "Shopping", "Medical", "Education", "Business", "Other")
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(AppStrings.get("add_transaction", settings.language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isIncome,
                            onClick = {
                                isIncome = false
                                category = "Food"
                            },
                            label = { Text(AppStrings.get("expense", settings.language)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isIncome,
                            onClick = {
                                isIncome = true
                                category = "Salary"
                            },
                            label = { Text(AppStrings.get("income", settings.language)) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (e.g. Groceries, Fuel)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_title_input")
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("${AppStrings.get("amount", settings.language)} (${settings.currencySymbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transaction_amount_input")
                    )

                    Text(text = AppStrings.get("category", settings.language), style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(AppStrings.get("description", settings.language)) },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0) {
                            onAddTransaction(title, amount, isIncome, category, desc)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_transaction_btn")
                ) {
                    Text(AppStrings.get("save", settings.language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(AppStrings.get("cancel", settings.language))
                }
            }
        )
    }
}

@Composable
private fun PeriodSpendingChip(
    title: String,
    amount: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$currency %.0f".format(amount),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun TransactionItemCard(
    item: ExpenseItem,
    currency: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isIncome) EmeraldSecondary.copy(alpha = 0.15f)
                        else RoseAccent.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = if (item.isIncome) "Income" else "Expense",
                    tint = if (item.isIncome) EmeraldSecondary else RoseAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                    Text(
                        text = DateUtils.formatShortDate(item.dateMillis),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Text(
                text = "${if (item.isIncome) "+" else "-"} $currency %.2f".format(item.amount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (item.isIncome) EmeraldSecondary else RoseAccent
                )
            )

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
