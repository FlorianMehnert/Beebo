package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBranchOffice(
    selectedBranchOffice: BranchOffice?,
    onBranchSelected: (BranchOffice) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zweigstelle",
            fontWeight = FontWeight(900),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier.widthIn(min = 100.dp, max = 280.dp)
        ) {
            FlowRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BranchOffice.entries.forEach { branch ->
                    FilterChip(
                        selected = selectedBranchOffice == branch,
                        onClick = { onBranchSelected(branch) },
                        label = { Text(text = branch.displayName) }
                    )
                }
            }
        }
    }
}
