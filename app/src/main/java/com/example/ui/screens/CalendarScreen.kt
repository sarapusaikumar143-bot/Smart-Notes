package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sparkles
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.DailyInsight
import com.example.domain.models.Transaction
import com.example.domain.models.TransactionType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    currentMonth: Int,
    currentYear: Int,
    selectedDay: Int,
    transactions: List<Transaction>,
    dailyInsight: DailyInsight,
    onDaySelected: (Int) -> Unit,
    onChangeMonth: (Int) -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenFiles: () -> Unit = {},
    onQuickAdd: (Int) -> Unit = {},
    onDeleteTransaction: (Transaction) -> Unit = {}
) {
    val monthName = DateFormatSymbols().months[currentMonth]

    // Calculate month income & expense
    val monthTxs = remember(transactions, currentMonth, currentYear) {
        transactions.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            c.get(Calendar.MONTH) == currentMonth && c.get(Calendar.YEAR) == currentYear
        }
    }
    val totalIncome = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    // Calculate day-by-day aggregates for DayCell: +Income and -Expense per day
    val dayAggregates = remember(monthTxs) {
        val map = mutableMapOf<Int, Pair<Double, Double>>() // Day -> Pair(income, expense)
        monthTxs.forEach { tx ->
            val c = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
            val d = c.get(Calendar.DAY_OF_MONTH)
            val current = map[d] ?: Pair(0.0, 0.0)
            if (tx.type == TransactionType.INCOME) {
                map[d] = Pair(current.first + tx.amount, current.second)
            } else if (tx.type == TransactionType.EXPENSE) {
                map[d] = Pair(current.first, current.second + tx.amount)
            }
        }
        map
    }

    // Selected day transactions
    val selectedDayTxs = remember(monthTxs, selectedDay) {
        monthTxs.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            c.get(Calendar.DAY_OF_MONTH) == selectedDay
        }
    }

    var dragOffset by remember { mutableFloatStateOf(0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .pointerInput(currentMonth, currentYear) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 60) {
                            onChangeMonth(-1) // Swipe Right -> Previous Month
                        } else if (dragOffset < -60) {
                            onChangeMonth(1)  // Swipe Left -> Next Month
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
            .testTag("calendar_screen_clean"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. TopBar (Month Navigation + Profile/Settings Icon)
        item {
            TopBar(
                monthName = monthName,
                year = currentYear,
                onPrevMonth = { onChangeMonth(-1) },
                onNextMonth = { onChangeMonth(1) },
                onOpenSettings = onOpenSettings,
                onOpenFiles = onOpenFiles
            )
        }

        // 2. Balance Card (Total Balance, Income/Expense, Balance bar at top)
        item {
            BalanceCard(
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpense = totalExpense
            )
        }

        // 3. Calendar Grid (7 columns, Income = Green dot, Expense = Red dot, Long press -> quick add)
        item {
            CalendarGrid(
                currentMonth = currentMonth,
                currentYear = currentYear,
                selectedDay = selectedDay,
                dayAggregates = dayAggregates,
                onDaySelected = onDaySelected,
                onDayLongClick = onQuickAdd
            )
        }

        // 4. Daily Insight AI Card
        item {
            DailyInsightCard(insight = dailyInsight)
        }

        // 5. Selected Day Transactions Ledger
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$monthName $selectedDay • Transactions (${selectedDayTxs.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(
                    onClick = { onQuickAdd(selectedDay) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry", fontSize = 11.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedDayTxs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions on $monthName $selectedDay.\nLong-press or tap 'Add Entry' above to add one.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(selectedDayTxs, key = { it.id }) { tx ->
                TransactionRowItem(
                    transaction = tx,
                    onDelete = { onDeleteTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

// 1. TopBar Component with Profile/Settings Icon
@Composable
fun TopBar(
    monthName: String,
    year: Int,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFiles: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$monthName $year",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Swipe ↔ to change month • Long-press day to add",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(onClick = onNextMonth) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
            IconButton(
                onClick = onOpenFiles,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("top_bar_files_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Files Explorer",
                    tint = DarkNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("top_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile & Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// 2. Balance Card Component with Progress Balance Bar at Top
@Composable
fun BalanceCard(
    totalBalance: Double,
    totalIncome: Double,
    totalExpense: Double
) {
    val totalCashflow = totalIncome + totalExpense
    val incomeRatio = if (totalCashflow > 0) (totalIncome / totalCashflow).toFloat() else 0.5f

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Month Balance",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${totalBalance.toInt()}",
                        color = if (totalBalance >= 0) MoneyGreen else DangerRed,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Linear Gradient Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(listOf(ElectricBlue, MoneyGreen))
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (totalBalance >= 0) "Surplus" else "Deficit",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Balance Bar at Top (Income vs Expense Visual Progress)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Income ${ (incomeRatio * 100).toInt() }%",
                        fontSize = 10.sp,
                        color = MoneyGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Expense ${ ((1f - incomeRatio) * 100).toInt() }%",
                        fontSize = 10.sp,
                        color = DangerRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { incomeRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MoneyGreen,
                    trackColor = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inflow: +₹${totalIncome.toInt()}",
                        color = MoneyGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = DangerRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Outflow: -₹${totalExpense.toInt()}",
                        color = DangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// 3. Calendar Grid Component
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarGrid(
    currentMonth: Int,
    currentYear: Int,
    selectedDay: Int,
    dayAggregates: Map<Int, Pair<Double, Double>>,
    onDaySelected: (Int) -> Unit,
    onDayLongClick: (Int) -> Unit
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week days header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
                Text(
                    text = d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(daysInMonth) { idx ->
                val day = idx + 1
                val (income, expense) = dayAggregates[day] ?: Pair(0.0, 0.0)
                DayCell(
                    day = day,
                    isSelected = day == selectedDay,
                    income = income,
                    expense = expense,
                    onClick = { onDaySelected(day) },
                    onLongClick = { onDayLongClick(day) }
                )
            }
        }
    }
}

// 4. Day Cell Component with Green Dot (Income) & Red Dot (Expense)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    income: Double,
    expense: Double,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ElectricBlue else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 2.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$day",
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )

            // Dots Indicator: Green dot for income, Red dot for expense
            Row(
                modifier = Modifier
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (income > 0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else MoneyGreen)
                    )
                }
                if (expense > 0) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else DangerRed)
                    )
                }
            }

            // Amounts
            if (income > 0) {
                Text(
                    text = "+${income.toInt()}",
                    color = if (isSelected) Color.White else MoneyGreen,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            } else if (expense > 0) {
                Text(
                    text = "-${expense.toInt()}",
                    color = if (isSelected) Color.White else DangerRed,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

// 5. Daily Insight Card Component
@Composable
fun DailyInsightCard(insight: DailyInsight) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = ElectricBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Sparkles, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = insight.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(text = insight.badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (insight.isPositive) MoneyGreen else DangerRed)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = insight.description, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)
            }
        }
    }
}

// 6. Transaction Row Item for Selected Day
@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (transaction.type == TransactionType.INCOME) MoneyGreen else DangerRed)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = transaction.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = transaction.category.displayName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}₹${transaction.amount.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (transaction.type == TransactionType.INCOME) MoneyGreen else DangerRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
