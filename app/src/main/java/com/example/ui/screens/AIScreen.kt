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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.viewmodel.ChatMessageItem

@Composable
fun AIScreen(
    messages: List<ChatMessageItem>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onVoiceClick: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_screen_clean")
    ) {
        // 1. Chat Messages
        Box(modifier = Modifier.weight(1f)) {
            ChatMessages(messages = messages, isLoading = isLoading)
        }

        // 2. Suggestion Chips
        SuggestionChips(
            onSelectSuggestion = { onSendMessage(it) }
        )

        // 3. Input bar with Voice Input Button
        VoiceInputBar(
            value = textInput,
            onValueChange = { textInput = it },
            onSend = {
                if (textInput.isNotBlank()) {
                    onSendMessage(textInput)
                    textInput = ""
                }
            },
            onVoiceClick = onVoiceClick
        )
    }
}

@Composable
fun ChatMessages(
    messages: List<ChatMessageItem>,
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (msg.isUser) DarkNavy else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = msg.text,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ElectricBlue, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI advisor is analyzing your data...", fontSize = 12.sp, color = ElectricBlue)
                }
            }
        }
    }
}

@Composable
fun SuggestionChips(onSelectSuggestion: (String) -> Unit) {
    val suggestions = listOf(
        "How can I save more money?",
        "What is my daily burn rate?",
        "Where did I spend the most?",
        "Can I afford a ₹3,000 trip?"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(suggestions) { s ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onSelectSuggestion(s) }
            ) {
                Text(
                    text = s,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun VoiceInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onVoiceClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ElectricBlue.copy(alpha = 0.15f))
        ) {
            Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = ElectricBlue)
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Ask your money questions...", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DarkNavy)
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
