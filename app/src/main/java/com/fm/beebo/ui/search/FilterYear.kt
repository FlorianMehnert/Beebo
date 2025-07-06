package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.fm.beebo.ui.components.ToggleButton
import com.fm.beebo.ui.components.YearPickerDialog
import com.fm.beebo.viewmodels.SettingsViewModel
import java.util.Calendar

@Composable
fun FilterYear(
    viewModel: SettingsViewModel,
    minYear: Int,
    maxYear: Int,
    filterByTimeSpan: Boolean,
    onFilterExpandedChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    var showMinYearDialog by remember { mutableStateOf(false) }
    var showMaxYearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        ToggleButton(
            text = "Mit Zeitspanne filtern",
            settingsViewModel = viewModel
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { showMinYearDialog = true },
                enabled = filterByTimeSpan
            ) {
                Text(text = "Von: $minYear")
            }
            TextButton(
                onClick = { showMaxYearDialog = true },
                enabled = filterByTimeSpan
            ) {
                Text(text = "Bis: $maxYear")
            }
        }
        if (maxYear > 2025) {
            Text(
                "Endjahr darf nicht größer als das aktuelle Jahr sein.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
        Button(
            onClick = {
                onFilterExpandedChange(false)
                onSearch()
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Anwenden")
        }
    }

    val calendar: Calendar = Calendar.getInstance()
    val year: Int = calendar.get(Calendar.YEAR)

    if (showMinYearDialog) {
        YearPickerDialog(
            minYear = 1900,
            maxYear = year,
            initialSelection = minYear,
            onYearSelected = { selectedYear ->
                if (selectedYear < maxYear) {
                    viewModel.setMinYear(selectedYear)
                }
            },
            onDismissRequest = { showMinYearDialog = false }
        )
    }
    if (showMaxYearDialog) {
        YearPickerDialog(
            minYear = minYear,
            maxYear = year,
            initialSelection = maxYear,
            onYearSelected = { selectedYear ->
                if (selectedYear > minYear) {
                    viewModel.setMaxYear(selectedYear)
                }
            },
            onDismissRequest = { showMaxYearDialog = false }
        )
    }
}
