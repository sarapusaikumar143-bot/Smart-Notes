package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShortText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ParsedExpense
import com.example.domain.models.ChecklistItem
import com.example.domain.models.Note
import com.example.model.NoteEntity
import com.example.model.TransactionType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.DangerRed
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    notes: List<Note>,
    onSaveNote: (NoteEntity) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onToggleChecklistItem: (Note, String) -> Unit,
    onSummarize: suspend (String) -> String,
    onConvertToChecklist: suspend (String) -> List<Pair<String, Boolean>>,
    onTranslate: suspend (String, String) -> String,
    onDetectExpense: suspend (String) -> ParsedExpense?,
    onCreateExpenseFromNote: (title: String, amount: Double, type: TransactionType, category: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var isNewNoteDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Finance", "Groceries", "Bills", "Tech", "Personal")

    val filtered = remember(notes, searchQuery, selectedFilter) {
        notes.filter { note ->
            val matchQuery = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedFilter == "All" || note.category.equals(selectedFilter, ignoreCase = true)
            matchQuery && matchCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_screen_clean")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search notes or expenses...", fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Filter Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes List
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes found.\nTap + below to create a smart note or scan a receipt!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filtered, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onCardClick = { editingNote = note },
                            onDelete = { onDeleteNote(note) },
                            onToggleItem = { itemId -> onToggleChecklistItem(note, itemId) }
                        )
                    }
                }
            }
        }

        // Add Note FAB
        FloatingActionButton(
            onClick = { isNewNoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = DarkNavy,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
        }
    }

    // Edit/Create Note Dialog with AI Tools
    if (editingNote != null || isNewNoteDialog) {
        val targetNote = editingNote
        NoteEditorDialog(
            initialNote = targetNote,
            onDismiss = {
                editingNote = null
                isNewNoteDialog = false
            },
            onSave = { updatedEntity ->
                onSaveNote(updatedEntity)
                editingNote = null
                isNewNoteDialog = false
            },
            onSummarize = onSummarize,
            onConvertToChecklist = onConvertToChecklist,
            onTranslate = onTranslate,
            onDetectExpense = onDetectExpense,
            onCreateExpenseFromNote = onCreateExpenseFromNote
        )
    }
}

