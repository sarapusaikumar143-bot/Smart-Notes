package com.example.ui.components

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.util.ExportManager

enum class ExportFormat {
    PDF_STYLED, EXCEL, CSV, SUMMARY
}

@Composable
fun ExportReportDialog(
    transactions: List<TransactionEntity>,
    wallets: List<WalletEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF_STYLED) }

    val reportText = remember(selectedFormat, transactions, wallets) {
        when (selectedFormat) {
            ExportFormat.PDF_STYLED, ExportFormat.SUMMARY -> ExportManager.generateFormattedReport(transactions, wallets)
            ExportFormat.CSV -> ExportManager.generateCsv(transactions, wallets)
            ExportFormat.EXCEL -> ExportManager.generateCsv(transactions, wallets).replace(",", "\t")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("export_report_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Financial Report", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select format:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFormat == ExportFormat.PDF_STYLED,
                        onClick = { selectedFormat = ExportFormat.PDF_STYLED },
                        label = { Text("PDF / Text", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFormat == ExportFormat.EXCEL,
                        onClick = { selectedFormat = ExportFormat.EXCEL },
                        label = { Text("Excel", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV },
                        label = { Text("CSV", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Report Preview:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ExportManager.shareReport(context, reportText, "Export Financial Report")
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                modifier = Modifier.testTag("share_export_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Export", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
