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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Category
import com.example.domain.models.Transaction
import com.example.domain.models.TransactionType
import com.example.domain.models.Wallet
import com.example.ui.components.CustomButton
import com.example.ui.components.InputField
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    wallets: List<Wallet>,
    onNavigateBack: () -> Unit,
    onSaveTransaction: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf(Category.FOOD) }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 1L) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var isRepeatEnabled by remember { mutableStateOf(false) }
    var repeatInterval by remember { mutableStateOf("Monthly") }
    var note by remember { mutableStateOf("") }
    var attachedImageName by remember { mutableStateOf<String?>(null) }

    var walletDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val selectedWallet = wallets.find { it.id == selectedWalletId } ?: wallets.firstOrNull()
    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("add_transaction_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Transaction",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Type Toggle: Expense / Income
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (type == TransactionType.EXPENSE) DangerRed else Color.Transparent)
                    .clickable { type = TransactionType.EXPENSE }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Expense",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (type == TransactionType.INCOME) MoneyGreen else Color.Transparent)
                    .clickable { type = TransactionType.INCOME }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Income",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Input
        InputField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount (₹)",
            placeholder = "e.g. 500",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Title Input
        InputField(
            value = title,
            onValueChange = { title = it },
            label = "Title",
            placeholder = "e.g. Petrol, Groceries, Biryani"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Wallet Dropdown Menu Box
        ExposedDropdownMenuBox(
            expanded = walletDropdownExpanded,
            onExpandedChange = { walletDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = "${selectedWallet?.name ?: "Select Wallet"} (₹${selectedWallet?.balance?.toInt() ?: 0})",
                onValueChange = {},
                readOnly = true,
                label = { Text("Wallet") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = walletDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            ExposedDropdownMenu(
                expanded = walletDropdownExpanded,
                onDismissRequest = { walletDropdownExpanded = false }
            ) {
                wallets.forEach { w ->
                    DropdownMenuItem(
                        text = { Text("${w.name} (₹${w.balance.toInt()})") },
                        onClick = {
                            selectedWalletId = w.id
                            walletDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Dropdown Menu Box
        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                Category.values().forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.displayName) },
                        onClick = {
                            selectedCategory = cat
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Date Picker display
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = dateStr, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Repeat Option Switch
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Repeat, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Repeat Transaction", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = if (isRepeatEnabled) "Recurring monthly bill/salary" else "One-time entry", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = isRepeatEnabled,
                    onCheckedChange = { isRepeatEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = ElectricBlue)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Attach Image option
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    attachedImageName = "bill_receipt_${System.currentTimeMillis()}.jpg"
                }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (attachedImageName != null) "Receipt Attached: $attachedImageName" else "Attach Bill / Receipt Image",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (attachedImageName != null) "Click to replace image" else "Scan or upload receipt photo",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        CustomButton(
            text = "Save Transaction",
            icon = Icons.Default.Check,
            containerColor = if (type == TransactionType.INCOME) MoneyGreen else DarkNavy,
            onClick = {
                val amtVal = amount.toDoubleOrNull() ?: 0.0
                if (amtVal > 0) {
                    val finalTitle = title.ifBlank { selectedCategory.displayName }
                    val tx = Transaction(
                        title = finalTitle,
                        amount = amtVal,
                        type = type,
                        category = selectedCategory,
                        walletId = selectedWalletId,
                        dateMillis = dateMillis,
                        note = note,
                        repeatInterval = if (isRepeatEnabled) repeatInterval else null,
                        receiptUri = attachedImageName
                    )
                    onSaveTransaction(tx)
                    onNavigateBack()
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}
