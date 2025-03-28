package com.fm.beebo.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.fm.beebo.datastore.SettingsDataStore
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaxPagesSlider(
    settingsDataStore: SettingsDataStore,
    modifier: Modifier = Modifier
) {
    val maxPagesSetting by settingsDataStore.maxPagesFlow.collectAsState(initial = 3)
    var sliderPosition by remember(maxPagesSetting) {
        mutableIntStateOf(maxPagesSetting)
    }
    LaunchedEffect(sliderPosition) {
        delay(300)
        settingsDataStore.setMaxPages(sliderPosition)
    }
    Column(modifier = modifier) {
        Slider(
            value = sliderPosition.toFloat(),
            onValueChange = { newValue ->
                sliderPosition = newValue.toInt()
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            valueRange = 1f..10f,
            thumb = {
                Icon(
                    imageVector = Icons.Default.Pages,
                    contentDescription = "Pages Thumb",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { shadowElevation = 8f }
                )
            }
        )

        val results = sliderPosition * 10
        Text(
            text = "Maximale Anzahl an Suchergebnissen: $results",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}