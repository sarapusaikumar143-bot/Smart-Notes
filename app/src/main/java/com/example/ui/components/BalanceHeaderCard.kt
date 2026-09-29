package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceHeaderCard(
    totalIncome: Double,
    totalExpense: Double,
    periodLabel: String = "This Month",
    modifier: Modifier = Modifier
) {
    val netBalance = totalIncome - totalExpense
    val totalVolume = totalIncome + totalExpense
    val incomeRatio = if (totalVolume > 0) (totalIncome / totalVolume).toFloat() else 0.5f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balance_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkNavy, Color(0xFF1E293B))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Net Balance ($periodLabel)",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${NumberFormat.getNumberInstance(Locale.getDefault()).format(netBalance.toLong())}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netBalance >= 0) Color.White else DangerRed
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = ElectricBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet Balance",
                                tint = ElectricBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress ratio bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF334155))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(incomeRatio.coerceIn(0.05f, 0.95f))
                            .height(6.dp)
                            .background(MoneyGreen)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(DangerRed)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Income Item
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MoneyGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Income",
                                    tint = MoneyGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Income", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "+₹${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalIncome.toLong())}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MoneyGreen
                            )
                        }
                    }

                    // Expense Item
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = DangerRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Expense",
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Expense", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "-₹${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalExpense.toLong())}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}
