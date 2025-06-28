package com.fm.beebo.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.AppBottomNavigation
import com.fm.beebo.ui.search.components.BallIndicator
import com.fm.beebo.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    var username by remember { mutableStateOf(userViewModel.username) }
    var password by remember { mutableStateOf(userViewModel.password) }
    val focusManager = LocalFocusManager.current
    val settingsDataStore = SettingsDataStore(LocalContext.current)
    val switchToBottomNavigationFlow by settingsDataStore.switchToBottomNavigationFlow.collectAsState(initial=false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Bibo Dresden") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                })
        },
        bottomBar = {
            if (switchToBottomNavigationFlow) {
                AppBottomNavigation(
                    navController = navController,
                    userViewModel = userViewModel,
                    currentRoute = "profile"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Benutzername") },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .semantics {
                        contentType = ContentType.Username
                    },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .semantics {
                        contentType = ContentType.Password
                    },
                keyboardActions = KeyboardActions(
                    onDone = {
                        userViewModel.username = username
                        userViewModel.password = password
                        userViewModel.login()
                        navController.popBackStack()
                    }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (userViewModel.isLoading) {
                BallIndicator(color = MaterialTheme.colorScheme.primary, diameter = 32.dp)
            } else {
                if (userViewModel.isLoggedIn) {
                    Button(
                        onClick = {
                            userViewModel.logout()
                            navController.popBackStack()
                        }, shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Abmelden")
                    }
                } else {
                    Button(
                        onClick = {
                            userViewModel.username = username
                            userViewModel.password = password
                            userViewModel.login()
                            navController.popBackStack()
                        }, shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Anmelden")
                    }
                }
            }

            userViewModel.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Color.Red)
            }
        }
    }
}