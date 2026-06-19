package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.repository.ChatRepository
import com.example.ui.screens.NogramAppContainer
import com.example.ui.screens.NogramRoute
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programmatic Firebase initialization with official metadata
        val options = FirebaseOptions.Builder()
            .setApplicationId("1:932509748743:android:deaedb6b2a99be36efe4a2")
            .setProjectId("nogramoffical")
            .setDatabaseUrl("https://nogramoffical-default-rtdb.asia-southeast1.firebasedatabase.app")
            .setApiKey("AIzaSyCb_R9j7MsUemFzLLBnmPPGFPNMKFrCMlQ")
            .build()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options)
        }

        val repository = ChatRepository.getInstance(this)
        val auth = FirebaseAuth.getInstance()

        setContent {
            MyApplicationTheme {
                // Determine start screen depending on active Firebase login session state
                val initialRoute = if (auth.currentUser != null) NogramRoute.MAIN_CHATS else NogramRoute.ONBOARDING
                var currentRoute by remember { mutableStateOf(initialRoute) }
                val backstack = remember { mutableStateListOf<NogramRoute>() }

                val navigateTo: (NogramRoute) -> Unit = { route ->
                    backstack.add(currentRoute)
                    currentRoute = route
                }

                val navigateBack: () -> Unit = {
                    if (backstack.isNotEmpty()) {
                        currentRoute = backstack.removeAt(backstack.lastIndex)
                    } else {
                        finish()
                    }
                }

                // Register standard Android back handler callback
                val dispatcher = this.onBackPressedDispatcher
                DisposableEffect(currentRoute) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            navigateBack()
                        }
                    }
                    dispatcher.addCallback(callback)
                    onDispose {
                        callback.remove()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Wrap inside safe padding box representation
                    NogramAppContainer(
                        repository = repository,
                        currentRoute = currentRoute,
                        onNavigate = navigateTo,
                        onBack = navigateBack
                    )
                }
            }
        }
    }
}
