package com.fm.beebo.ui.search.details

import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.fm.beebo.network.getCookies
import com.fm.beebo.ui.CustomWebViewClient
import com.fm.beebo.ui.search.addReminderToCalendar
import com.fm.beebo.viewmodels.LibrarySearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    viewModel: LibrarySearchViewModel,
    onBack: () -> Unit
) {
    val itemDetails = viewModel.selectedItemDetails
    var isWebViewVisible by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemDetails?.title?.replace("¬", "")?.replace("\n", "") ?: "Katalog",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable { isExpanded = !isExpanded },
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                        overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    ToggleIconButton(
                        checked = isWebViewVisible,
                        onCheckedChange = { isWebViewVisible = it },


                        )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            if (isWebViewVisible) {
                AndroidView(factory = { context ->
                    WebView(context).apply {
                        webViewClient =
                            CustomWebViewClient(CookieManager.getInstance().getCookies())
                        settings.javaScriptEnabled = true
                        loadUrl(itemDetails?.url ?: "")
                    }
                })
            } else {
                if (itemDetails != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (itemDetails.kindOfMedium.isNotEmpty()) {
                                    DetailCard(
                                        "Art des Mediums",
                                        itemDetails.kindOfMedium,
                                        Icons.Filled.Book
                                    )
                                }
                                DetailCard(
                                    "Erscheinungsjahr",
                                    itemDetails.year.toString(),
                                    Icons.Filled.CalendarToday
                                )
                                if (itemDetails.author.isNotEmpty()) {
                                    val cardTitle =
                                        if (itemDetails.kindOfMedium == "DVD") "Mitwirkende" else "Autor/in"
                                    DetailCard(
                                        cardTitle,
                                        itemDetails.author.replace(";", ""),
                                        Icons.Filled.Person
                                    )
                                }

                                if (itemDetails.isbn.isNotEmpty()) {
                                    DetailCard("ISBN", itemDetails.isbn, Icons.Filled.Book)
                                }
                                if (itemDetails.language.isNotEmpty()) {
                                    DetailCard(
                                        "Sprache",
                                        itemDetails.language,
                                        Icons.Filled.Translate
                                    )
                                }
                                if (!itemDetails.isAvailable && itemDetails.dueDates.isNotEmpty()) {
                                    DetailCard(
                                        "Entliehen bis",
                                        itemDetails.dueDates[0],
                                        Icons.Filled.EventBusy,
                                        hasBookmark = true,
                                    )
                                }
                                DetailCard(
                                    "Verfügbarkeit",
                                    if (itemDetails.isAvailable) "Ausleihbar" else "Nicht verfügbar",
                                    if (itemDetails.isAvailable) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    if (itemDetails.isAvailable) Color.Green else Color.Red,
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            if (itemDetails.availableLibraries.isNotEmpty()) {
                                Section("Verfügbar in", itemDetails.availableLibraries)
                            }
                            if (itemDetails.unavailableLibraries.isNotEmpty()) {
                                Section_unavailable(
                                    "Nicht verfügbar in",
                                    itemDetails.unavailableLibraries
                                )
                            }
                            if (itemDetails.orderableLibraries.isNotEmpty()) {
                                Section("Bestellbar in", itemDetails.orderableLibraries)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Keine Details verfügbar", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: String,
    icon: ImageVector,
    iconColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    hasBookmark: Boolean = false,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }


    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                clipboardManager.setText(AnnotatedString(content))
                Toast.makeText(context, "Inhalt zur Zwischenablage kopiert!", Toast.LENGTH_SHORT)
                    .show()
            }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    tint = iconColor
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (hasBookmark) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Bookmark",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = content,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }

    if (showDialog) {
        val dateRegex = Regex("\\d{2}\\.\\d{2}\\.\\d{4}")
        val extractedDate = dateRegex.find(content)?.value ?: content
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Reminder") },
            text = { Text("Erinnerung am Datum ${extractedDate} einfügen?") },
            confirmButton = {
                TextButton(onClick = {
                    addReminderToCalendar(context, title, content)
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}


@Composable
fun Section(title: String, items: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        items.forEach { item ->
            Text(
                text = item.replace("(", "").replace(")", ""),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun Section_unavailable(title: String, items: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        items.forEach { item ->
            Text(
                text = item.first.replace("(", "").replace(")", ""),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun ToggleIconButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Icon(
        imageVector = Icons.Filled.Explore,
        contentDescription = "Toggle Icon",
        tint = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .size(28.dp)
            .clickable {
                onCheckedChange(!checked)
            }

    )
}