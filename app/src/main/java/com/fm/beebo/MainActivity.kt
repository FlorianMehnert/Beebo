package com.fm.beebo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.fm.beebo.ui.LibrarySearchScreen
import com.fm.beebo.ui.theme.BeeboTheme
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fm.beebo.ui.SettingsScreen
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.switchPreference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeeboTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "librarySearch"
                ) {
                    composable("librarySearch") {
                        LibrarySearchScreen(
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            onBackPress = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
