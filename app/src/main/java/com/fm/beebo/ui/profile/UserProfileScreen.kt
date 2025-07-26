package com.fm.beebo.ui.profile

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.network.NetworkConfig
import com.fm.beebo.network.syncToHttpClient
import com.fm.beebo.ui.CustomWebViewClient
import com.fm.beebo.ui.components.AppBottomNavigation
import com.fm.beebo.ui.components.BallIndicator
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
    var isWebViewVisible by remember { mutableStateOf(false) }

    // Fetch account details when logged in
    LaunchedEffect(userViewModel.isLoggedIn) {
        if (userViewModel.isLoggedIn && userViewModel.accountFees == null) {
            userViewModel.fetchAccountDetails()
        }
    }

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
                },
                actions = {
                    if (userViewModel.isLoggedIn) {
                        ToggleIconButton(
                            checked = isWebViewVisible,
                            onCheckedChange = { isWebViewVisible = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // In UserProfileScreen.kt, update the WebView section:
            if (isWebViewVisible && userViewModel.isLoggedIn) {
                // Show WebView
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            val cookieManager = CookieManager.getInstance()
                            val currentCookies = cookieManager.syncToHttpClient()

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)

                                    // Check if we're on the main account page and navigate to fees
                                    if (url?.contains("userAccount.do") == true &&
                                        !url.contains("type=8") &&
                                        !url.contains("accountTyp=FEES")) {
                                        // Click on the fees link via JavaScript
                                        view?.evaluateJavascript(
                                            """
                                (function() {
                                    var feesLink = document.querySelector('a[onclick*="showAccount(8)"]');
                                    if (feesLink) {
                                        feesLink.click();
                                    }
                                })();
                                """.trimIndent()
                                        ) { }
                                    }
                                }
                            }

                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true

                            // Start with the main account page
                            val accountUrl = "${NetworkConfig.BASE_LOGGED_IN_URL}/webOPACClient/userAccount.do?methodToCall=show&type=1"
                            loadUrl(accountUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show normal UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (userViewModel.isLoggedIn) {
                        // Show account information when logged in
                        Text(
                            text = "Angemeldet als: ${userViewModel.username}",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        userViewModel.accountFees?.let { fees ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Gebühren",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = fees,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                userViewModel.logout()
                                navController.popBackStack()
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Abmelden")
                        }
                    } else {
                        // Show login form when not logged in
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
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                userViewModel.username = username
                                userViewModel.password = password
                                userViewModel.login()
                            },
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Anmelden")
                        }
                    }

                    if (userViewModel.isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BallIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            diameter = 32.dp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    userViewModel.errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleIconButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Icon(
        imageVector = Icons.Filled.Explore,
        contentDescription = "Toggle WebView",
        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    radius = 29.dp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                onClick = { onCheckedChange(!checked) }
            )
    )
}
