package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.NexusNavGraph
import com.example.ui.theme.NexusTheme
import com.example.ui.viewmodel.MangaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MangaViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        com.example.util.InAppUpdateManager.checkPendingInstall(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val homeState by viewModel.homeUiState.collectAsState()
            val settings = homeState.appSettings

            NexusTheme(
                themeMode = settings.themeMode,
                backgroundStyle = settings.backgroundStyle,
                accentColor = settings.accentColor
            ) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    NexusNavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
