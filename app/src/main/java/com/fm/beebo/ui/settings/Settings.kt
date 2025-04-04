import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.search.ExperimentalFeatureToggle
import com.fm.beebo.ui.search.details.ToggleIconButton
import com.fm.beebo.ui.settings.MaxPagesSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    settingsDataStore: SettingsDataStore,
    onBackPress: () -> Unit,
    onShowLibraries: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val enableDefaultSearchTerm by settingsDataStore.enableDefaultSearchTermFlow.collectAsState(
        initial = false
    )
    val defaultSearchTerm by settingsDataStore.defaultSearchTermFlow.collectAsState(initial = "")
    val bulkFetch by settingsDataStore.bulkFetchEnabledFlow.collectAsState(initial = false)

    var text by remember(defaultSearchTerm) {
        mutableStateOf(defaultSearchTerm)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { newText -> text = newText },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Standard Suchbegriff") },
                singleLine = true,
                enabled = enableDefaultSearchTerm,
                leadingIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            settingsDataStore.enableDefaultSearchTerm(!enableDefaultSearchTerm)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Toggle Search Mode",
                            tint = if (enableDefaultSearchTerm) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { text = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Löschen"
                            )
                        }
                    }
                }
            )
            Spacer(Modifier.height(8.dp))


            LaunchedEffect(text) {
                delay(500)
                settingsDataStore.setDefaultSearchTerm(text)
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            ) {
                MaxPagesSlider(
                    settingsDataStore = settingsDataStore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.Engineering,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Erweiterte Einstellungen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.Start) {
                        ExperimentalFeatureToggle(
                            settingsDataStore.bulkFetchEnabledFlow.collectAsState(false).value,
                            "Starte Hintergrundprozesse um Details zu jedem Suchergebnis zu bekommen",
                            "Automatische Details",
                            "Startet für jedes Medium in der Trefferliste einen Hintergrundprozess um Details zu diesem Suchergebnis zu bekommen. Das wird bei größeren Suchanfragen wesentlich mehr Daten verbrauchen im Vergleich dazu, wenn diese Einstellung deaktiviert ist. 100 Suchergebnisse verbrauchen ungefähr 25MB.",
                            {
                                coroutineScope.launch {
                                    settingsDataStore.setBulkFetch(!bulkFetch)
                                }
                            },)
                    }
                }

            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceContainer,
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Über die App",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Version: 1.5",
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "Autor: Merkosh",
                            fontSize = 14.sp,
                        )
                        Text(
                            text = "Quellcode",
                            fontSize = 14.sp,
                            style = TextStyle(
                                textDecoration = TextDecoration.Underline
                            ), modifier = Modifier.clickable {
                                onShowLibraries()
                            }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Open Source Lizenzen",
                            fontSize = 14.sp,
                            style = TextStyle(
                                textDecoration = TextDecoration.Underline
                            ), modifier = Modifier.clickable {
                                onShowLibraries()
                            }
                        )
                    }
                }

            }

        }

    }
}

