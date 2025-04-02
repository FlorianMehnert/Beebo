package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SearchStatus(
    isLoading: Boolean,
    progress: Float, // Progress in range [0,1] or -1 for indeterminate
    resultCount: Int,
    totalResults: Int
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading) {
            LinearProgressIndicator(
            progress = { if (progress >= 0f) progress else 0f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.secondary,
            strokeCap = StrokeCap.Square,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val icon = when {
                    resultCount > 0 -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                }

                val color = when {
                    resultCount > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (totalResults > 0)
                        "$resultCount Treffer von ungefähr $totalResults"
                    else
                        "Keine Treffer gefunden",
                    color = color
                )
            }
        }
    }
}

