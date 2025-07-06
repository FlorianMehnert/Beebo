package com.fm.beebo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.fm.beebo.viewmodels.UserViewModel

@Composable
fun AppBottomNavigation(
    navController: NavController,
    userViewModel: UserViewModel,
    currentRoute: String
) {
    NavigationBar {
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentRoute == "librarySearch") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Suche"
                )
            },
            label = { Text("Suche") },
            selected = currentRoute == "librarySearch",
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
            label = { Text("Profil") },
            selected = currentRoute == "profile",
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
            label = { Text("Einstellungen") },
            selected = currentRoute == "settings",
            onClick = {
                if (currentRoute != "settings") {
                    navController.navigate("settings")
                }
            }
        )
    }
}