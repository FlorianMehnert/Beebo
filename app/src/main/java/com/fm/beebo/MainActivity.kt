package com.fm.beebo

import SettingsScreen
import android.content.Context
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.network.configure
import com.fm.beebo.ui.osm.MapScreen
import com.fm.beebo.ui.search.LibrariesScreen
import com.fm.beebo.ui.profile.UserProfileScreen
import com.fm.beebo.ui.search.SearchScreen
import com.fm.beebo.ui.search.details.ItemDetailsScreen
import com.fm.beebo.ui.theme.BeeboTheme
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.UserViewModel
import com.fm.beebo.viewmodels.SettingsViewModel

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var appContext: Context
            private set
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        val settingsDataStore = SettingsDataStore(applicationContext)
        val settingsViewModel = SettingsViewModel()
        val userViewModel = UserViewModel()
        val cookieManager = CookieManager.getInstance()
        cookieManager.configure()

        setContent {
            BeeboTheme {
                val navController = rememberNavController()
                val librarySearchViewModel: LibrarySearchViewModel = viewModel()

                NavHost(navController = navController, startDestination = "librarySearch") {
                    composable("librarySearch") {
                        SearchScreen(
                            onSettingsClick = { navController.navigate("settings") },
                            viewModel = librarySearchViewModel,
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
                    composable ("map") {
                        MapScreen(
                            navController,
                            userViewModel,
                            settingsViewModel
                        )
                    }
                    composable(
                        route = "item_details/{url}",
                        arguments = listOf(navArgument("url") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                        val decodedUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8")

                        ItemDetailsScreen(
                            viewModel = librarySearchViewModel,
                            selectedItemUrl = decodedUrl,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                settingsViewModel.onAppStart()
            }
        }
    }
}
