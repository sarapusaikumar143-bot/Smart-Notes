package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Wallet
import com.example.domain.models.WalletType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen

@Composable
fun WalletScreen(
    wallets: List<Wallet>,
    onOpenTransfer: () -> Unit,
    onAddWallet: () -> Unit
) {
    val totalBalance = wallets.sumOf { it.balance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("wallet_screen_clean")
    ) {
        // 1. WalletBalance Component
        WalletBalance(
            totalBalance = totalBalance,
            walletCount = wallets.size,
            onOpenTransfer = onOpenTransfer
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. WalletList Component
        WalletList(
            wallets = wallets,
            onAddWallet = onAddWallet
        )
    }
}

// 1. WalletBalance Component
@Composable
fun WalletBalance(
    totalBalance: Double,
    walletCount: Int,
    onOpenTransfer: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Liquid Balance", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "₹${totalBalance.toInt()}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = onOpenTransfer,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Transfer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Synced across $walletCount active accounts", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

// 2. WalletList Component
@Composable
fun WalletList(
    wallets: List<Wallet>,
    onAddWallet: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Accounts & Wallets", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Button(
                onClick = onAddWallet,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wallets, key = { it.id }) { wallet ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ElectricBlue.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val icon = when (wallet.type) {
                                        WalletType.BANK -> Icons.Default.AccountBalance
                                        WalletType.UPI -> Icons.Default.QrCode
                                        WalletType.CASH -> Icons.Default.Payments
                                        WalletType.CARD -> Icons.Default.CreditCard
                                        WalletType.SAVINGS -> Icons.Default.Savings
                                    }
                                    Icon(imageVector = icon, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = wallet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = wallet.type.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(text = "₹${wallet.balance.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