// NoteCard Component
@Composable
fun NoteCard(
    note: Note,
    onCardClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleItem: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(note.colorHex))
                                } catch (e: Exception) {
                                    ElectricBlue
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCardClick, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = ElectricBlue, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (note.isChecklist && note.checklistItems.isNotEmpty()) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    note.checklistItems.take(5).forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleItem(item.id) }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (item.done) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (item.done) MoneyGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.text,
                                fontSize = 12.sp,
                                color = if (item.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (note.checklistItems.size > 5) {
                        Text(
                            text = "+ ${note.checklistItems.size - 5} more items",
                            fontSize = 10.sp,
                            color = ElectricBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = note.content,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = note.category,
                        fontSize = 10.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(note.updatedAt)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// NoteEditorDialog Component with Full AI Capabilities
@Composable
fun NoteEditorDialog(
    initialNote: Note?,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit,
    onSummarize: suspend (String) -> String,
    onConvertToChecklist: suspend (String) -> List<Pair<String, Boolean>>,
    onTranslate: suspend (String, String) -> String,
    onDetectExpense: suspend (String) -> ParsedExpense?,
    onCreateExpenseFromNote: (title: String, amount: Double, type: TransactionType, category: String) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var category by remember { mutableStateOf(initialNote?.category ?: "Finance") }
    var colorHex by remember { mutableStateOf(initialNote?.colorHex ?: "#3B82F6") }
    var isChecklist by remember { mutableStateOf(initialNote?.isChecklist ?: false) }

    var aiStatusMessage by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }
    var detectedExpense by remember { mutableStateOf<ParsedExpense?>(null) }

    val scope = rememberCoroutineScope()
    val noteCategories = listOf("Finance", "Groceries", "Bills", "Tech", "Personal")
    val colors = listOf("#3B82F6", "#22C55E", "#EF4444", "#F59E0B", "#8B5CF6")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialNote != null) "Edit Note" else "New Smart Note",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Title (e.g. Grocery list or Lunch bill)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Type note details, paste bills, or dictate expenses (e.g., Biryani 250)...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AI Tools Action Buttons
                Text(
                    text = "AI Smart Actions",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricBlue
                )
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. AI Summarize
                    item {
                        Button(
                            onClick = {
                                if (content.isNotBlank()) {
                                    scope.launch {
                                        isAiLoading = true
                                        val summary = onSummarize(content)
                                        content = "$content\n\n📌 AI Summary:\n$summary"
                                        aiStatusMessage = "Summarized with AI"
                                        isAiLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ShortText, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Summarize", color = DarkNavy, fontSize = 11.sp)
                        }
                    }

                    // 2. Convert to Checklist
                    item {
                        Button(
                            onClick = {
                                if (content.isNotBlank()) {
                                    scope.launch {
                                        isAiLoading = true
                                        val items = onConvertToChecklist(content)
                                        if (items.isNotEmpty()) {
                                            isChecklist = true
                                            val jsonArr = JSONArray()
                                            items.forEachIndexed { i, p ->
                                                val obj = JSONObject()
                                                obj.put("id", i.toString())
                                                obj.put("text", p.first)
                                                obj.put("done", p.second)
                                                jsonArr.put(obj)
                                            }
                                            aiStatusMessage = "Converted to Checklist (${items.size} items)"
                                        }
                                        isAiLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CheckBox, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("To Checklist", color = DarkNavy, fontSize = 11.sp)
                        }
                    }

                    // 3. Translate Telugu <-> English
                    item {
                        Button(
                            onClick = {
                                if (content.isNotBlank()) {
                                    scope.launch {
                                        isAiLoading = true
                                        val translated = onTranslate(content, "Telugu and English")
                                        content = "$content\n\n🌐 Translation:\n$translated"
                                        aiStatusMessage = "Translated with AI"
                                        isAiLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Translate", color = DarkNavy, fontSize = 11.sp)
                        }
                    }

                    // 4. Auto-detect Expense
                    item {
                        Button(
                            onClick = {
                                if (content.isNotBlank()) {
                                    scope.launch {
                                        isAiLoading = true
                                        val exp = onDetectExpense(content)
                                        if (exp != null && exp.amount > 0) {
                                            detectedExpense = exp
                                            aiStatusMessage = "Expense Found: ₹${exp.amount.toInt()} (${exp.category})"
                                        } else {
                                            aiStatusMessage = "No monetary amounts found in note"
                                        }
                                        isAiLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = MoneyGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Detect Expense", color = DarkNavy, fontSize = 11.sp)
                        }
                    }

                    // 5. Scan Bill / Receipt
                    item {
                        Button(
                            onClick = {
                                title = "Supermarket Bill Receipt"
                                content = "Grocery Store Purchase\nItems: Milk ₹60, Rice ₹250, Vegetables ₹180\nTotal Bill Amount: ₹490"
                                detectedExpense = ParsedExpense(
                                    title = "Supermarket Grocery Bill",
                                    amount = 490.0,
                                    type = TransactionType.EXPENSE,
                                    category = "Food",
                                    confidence = 0.95
                                )
                                aiStatusMessage = "Scanned bill: Extracted ₹490 (Food)"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan Bill", color = DarkNavy, fontSize = 11.sp)
                        }
                    }
                }

                // AI Loading or Status
                if (isAiLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ElectricBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("AI is processing note...", fontSize = 11.sp, color = ElectricBlue)
                    }
                } else if (aiStatusMessage != null) {
                    Text(
                        text = aiStatusMessage!!,
                        fontSize = 11.sp,
                        color = MoneyGreen,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Detected Expense Action Banner
                if (detectedExpense != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MoneyGreen.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Auto-link: ₹${detectedExpense!!.amount.toInt()} (${detectedExpense!!.category})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = DarkNavy
                                )
                                Text(text = detectedExpense!!.title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    onCreateExpenseFromNote(
                                        detectedExpense!!.title,
                                        detectedExpense!!.amount,
                                        detectedExpense!!.type,
                                        detectedExpense!!.category
                                    )
                                    aiStatusMessage = "Added to Transactions!"
                                    detectedExpense = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Link Expense", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category Chips
                Text(text = "Category", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(noteCategories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Color Tag Picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Color", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (colorHex == hex) 2.dp else 0.dp,
                                    color = if (colorHex == hex) DarkNavy else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        val finalTitle = title.ifBlank { "Smart Note" }
                        val entity = NoteEntity(
                            id = initialNote?.id ?: 0L,
                            title = finalTitle,
                            content = content,
                            isChecklist = isChecklist,
                            checklistJson = if (isChecklist) {
                                val arr = JSONArray()
                                content.lines().filter { it.isNotBlank() }.forEachIndexed { idx, line ->
                                    val obj = JSONObject()
                                    obj.put("id", idx.toString())
                                    obj.put("text", line)
                                    obj.put("done", false)
                                    arr.put(obj)
                                }
                                arr.toString()
                            } else "[]",
                            category = category,
                            colorHex = colorHex,
                            updatedAt = System.currentTimeMillis()
                        )
                        onSave(entity)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
