package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.DangerRed

@Composable
fun AppLockScreen(
    correctPin: String,
    onUnlocked: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    fun handleDigit(d: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + d
            enteredPin = newPin
            showError = false
            if (newPin.length == 4) {
                if (newPin == correctPin) {
                    onUnlocked()
                } else {
                    showError = true
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            showError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .testTag("app_lock_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = ElectricBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = ElectricBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AI Money Security",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter your 4-digit PIN to unlock",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val filled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (filled) ElectricBlue else Color.Transparent)
                            .border(2.dp, if (showError) DangerRed else Color(0xFF64748B), CircleShape)
                    )
                }
            }

            AnimatedVisibility(visible = showError) {
                Text(
                    text = "Incorrect PIN. Try again.",
                    color = DangerRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Keypad
            val digits = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in digits) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (item in row) {
                            if (item.isEmpty()) {
                                Spacer(modifier = Modifier.size(64.dp))
                            } else if (item == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { handleBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .clickable { handleDigit(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Default PIN is 1234",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
