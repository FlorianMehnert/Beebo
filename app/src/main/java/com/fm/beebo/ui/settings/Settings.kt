import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.fm.beebo.datastore.SettingsDataStore
import com.fm.beebo.ui.settings.MaxPagesSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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


            LaunchedEffect(text) {
                delay(500)
                settingsDataStore.setDefaultSearchTerm(text)
            }

            MaxPagesSlider(
                settingsDataStore = settingsDataStore,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            IconButton(
                onClick = onShowLibraries,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Einstellungen",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.width(40.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Über verwendete Bibliotheken")
                }
            }
        }
    }
}