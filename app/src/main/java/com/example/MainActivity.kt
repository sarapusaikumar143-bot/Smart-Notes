package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppLockScreen
import com.example.ui.navigation.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isLocked = viewModel.isAppLocked.collectAsStateWithLifecycle().value
                val userPin = viewModel.userPin.collectAsStateWithLifecycle().value

                if (isLocked) {
                    AppLockScreen(
                        correctPin = userPin,
                        onUnlocked = { viewModel.unlockApp(userPin) }
                    )
                } else {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
