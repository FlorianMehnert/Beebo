package com.fm.beebo

import com.fm.beebo.datastore.SettingsDataStore
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fm.beebo.ui.LibrariesScreen
import com.fm.beebo.ui.search.LibrarySearchScreen
import com.fm.beebo.ui.settings.SettingsScreen
import com.fm.beebo.ui.theme.BeeboTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsDataStore = SettingsDataStore(applicationContext)

        setContent {
            BeeboTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "librarySearch") {
                    composable("librarySearch") {
                        LibrarySearchScreen(
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settingsDataStore = settingsDataStore,
                            onShowLibraries = { navController.navigate("libraries") },
                            onBackPress = { navController.popBackStack() }
                        )
                    }
                    composable("libraries") {
                        LibrariesScreen(onBackPress = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

