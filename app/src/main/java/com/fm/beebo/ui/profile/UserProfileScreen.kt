package com.fm.beebo.ui.profile

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.fm.beebo.models.LibraryMedia
import com.fm.beebo.network.NetworkConfig
import com.fm.beebo.network.syncToHttpClient
import com.fm.beebo.ui.components.AppBottomNavigation
import com.fm.beebo.ui.components.BallIndicator
import com.fm.beebo.viewmodels.UserViewModel

import androidx.compose.material3.pulltorefresh.*


enum class ProfileTab { ACCOUNT, WISHLIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ProfileTab.ACCOUNT) }
    var isWebViewVisible by remember { mutableStateOf(false) }
    val wishSet by userViewModel.wishList.collectAsState(initial = emptySet())

    // Initialize UserViewModel
    LaunchedEffect(Unit) {
        userViewModel.initialize()
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchWishlist()
    }

    Scaffold(
        topBar = {
            ProfileTopBar(
                navController = navController,
                userViewModel = userViewModel,
                isWebViewVisible = isWebViewVisible,
                onWebViewToggle = { isWebViewVisible = it }
            )
        },
        bottomBar = {
            AppBottomNavigation(
                navController = navController,
                userViewModel = userViewModel,
                currentRoute = "profile"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isWebViewVisible && userViewModel.isLoggedIn) {
                ProfileWebView()
            } else {
                ProfileContent(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    wishSet = wishSet,
                    userViewModel = userViewModel,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    navController: NavController,
    userViewModel: UserViewModel,
    isWebViewVisible: Boolean,
    onWebViewToggle: (Boolean) -> Unit
) {
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
                    onCheckedChange = onWebViewToggle
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    )
}

@Composable
private fun ProfileContent(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    wishSet: Set<String>,
    userViewModel: UserViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ProfileTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            wishCount = wishSet.size,
            isLoggedIn = userViewModel.isLoggedIn
        )

        when (selectedTab) {
            ProfileTab.ACCOUNT -> {
                if (userViewModel.isLoggedIn) {
                    AccountContent(userViewModel, navController)
                } else {
                    LoginForm(userViewModel)
                }
            }
            ProfileTab.WISHLIST -> {
                WishlistContent(
                    wishlistIds = wishSet,
                    userViewModel = userViewModel,
                    navController = navController
                )
            }
        }

        LoadingAndErrorStates(userViewModel)
    }
}

@Composable
private fun ProfileTabs(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    wishCount: Int,
    isLoggedIn: Boolean
) {
    TabRow(
        selectedTabIndex = selectedTab.ordinal,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Tab(
            selected = selectedTab == ProfileTab.ACCOUNT,
            onClick = { onTabSelected(ProfileTab.ACCOUNT) },
            text = { Text(if (isLoggedIn) "Konto" else "Anmelden") }
        )
        Tab(
            selected = selectedTab == ProfileTab.WISHLIST,
            onClick = { onTabSelected(ProfileTab.WISHLIST) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Merkliste")
                    if (wishCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Badge { Text(wishCount.toString()) }
                    }
                }
            }
        )
    }
}

@Composable
private fun LoginForm(userViewModel: UserViewModel) {
    var username by remember { mutableStateOf(userViewModel.username) }
    var password by remember { mutableStateOf(userViewModel.password) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Benutzername") },
            modifier = Modifier.semantics { contentType = ContentType.Username },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
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
            modifier = Modifier.semantics { contentType = ContentType.Password },
            keyboardActions = KeyboardActions(
                onDone = { performLogin(userViewModel, username, password) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { performLogin(userViewModel, username, password) },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Anmelden")
        }
    }
}

private fun performLogin(userViewModel: UserViewModel, username: String, password: String) {
    userViewModel.username = username
    userViewModel.password = password
    userViewModel.login()
}

@Composable
private fun AccountContent(
    userViewModel: UserViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Angemeldet als: ${userViewModel.username}",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        userViewModel.accountFees?.let { fees ->
            FeesCard(fees)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = {
                userViewModel.logout()
                navController.popBackStack()
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Abmelden")
        }
    }
}

@Composable
private fun FeesCard(fees: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishlistContent(
    wishlistIds: Set<String>,
    userViewModel: UserViewModel,
    navController: NavController
) {
    val wishlistItems by userViewModel.wishlistItems.collectAsState()
    val refreshing = userViewModel.isWishlistLoading

    val pullState = rememberPullToRefreshState()

    LaunchedEffect(userViewModel.isLoggedIn) {
        if (userViewModel.isLoggedIn) userViewModel.fetchWishlist()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PullToRefreshBox(
            state = pullState,
            isRefreshing = refreshing,
            onRefresh = { userViewModel.fetchWishlist() },
        ) {
            when {
                refreshing -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        BallIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            diameter = 32.dp
                        )
                    }
                }

                wishlistItems.isEmpty() -> {
                    EmptyWishlistState()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = wishlistItems,
                            key = {
                                "${it.title}|${it.year}|${it.kindOfMedium.name}|${it.url.hashCode()}"
                            }
                        ) { item ->
                            WishlistItemCard(
                                item = item,
                                onRemove = { userViewModel.toggleWishlistUsingServerLink(item) },
                                onClick = {
                                    navController.navigate(
                                        "item_details/${java.net.URLEncoder.encode(item.url, "UTF-8")}"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WishlistItemCard(
    item: LibraryMedia,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val displayMedium = item.kindOfMedium.getIcon()
    var showMediumTooltip by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isAvailable)
                            MaterialTheme.colorScheme.inversePrimary
                        else
                            MaterialTheme.colorScheme.surface
                    )
                    .clickable { showMediumTooltip = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                if (item.author.isNotBlank()) {
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.year.isNotBlank()) {
                        Text(
                            text = item.year,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = item.kindOfMedium.getChipString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Aus Merkliste entfernen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showMediumTooltip) {
        AlertDialog(
            onDismissRequest = { showMediumTooltip = false },
            title = { Text("Medienart") },
            text  = { Text("$displayMedium steht für \"${item.kindOfMedium.getChipString()}\"") },
            confirmButton = {
                TextButton(onClick = { showMediumTooltip = false }) { Text("Schließen") }
            }
        )
    }
}




@Composable
private fun EmptyWishlistState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Deine Merkliste ist leer",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Füge Medien über das Herz-Symbol hinzu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingAndErrorStates(userViewModel: UserViewModel) {
    if (userViewModel.isLoading) {
        Spacer(modifier = Modifier.height(16.dp))
        BallIndicator(
            color = MaterialTheme.colorScheme.primary,
            diameter = 32.dp,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        )
    }

    userViewModel.errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun ProfileWebView() {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                val cookieManager = CookieManager.getInstance()
                cookieManager.syncToHttpClient()

                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                val wishlistUrl = "${NetworkConfig.BASE_LOGGED_IN_URL}/webOPACClient/memorizelist.do?methodToCall=show"
                loadUrl(wishlistUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
private fun ToggleIconButton(
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

@Composable
private fun WishlistItemPlaceholder(
    itemId: String,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Media ID: $itemId",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Details werden geladen...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Aus Merkliste entfernen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
