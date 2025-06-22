package com.fm.beebo.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fm.beebo.ui.settings.FilterOptions
import com.fm.beebo.viewmodels.SettingsViewModel

@Composable
fun FilterOptionsSection(
    viewModel: SettingsViewModel,
    selectedFilterOptions: Pair<FilterOptions, Boolean>
) {
    Text(
        text = "Filterart",
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(
            start = 32.dp,
            end = 32.dp,
            bottom = 16.dp
        )
    )

    FilterOptions.entries.forEach { option ->
        DropdownMenuItem(
            text = { Text(text = option.toString()) },
            onClick = {
                viewModel.setSortBy(option, true)
                // Do not close the dropdown menu here
            },
            modifier = Modifier.conditional(selectedFilterOptions.first == option) {
                val background = background(
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    )
                )
                background
            }
        )
    }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
    )
}


@Composable
fun Modifier.conditional(
    isConditionMet: Boolean,
    modifier: @Composable Modifier.() -> Modifier
): Modifier {
    return if (isConditionMet) {
        this.then(modifier())
    } else {
        this
    }
}