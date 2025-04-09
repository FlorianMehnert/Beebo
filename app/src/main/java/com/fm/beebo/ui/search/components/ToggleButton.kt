package com.fm.beebo.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fm.beebo.viewmodels.SettingsViewModel


@Composable
fun ToggleButton(
    text: String = "Toggle",
    settingsViewModel: SettingsViewModel,
) {
    var isToggled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
            .clickable {
                isToggled = !isToggled
                settingsViewModel.setFilterByTimeSpan(isToggled)
            }, contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isToggled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(6.dp),
        )
    }
}