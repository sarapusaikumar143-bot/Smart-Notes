package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ai.ParsedExpense
import com.example.ai.SmartExpenseParser
import com.example.model.TransactionType
import com.example.model.WalletEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickAddTransactionSheet(
    targetDateMillis: Long,
    wallets: List<WalletEntity>,
    expenseParser: SmartExpenseParser,
    onDismiss: () -> Unit,
    onSaveTransaction: (title: String, amount: Double, type: TransactionType, category: String, walletId: Long, note: String) -> Unit,
    onOpenVoiceInput: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var naturalInput by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: 1L) }
    var note by remember { mutableStateOf("") }
    var aiParsedBadge by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Food", "Transport", "Bills", "Shopping", "Entertainment", "Health", "Salary", "Investment", "Other")

    // Live AI Auto-Categorize trigger
    LaunchedEffect(naturalInput) {
        if (naturalInput.isNotBlank()) {
            val parsed = expenseParser.parseOffline(naturalInput)
            if (parsed.amount > 0 || parsed.title.isNotBlank()) {
                title = parsed.title
                if (parsed.amount > 0) {
                    amountText = if (parsed.amount % 1.0 == 0.0) parsed.amount.toInt().toString() else parsed.amount.toString()
                }
                selectedType = parsed.type
                selectedCategory = parsed.category
                aiParsedBadge = "🤖 AI Detected: ${parsed.category} • ${parsed.type.name} • ₹${parsed.amount.toInt()}"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("quick_add_transaction_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Add Transaction",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Natural AI Input Bar with Voice Button
            OutlinedTextField(
                value = naturalInput,
                onValueChange = { naturalInput = it },
                label = { Text("Smart AI Input (e.g., 'Biryani 250', 'Petrol 500')") },
                placeholder = { Text("Type or tap mic...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_natural_input"),
                trailingIcon = {
                    IconButton(
                        onClick = onOpenVoiceInput,
                        modifier = Modifier.testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = ElectricBlue
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // AI Detection feedback banner
            AnimatedVisibility(visible = aiParsedBadge != null) {
                Surface(
                    color = ElectricBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = aiParsedBadge ?: "",
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Income / Expense Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.EXPENSE) DangerRed else Color.Transparent)
                        .clickable { selectedType = TransactionType.EXPENSE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType == TransactionType.INCOME) MoneyGreen else Color.Transparent)
                        .clickable { selectedType = TransactionType.INCOME }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount & Title Fields
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("amount_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Merchant") },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("title_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Selection
            Text(
                text = "Select Wallet",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                wallets.forEach { wallet ->
                    val isSelected = wallet.id == selectedWalletId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedWalletId = wallet.id },
                        label = { Text("${wallet.name} (₹${wallet.balance.toInt()})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection Chips
            Text(
                text = "Category",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (selectedType == TransactionType.INCOME) MoneyGreen else DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Tag (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val finalTitle = title.ifBlank { selectedCategory }
                    if (amount > 0) {
                        onSaveTransaction(
                            finalTitle,
                            amount,
                            selectedType,
                            selectedCategory,
                            selectedWalletId,
                            note
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.INCOME) MoneyGreen else DarkNavy
                )
            ) {
                Text(
                    text = "Save ${selectedType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
