package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Category
import com.example.domain.models.FinancialPrediction
import com.example.domain.models.Transaction
import com.example.domain.models.TransactionType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed

enum class Period { WEEK, MONTH, YEAR }

@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    prediction: FinancialPrediction
) {
    var selectedPeriod by remember { mutableStateOf(Period.MONTH) }

    val filtered = remember(transactions, selectedPeriod) {
        val now = System.currentTimeMillis()
        val cutoff = when (selectedPeriod) {
            Period.WEEK -> now - 7L * 24 * 60 * 60 * 1000
            Period.MONTH -> now - 30L * 24 * 60 * 60 * 1000
            Period.YEAR -> now - 365L * 24 * 60 * 60 * 1000
        }
        transactions.filter { it.dateMillis >= cutoff }
    }

    val totalIncome = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    val categoryBreakdown = remember(filtered) {
        filtered.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .entries.sortedByDescending { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("analytics_screen_clean")
    ) {
        // 1. Chart Header with Period Selector
        ChartHeader(
            selectedPeriod = selectedPeriod,
            onSelectPeriod = { selectedPeriod = it },
            totalExpense = totalExpense,
            totalIncome = totalIncome
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Donut Chart (Category distribution)
        DonutChart(
            categoryBreakdown = categoryBreakdown,
            totalExpense = totalExpense
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Line Chart (Cashflow trend)
        LineChart(
            transactions = filtered
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Category Breakdown List
        CategoryBreakdown(
            categoryBreakdown = categoryBreakdown,
            totalExpense = totalExpense
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// 1. ChartHeader Component
@Composable
fun ChartHeader(
    selectedPeriod: Period,
    onSelectPeriod: (Period) -> Unit,
    totalExpense: Double,
    totalIncome: Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            Period.values().forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DarkNavy else Color.Transparent)
                        .clickable { onSelectPeriod(period) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Total Spending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "₹${totalExpense.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DangerRed)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Total Inflow", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "+₹${totalIncome.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MoneyGreen)
            }
        }
    }
}

// 2. DonutChart Component
@Composable
fun DonutChart(
    categoryBreakdown: List<Map.Entry<Category, Double>>,
    totalExpense: Double
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Expense Distribution (Donut)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            val anim = remember { Animatable(0f) }
            LaunchedEffect(categoryBreakdown) {
                anim.snapTo(0f)
                anim.animateTo(1f, tween(600))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                val colors = listOf(
                    Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF10B981),
                    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4)
                )

                Canvas(modifier = Modifier.size(130.dp)) {
                    val strokeWidth = 24.dp.toPx()
                    val radius = size.minDimension / 2 - strokeWidth / 2
                    var startAngle = -90f

                    if (categoryBreakdown.isEmpty() || totalExpense == 0.0) {
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            radius = radius,
                            style = Stroke(strokeWidth)
                        )
                    } else {
                        categoryBreakdown.forEachIndexed { i, entry ->
                            val sweep = ((entry.value / totalExpense) * 360f).toFloat() * anim.value
                            drawArc(
                                color = colors[i % colors.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweep
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Spent", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "₹${totalExpense.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// 3. LineChart Component
@Composable
fun LineChart(transactions: List<Transaction>) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Cashflow Trend (Line Chart)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.takeLast(10)
            val maxVal = (expenses.maxOfOrNull { it.amount } ?: 1000.0).toFloat()

            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                if (expenses.size > 1) {
                    val stepX = size.width / (expenses.size - 1)
                    val path = Path()

                    expenses.forEachIndexed { index, tx ->
                        val x = index * stepX
                        val y = size.height - (tx.amount.toFloat() / maxVal * (size.height - 20f))
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

                        drawCircle(
                            color = ElectricBlue,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = ElectricBlue,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

// 4. CategoryBreakdown Component
@Composable
fun CategoryBreakdown(
    categoryBreakdown: List<Map.Entry<Category, Double>>,
    totalExpense: Double
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Category Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            categoryBreakdown.forEach { (cat, amt) ->
                val pct = if (totalExpense > 0) (amt / totalExpense * 100).toInt() else 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(cat.colorHex)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = cat.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "₹${amt.toInt()} ($pct%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
