package com.fm.beebo.ui.search.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fm.beebo.viewmodels.LibrarySearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetails(viewModel: LibrarySearchViewModel, onBack: () -> Unit) {
    val itemDetails = viewModel.selectedItemDetails
    val uriHandler = LocalUriHandler.current
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = itemDetails?.title?.replace("¬", "") ?: "Katalog",
                        modifier = Modifier.clickable {
                            itemDetails?.url?.let { url ->
                                uriHandler.openUri(url)
                            }
                        }
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
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (itemDetails != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.elevatedCardElevation()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRow("Erscheinungsjahr", itemDetails.year.toString(), Icons.Filled.CalendarToday)
                                if (itemDetails.author.isNotEmpty()) {
                                    DetailRow("Autor", itemDetails.author, Icons.Filled.Person)
                                }
                                DetailRow(
                                    "Verfügbarkeit",
                                    if (itemDetails.isAvailable) "Ausleihbar" else "Nicht verfügbar",
                                    if (itemDetails.isAvailable) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    if (itemDetails.isAvailable) Color.Green else Color.Red
                                )
                                DetailRow("ISBN", itemDetails.isbn, Icons.Filled.Book)
                                DetailRow("Sprache", itemDetails.language, Icons.Filled.Translate)
                                if (!itemDetails.isAvailable && itemDetails.dueDates.isNotEmpty()) {
                                    DetailRow("Entliehen bis", itemDetails.dueDates[0], Icons.Filled.EventBusy)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (itemDetails.availableLibraries.isNotEmpty()) {
                            Section("Verfügbar in", itemDetails.availableLibraries)
                        }
                        if (itemDetails.unavailableLibraries.isNotEmpty()) {
                            Section_unavailable("Nicht verfügbar in", itemDetails.unavailableLibraries)
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

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector, iconColor: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.width(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun Section(title: String, items: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        items.forEach { item ->
            Text(text = "• $item", style = MaterialTheme.typography.bodyMedium)
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun Section_unavailable(title: String, items: List<Pair<String, String>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        items.forEach { item ->
            Text(text = "• $item", style = MaterialTheme.typography.bodyMedium)
        }
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}
