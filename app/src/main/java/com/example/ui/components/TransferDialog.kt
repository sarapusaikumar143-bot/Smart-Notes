package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WalletEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    wallets: List<WalletEntity>,
    onDismiss: () -> Unit,
    onTransferConfirmed: (fromWalletId: Long, toWalletId: Long, amount: Double, note: String) -> Unit
) {
    if (wallets.size < 2) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Insufficient Wallets") },
            text = { Text("You need at least 2 wallets to transfer funds between them.") },
            confirmButton = {
                Button(onClick = onDismiss) { Text("OK") }
            }
        )
        return
    }

    var fromWallet by remember { mutableStateOf(wallets[0]) }
    var toWallet by remember { mutableStateOf(wallets.getOrNull(1) ?: wallets[0]) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("transfer_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transfer Funds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // From Wallet Dropdown
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${fromWallet.name} (₹${fromWallet.balance.toInt()})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Wallet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        wallets.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} (₹${w.balance.toInt()})") },
                                onClick = {
                                    fromWallet = w
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // To Wallet Dropdown
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${toWallet.name} (₹${toWallet.balance.toInt()})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Wallet") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        wallets.filter { it.id != fromWallet.id }.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} (₹${w.balance.toInt()})") },
                                onClick = {
                                    toWallet = w
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Transfer Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Purpose (e.g. ATM Cash withdrawal)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && fromWallet.id != toWallet.id) {
                        onTransferConfirmed(fromWallet.id, toWallet.id, amount, note)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Transfer Now", fontWeight = FontWeight.Bold)
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
