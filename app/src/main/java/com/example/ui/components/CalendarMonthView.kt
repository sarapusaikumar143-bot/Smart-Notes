package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionEntity
import com.example.model.TransactionType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarMonthView(
    month: Int, // 0-11
    year: Int,
    selectedDateMillis: Long,
    transactions: List<TransactionEntity>,
    onDateSelected: (Long) -> Unit,
    onMonthChanged: (Int) -> Unit,
    onQuickAddRequested: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthName = DateFormatSymbols().months[month]

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val isSameMonthYear = selCal.get(Calendar.MONTH) == month && selCal.get(Calendar.YEAR) == year
    val selectedDay = if (isSameMonthYear) selCal.get(Calendar.DAY_OF_MONTH) else -1

    val todayCal = Calendar.getInstance()
    val isTodayMonthYear = todayCal.get(Calendar.MONTH) == month && todayCal.get(Calendar.YEAR) == year
    val todayDay = if (isTodayMonthYear) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    // Group transactions by day of this month
    val dayTransactions = remember(transactions, month, year) {
        val map = mutableMapOf<Int, Pair<Boolean, Boolean>>() // Day -> Pair(hasIncome, hasExpense)
        for (tx in transactions) {
            val c = Calendar.getInstance()
            c.timeInMillis = tx.dateMillis
            if (c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year) {
                val day = c.get(Calendar.DAY_OF_MONTH)
                val current = map[day] ?: Pair(false, false)
                val hasIncome = current.first || (tx.type == TransactionType.INCOME)
                val hasExpense = current.second || (tx.type == TransactionType.EXPENSE)
                map[day] = Pair(hasIncome, hasExpense)
            }
        }
        map
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_month_view")
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (totalDrag > 80f) {
                            onMonthChanged(-1) // swipe right -> prev month
                        } else if (totalDrag < -80f) {
                            onMonthChanged(1) // swipe left -> next month
                        }
                        totalDrag = 0f
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChanged(-1) },
                    modifier = Modifier.testTag("prev_month_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$monthName $year",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Swipe left/right to change • Long-press day to add",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onMonthChanged(1) },
                    modifier = Modifier.testTag("next_month_button")
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days of week header
            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                for (day in daysOfWeek) {
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (6 rows max)
            val totalCells = (firstDayOfWeek - 1) + daysInMonth
            val totalRows = (totalCells + 6) / 7

            for (row in 0 until totalRows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - (firstDayOfWeek - 1) + 1

                        if (dayNumber in 1..daysInMonth) {
                            val isSelected = (dayNumber == selectedDay)
                            val isToday = (dayNumber == todayDay)
                            val status = dayTransactions[dayNumber]
                            val hasIncome = status?.first == true
                            val hasExpense = status?.second == true

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> ElectricBlue
                                            isToday -> ElectricBlue.copy(alpha = 0.12f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            val c = Calendar.getInstance()
                                            c.set(year, month, dayNumber, 12, 0, 0)
                                            onDateSelected(c.timeInMillis)
                                        },
                                        onLongClick = {
                                            val c = Calendar.getInstance()
                                            c.set(year, month, dayNumber, 12, 0, 0)
                                            onQuickAddRequested(c.timeInMillis)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> ElectricBlue
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    // Indicator dots: Income = Green dot, Expense = Red dot
                                    Row(
                                        modifier = Modifier.height(6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (hasIncome) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else MoneyGreen)
                                            )
                                        }
                                        if (hasIncome && hasExpense) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                        if (hasExpense) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else DangerRed)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Blank filler
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
