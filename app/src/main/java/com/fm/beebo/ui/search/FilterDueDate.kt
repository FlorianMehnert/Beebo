package com.fm.beebo.ui.search

import androidx.compose.material3.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fm.beebo.viewmodels.SettingsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDueDate(
    viewModel: SettingsViewModel,
    dueDateFilter: LocalDate?,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit,
    onFilterExpandedChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Verfügbar bis",
            fontWeight = FontWeight(900),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        val formattedDate = dueDateFilter?.let {
            "${it.dayOfMonth}.${it.monthValue}.${it.year}"
        } ?: "Kein Datum ausgewählt"

        OutlinedButton(
            onClick = { onShowDatePickerChange(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(formattedDate)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Datum auswählen"
                )
            }
        }

        if (dueDateFilter != null) {
            TextButton(
                onClick = { viewModel.setDueDateFilter(null) }
            ) {
                Text("Datum zurücksetzen")
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateFilter?.toEpochDay()
                ?.times(24 * 60 * 60 * 1000)
        )
        DatePickerDialog(
            onDismissRequest = { onShowDatePickerChange(false) },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate =
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            viewModel.setDueDateFilter(localDate)
                        }
                        onShowDatePickerChange(false)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowDatePickerChange(false) }
                ) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                headline = {
                    Text(
                        text = "Datum auswählen",
                        Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}