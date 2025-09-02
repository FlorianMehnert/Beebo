package com.fm.beebo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.viewmodels.UserViewModel

@Composable
fun AppBottomNavigation(
    navController: NavController,
    userViewModel: UserViewModel,
    currentRoute: String
) {
    val settingsDataStore = SettingsDataStore(LocalContext.current)
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentRoute == "librarySearch") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Suche"
                )
            },
            label = {
                Text(
                    text = "Suche",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == "librarySearch",
            alwaysShowLabel = false, // Only show label when selected
            onClick = {
                if (currentRoute != "librarySearch") {
                    navController.navigate("librarySearch") {
                        popUpTo("librarySearch") { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (userViewModel.isLoggedIn) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profil"
                )
            },
            label = {
                Text(
                    text = "Profil",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == "profile",
            alwaysShowLabel = false, // Only show label when selected
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile")
                }
            }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentRoute == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Einstellungen"
                )
            },
            label = {
                Text(
                    text = "Einstellungen",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == "settings",
            alwaysShowLabel = false, // Only show label when selected
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings")
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    if (currentRoute == "map") Icons.Filled.Map else Icons.Outlined.Map,
                    contentDescription = "Übersichtskarte"
                )
            },
            label = {
                Text(
                    text = "Standorte",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = currentRoute == "map",
            alwaysShowLabel = false, // Only show label when selected
            onClick = {
                if (currentRoute != "map") {
                    navController.navigate("map")
                }
            }
        )
    }
}
