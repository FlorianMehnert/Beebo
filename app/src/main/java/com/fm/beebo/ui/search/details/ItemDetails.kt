package com.fm.beebo.ui.search.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fm.beebo.viewmodels.LibrarySearchViewModel

@Composable
fun ItemDetails(viewModel: LibrarySearchViewModel, onBack: () -> Unit) {
    val itemDetails = viewModel.selectedItemDetails

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (itemDetails != null) {
                Text(
                    text = itemDetails.title.replace("¬", ""), style = MaterialTheme.typography.titleLarge
                )
            } else {
                Text(
                    text = "Katalog", style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (itemDetails != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Year: ${itemDetails.year}", style = MaterialTheme.typography.bodyMedium
            )
            if (itemDetails.author.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Autor: ${itemDetails.author}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verfügbarkeit: ${if (itemDetails.isAvailable) "Ausleihbar" else "Nicht verfügbar"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ISBN: ${itemDetails.isbn}", style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sprache: ${itemDetails.language}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (!itemDetails.isAvailable && itemDetails.dueDates.isNotEmpty()) {
                Text(
                    text = "Due Date: ${itemDetails.dueDates[0]}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                text = "Keine Details verfügbar"
            )
            CircularProgressIndicator()
        }
    }
}