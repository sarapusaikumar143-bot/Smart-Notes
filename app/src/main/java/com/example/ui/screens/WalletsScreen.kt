package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WalletEntity
import com.example.model.WalletType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletsScreen(
    wallets: List<WalletEntity>,
    onOpenTransferDialog: () -> Unit,
    onAddWallet: (name: String, type: WalletType, balance: Double, colorHex: String) -> Unit
) {
    var showAddWalletDialog by remember { mutableStateOf(false) }
    val totalNetWorth = remember(wallets) {
        wallets.sumOf { it.balance }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallets_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp)
    ) {
        // Total Net Worth Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(DarkNavy, Color(0xFF1E293B))
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
                                    text = "Total Liquid Net Worth",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${NumberFormat.getNumberInstance(Locale.getDefault()).format(totalNetWorth.toLong())}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = onOpenTransferDialog,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.testTag("transfer_button_header")
                            ) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Transfer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Managed across ${wallets.size} wallets • Real-time synchronization",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section Title & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Wallets & Accounts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { showAddWalletDialog = true }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Wallet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Wallet Cards
        items(wallets, key = { it.id }) { wallet ->
            WalletCardItem(wallet = wallet, onTransfer = onOpenTransferDialog)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    if (showAddWalletDialog) {
        AddWalletDialog(
            onDismiss = { showAddWalletDialog = false },
            onAdd = { name, type, balance, color ->
                onAddWallet(name, type, balance, color)
                showAddWalletDialog = false
            }
        )
    }
}

@Composable
fun WalletCardItem(
    wallet: WalletEntity,
    onTransfer: () -> Unit
) {
    val icon = when (wallet.type) {
        WalletType.BANK -> Icons.Default.AccountBalance
        WalletType.UPI -> Icons.Default.QrCode
        WalletType.CASH -> Icons.Default.Payments
        WalletType.CARD -> Icons.Default.CreditCard
        WalletType.SAVINGS -> Icons.Default.Savings
    }

    val cardColor = try {
        Color(android.graphics.Color.parseColor(wallet.colorHex))
    } catch (e: Exception) {
        ElectricBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_card_${wallet.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = cardColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = wallet.name,
                            tint = cardColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = wallet.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = wallet.type.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${NumberFormat.getNumberInstance(Locale.getDefault()).format(wallet.balance.toLong())}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MoneyGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: WalletType, balance: Double, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.BANK) }
    var balanceText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val colorHex = when (selectedType) {
        WalletType.BANK -> "#3B82F6"
        WalletType.UPI -> "#22C55E"
        WalletType.CASH -> "#F59E0B"
        WalletType.CARD -> "#8B5CF6"
        WalletType.SAVINGS -> "#06B6D4"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Wallet / Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name (e.g. HDFC Bank, GPay)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wallet Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        WalletType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text("Initial Balance (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onAdd(name, selectedType, bal, colorHex)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Add Wallet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
