package com.fm.beebo.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryResultListItem(text: String, isAvailable: Boolean, onClick: () -> Unit) {
    val parts = text.split(" ", limit = 4)
    val year = if (parts.isNotEmpty()) parts.getOrNull(0) ?: "" else ""
    val medium = if (parts.size > 1) parts.getOrNull(1) ?: "" else ""
    var title = if (parts.size > 2) parts.drop(2).joinToString(" ") else ""

    // Split title and availability info
    val availabilityText =
        if (title.contains(" ausleihbar")) " ausleihbar" else " nicht_ausleihbar "
    title = title.replace(availabilityText, "").replace("¬", "")

    // Extract due date if present
    val dueDate = if (!isAvailable && title.contains(Regex("\\d{2}\\.\\d{2}\\.\\d{4}"))) {
        val regex = Regex("(\\d{2}\\.\\d{2}\\.\\d{4})")
        val match = regex.find(title)
        match?.value ?: ""
    } else ""

    // Clean up title if it contains due date
    if (dueDate.isNotEmpty()) {
        title = title.replace(dueDate, "").trim()
    }

    // Determine what to display in the medium icon
    val displayMedium = medium.ifEmpty { "?" }

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
            // Medium icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isAvailable) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Only show the year if it's not empty
                    if (year.isNotEmpty()) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No year found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (dueDate.isNotEmpty()) {
                        // Add spacer only if year is shown
                        if (year.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = buildAnnotatedString {
                                append("Entliehen bis: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(dueDate)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}