package com.fm.beebo

import SettingsScreen
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.network.configure
import com.fm.beebo.ui.LibrariesScreen
import com.fm.beebo.ui.profile.UserProfileScreen
import com.fm.beebo.ui.search.SearchScreen
import com.fm.beebo.ui.theme.BeeboTheme
import com.fm.beebo.viewmodels.UserViewModel
import com.fm.beebo.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settingsDataStore = SettingsDataStore(applicationContext)
        val settingsViewModel = SettingsViewModel()
        val userViewModel = UserViewModel()
        val cookieManager = CookieManager.getInstance()
        cookieManager.configure()

        setContent {
            BeeboTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "librarySearch") {
                    composable("librarySearch") {
                        SearchScreen(
                            onSettingsClick = { navController.navigate("settings") },
                            settingsViewModel = settingsViewModel,
                            userViewModel = userViewModel,
                            navController = navController
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            settingsDataStore = settingsDataStore,
                            onShowLibraries = { navController.navigate("libraries") },
                            onBackPress = { navController.popBackStack() },
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }
                    composable("libraries") {
                        LibrariesScreen(onBackPress = { navController.popBackStack() })
                    }
                    composable("profile") {
                        UserProfileScreen(
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }
                }
                settingsViewModel.onAppStart()
            }
        }
    }
}