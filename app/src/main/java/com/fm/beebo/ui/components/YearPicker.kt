package com.fm.beebo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun YearPickerDialog(
    minYear: Int,
    maxYear: Int,
    initialSelection: Int,
    onYearSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialSelection) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Jahr auswählen",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Year grid - 3 columns of years
                MultiColumnYearGrid(
                    minYear = minYear,
                    maxYear = maxYear,
                    selectedYear = selectedYear,
                    onYearClick = { selectedYear = it }
                )

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onYearSelected(selectedYear)
                            onDismissRequest()
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun MultiColumnYearGrid(
    minYear: Int,
    maxYear: Int,
    selectedYear: Int,
    onYearClick: (Int) -> Unit
) {
    val numColumns = 3

    val years = (minYear..maxYear).toList().reversed()
    val rows = years.chunked(numColumns)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        itemsIndexed(rows) { _, rowYears ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowYears.forEach { year ->
                    YearItem(
                        year = year,
                        selected = year == selectedYear,
                        onClick = { onYearClick(year) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty spaces in last row if needed
                if (rowYears.size < numColumns) {
                    repeat(numColumns - rowYears.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun YearItem(
    year: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .padding(4.dp)
            .height(48.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = year.toString(),
                color = textColor
            )
        }
    }
}
