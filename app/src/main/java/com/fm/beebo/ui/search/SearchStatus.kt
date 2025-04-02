package com.fm.beebo.ui.search

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchStatus(
    isLoading: Boolean,
    statusMessage: String,
    resultCount: Int
) {
    if (statusMessage.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                val icon = when {
                    statusMessage.contains("Error") -> Icons.Default.Warning
                    resultCount > 0 -> Icons.Default.CheckCircle
                    else -> Icons.Default.Info
                }

                val color = when {
                    statusMessage.contains("Error") -> MaterialTheme.colorScheme.error
                    resultCount > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = statusMessage,
                color = if (isLoading) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
