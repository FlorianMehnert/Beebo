package com.fm.beebo.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExperimentalFeatureToggle(
    enabled: Boolean,
    text: String,
    featureTitle: String,
    extendedText: String,
    action: () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .width(250.dp)
                .clickable { showTooltip = true }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = enabled,
            onCheckedChange = {
                action()
            }
        )
    }
    if (showTooltip) {
        AlertDialog(
            onDismissRequest = { showTooltip = false },
            title = { Text(featureTitle) },
            text = {
                Text(extendedText)
            },
            confirmButton = {
                TextButton(onClick = { showTooltip = false }) {
                    Text("Schließen")
                }
            }
        )
    }
}