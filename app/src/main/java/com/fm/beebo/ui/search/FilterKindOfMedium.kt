package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fm.beebo.ui.settings.Media
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterKindOfMedium(
    viewModel: SettingsViewModel,
    searchViewModel: LibrarySearchViewModel,
    selectedMediaTypes: List<Media>,
    onFilterExpandedChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Medienart",
            fontWeight = FontWeight(900),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        val allMediaTypes = Media.entries
            .distinct()
            .filter { !listOf(Media.Alles, Media.Einzelband).contains(it) }

        Box(
            modifier = Modifier.widthIn(
                min = 100.dp,
                max = 280.dp
            )
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allMediaTypes.forEach { mediaType ->
                    FilterChip(
                        selected = selectedMediaTypes.contains(mediaType),
                        onClick = {
                            viewModel.toggleMediaType(mediaType)
                        },
                        label = {
                            val hasHits = searchViewModel.getCountOfMedium(mediaType)
                            var hasHitsString = ""
                            if (hasHits > 0) {
                                hasHitsString = " ($hasHits)"
                            } else {
                                hasHitsString = ""
                            }
                            Text(
                                text = mediaType.getChipString() + if (searchViewModel.results.isEmpty()) "" else hasHitsString
                            )
                        },
                    )
                }
            }
        }
        Button(
            onClick = {
                onFilterExpandedChange(false)
                onSearch()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Anwenden")
        }
    }
}
