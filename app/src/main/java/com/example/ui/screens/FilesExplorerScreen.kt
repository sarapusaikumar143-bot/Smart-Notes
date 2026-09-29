package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NoteEntity
import com.example.model.TransactionEntity
import com.example.model.WalletEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueSoft
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.MoneyGreenSoft
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.util.AppFileItem
import com.example.util.AppFileManager
import com.example.util.FileCategory
import com.example.util.StorageStats

@Composable
fun FilesExplorerScreen(
    transactions: List<TransactionEntity>,
    wallets: List<WalletEntity>,
    notes: List<NoteEntity>
) {
    val context = LocalContext.current

    var filesList by remember { mutableStateOf<List<AppFileItem>>(emptyList()) }
    var storageStats by remember {
        mutableStateOf(
            StorageStats(0, 0, 0, "0 B", "0 B")
        )
    }
    var selectedCategory by remember { mutableStateOf(FileCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    var previewFileItem by remember { mutableStateOf<AppFileItem?>(null) }
    var previewContent by remember { mutableStateOf("") }
    var showApkDownloadGuide by remember { mutableStateOf(false) }
    var showAabGuide by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<AppFileItem?>(null) }

    fun refreshFiles() {
        AppFileManager.ensureInitialFiles(context, transactions, wallets, notes)
        filesList = AppFileManager.loadAllFiles(context)
        storageStats = AppFileManager.getStorageStats(context)
    }

    LaunchedEffect(Unit) {
        refreshFiles()
    }

    val filteredFiles = remember(filesList, selectedCategory, searchQuery) {
        filesList.filter { file ->
            val matchesCategory = selectedCategory == FileCategory.ALL || file.category == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                    file.name.contains(searchQuery, ignoreCase = true) ||
                    file.extension.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("files_explorer_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Files Explorer",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage exports, documents & Apk Dubb artifacts",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { refreshFiles() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("refresh_files_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Files",
                        tint = DarkNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Featured APK Dubb File Card
        item {
            ApkDubbFileCard(
                onOpenGuide = { showApkDownloadGuide = true },
                onShareApkInfo = {
                    val manifestText = AppFileManager.buildApkManifestContent(context)
                    AppFileManager.shareText(context, manifestText, "Share APK Dubb Specifications")
                },
                onGenerateManifest = {
                    val file = AppFileManager.generateApkManifestFile(context)
                    Toast.makeText(context, "Generated: ${file.name}", Toast.LENGTH_SHORT).show()
                    refreshFiles()
                },
                onAddApkToFolder = {
                    val file = AppFileManager.ensureApkFile(context)
                    Toast.makeText(context, "Added ${file.name} to Explore files!", Toast.LENGTH_SHORT).show()
                    refreshFiles()
                }
            )
        }

        // 2b. Featured Live AAB Bundle File Card
        item {
            LiveAabBundleCard(
                onOpenGuide = { showAabGuide = true },
                onShareAabInfo = {
                    val manifestText = AppFileManager.buildAabManifestContent(context)
                    AppFileManager.shareText(context, manifestText, "Share Live AAB Specifications")
                },
                onGenerateManifest = {
                    val file = AppFileManager.generateAabManifestFile(context)
                    Toast.makeText(context, "Generated: ${file.name}", Toast.LENGTH_SHORT).show()
                    refreshFiles()
                }
            )
        }

        // 3. Storage Diagnostics Overview
        item {
            StorageOverviewCard(
                stats = storageStats,
                onClearCache = {
                    val freed = AppFileManager.clearCache(context)
                    Toast.makeText(
                        context,
                        "Cleaned ${AppFileManager.formatBytes(freed)} cache",
                        Toast.LENGTH_SHORT
                    ).show()
                    refreshFiles()
                }
            )
        }

        // 4. Quick File Generation Actions
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Generate New Export:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val f = AppFileManager.generateCsvExportFile(context, transactions, wallets)
                            Toast.makeText(context, "Created ${f.name}", Toast.LENGTH_SHORT).show()
                            refreshFiles()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gen_csv_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val f = AppFileManager.generateReportExportFile(context, transactions, wallets)
                            Toast.makeText(context, "Created ${f.name}", Toast.LENGTH_SHORT).show()
                            refreshFiles()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gen_report_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audit Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val f = AppFileManager.generateJsonBackupFile(context, transactions, wallets, notes)
                            Toast.makeText(context, "Created ${f.name}", Toast.LENGTH_SHORT).show()
                            refreshFiles()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gen_json_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("JSON Backup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 5. Search Bar & Category Filter
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search files by name or extension...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate500)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_files_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Slate200
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categories Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FileCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("category_chip_${category.name}")
                        )
                    }
                }
            }
        }

        // 6. Section Header & Files Count
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage Directory (${filteredFiles.size} items)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to preview or share",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 7. Files List or Empty State
        if (filteredFiles.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No files found in this category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap one of the 'Generate New Export' buttons above to create an export.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredFiles, key = { it.id }) { item ->
                FileListItemCard(
                    fileItem = item,
                    onPreview = {
                        previewFileItem = item
                        previewContent = AppFileManager.readFileContent(item.file)
                    },
                    onShare = {
                        AppFileManager.shareFile(context, item.file)
                    },
                    onDelete = {
                        fileToDelete = item
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal: Preview File Dialog
    if (previewFileItem != null) {
        AlertDialog(
            onDismissRequest = { previewFileItem = null },
            modifier = Modifier.testTag("file_preview_dialog"),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getIconForExtension(previewFileItem!!.extension),
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = previewFileItem!!.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${previewFileItem!!.formattedSize} • ${previewFileItem!!.formattedDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = previewContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(previewFileItem!!.name, previewContent))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }

                    if (previewFileItem!!.extension == "apk") {
                        Button(
                            onClick = {
                                AppFileManager.installApk(context, previewFileItem!!.file)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Install", color = DarkNavy, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            AppFileManager.shareFile(context, previewFileItem!!.file)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { previewFileItem = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: APK Dubb Download Guide
    if (showApkDownloadGuide) {
        ApkDownloadGuideDialog(onDismiss = { showApkDownloadGuide = false })
    }

    // Modal: Live AAB Guide
    if (showAabGuide) {
        AabGuideDialog(onDismiss = { showAabGuide = false })
    }

    // Modal: Confirm Delete File
    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Delete File?", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Are you sure you want to delete ${fileToDelete!!.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        AppFileManager.deleteFile(fileToDelete!!.file)
                        Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                        fileToDelete = null
                        refreshFiles()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------
// UI Subcomponents
// ----------------------------------------------------

@Composable
fun ApkDubbFileCard(
    onOpenGuide: () -> Unit,
    onShareApkInfo: () -> Unit,
    onGenerateManifest: () -> Unit,
    onAddApkToFolder: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("apk_dubb_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MoneyGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Android APK",
                            tint = MoneyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Apk Dubb File",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "app-debug.apk (Development Build)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MoneyGreen.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "READY",
                        color = MoneyGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(label = "VARIANT", value = "Debug (Dubb)")
                SpecItem(label = "VERSION", value = "1.0 (Code 1)")
                SpecItem(label = "TARGET SDK", value = "Android 16 (36)")
                SpecItem(label = "KEYSTORE", value = "debugConfig")
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Primary Add app-debug.apk to Folder Button
            Button(
                onClick = onAddApkToFolder,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_apk_to_folder_btn"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add app-debug.apk to Explore Files", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenGuide,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShareApkInfo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Specs", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onGenerateManifest,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manifest", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun StorageOverviewCard(
    stats: StorageStats,
    onClearCache: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "App Storage: ${stats.formattedAppFilesSize}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${stats.totalFilesCount} files stored • Cache: ${stats.formattedCacheSize}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(
                onClick = onClearCache,
                colors = ButtonDefaults.textButtonColors(contentColor = ElectricBlue)
            ) {
                Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clean Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FileListItemCard(
    fileItem: AppFileItem,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val (iconColor, iconBg) = when (fileItem.category) {
        FileCategory.APK_BUILD -> MoneyGreen to MoneyGreenSoft
        FileCategory.FINANCIAL_REPORT -> ElectricBlue to ElectricBlueSoft
        FileCategory.DATABASE_BACKUP -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
        else -> Slate800 to Slate100
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() }
            .testTag("file_item_${fileItem.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForExtension(fileItem.extension),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = fileItem.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${fileItem.formattedSize} • ${fileItem.formattedDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (fileItem.extension == "apk") {
                    IconButton(
                        onClick = { AppFileManager.installApk(context, fileItem.file) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Install APK",
                            tint = MoneyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onPreview, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Preview",
                        tint = ElectricBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DarkNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ApkDownloadGuideDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val adbCmd = "adb install -r app/build/outputs/apk/debug/app-debug.apk"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("apk_guide_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = MoneyGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to Download APK Dubb File", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Method 1: Direct Download from Google AI Studio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkNavy
                )
                Text(
                    text = "1. Look at the top-right corner of Google AI Studio.\n" +
                            "2. Click the 'Settings / Export' menu (or Share button).\n" +
                            "3. Select 'Export / Download APK' (or AAB).\n" +
                            "4. Download the generated 'app-debug.apk' and install it on any Android device.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Divider()

                Text(
                    text = "Method 2: Command Line ADB Install",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkNavy
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adbCmd,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Slate800,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCmd))
                                Toast.makeText(context, "Command copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Got It")
            }
        }
    )
}

fun getIconForExtension(ext: String) = when (ext.lowercase()) {
    "apk" -> Icons.Default.Android
    "aab" -> Icons.Default.CloudDownload
    "csv" -> Icons.Default.TableChart
    "json" -> Icons.Default.Storage
    "txt" -> Icons.Default.Description
    else -> Icons.Default.InsertDriveFile
}

@Composable
fun LiveAabBundleCard(
    onOpenGuide: () -> Unit,
    onShareAabInfo: () -> Unit,
    onGenerateManifest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_aab_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Android App Bundle",
                            tint = ElectricBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live AAB Bundle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "app-release.aab (Play Store Production)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricBlue.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "LIVE AAB",
                        color = ElectricBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // GitHub repository banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GitHub: sarapusaikumar143-bot/Smart-Notes",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpecItem(label = "FORMAT", value = ".aab Bundle")
                SpecItem(label = "VARIANT", value = "Release/Live")
                SpecItem(label = "DELIVERY", value = "Dynamic Split")
                SpecItem(label = "TARGET SDK", value = "Android 16 (36)")
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenGuide,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AAB Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onShareAabInfo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Specs", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = onGenerateManifest,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manifest", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AabGuideDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val gradleAabCmd = "gradle bundleRelease"
    val gradleApkCmd = "gradle assembleDebug"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("aab_guide_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Live AAB (App Bundle) Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "GitHub Repository: sarapusaikumar143-bot/Smart-Notes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ElectricBlue
                )

                Text(
                    text = "1. Difference between APK and AAB:\n" +
                            "• APK (Dub APK): Directly installable on Android devices.\n" +
                            "• AAB (Live Bundle): The official publishing format for Google Play Store. It generates optimized APKs for every device architecture.\n\n" +
                            "2. How to Export from Google AI Studio:\n" +
                            "• Click 'Export' / 'Settings' in the top-right toolbar.\n" +
                            "• Select 'Export AAB' (or 'Download APK').\n" +
                            "• Or select 'Push to GitHub' with repo name: 'Smart-Notes'.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Divider()

                Text(
                    text = "Gradle Build Commands:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkNavy
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Slate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Build Live AAB: $gradleAabCmd",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Build Dub APK: $gradleApkCmd",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Slate800
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Got It")
            }
        }
    )
}
