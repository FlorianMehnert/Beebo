package com.fm.beebo.ui.search

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.fm.beebo.viewmodels.LibrarySearchViewModel
import com.fm.beebo.viewmodels.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    viewModel: SettingsViewModel,
    searchViewModel: LibrarySearchViewModel,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(isHolding) {
        if (isHolding) {
            progress = 0f
            val duration = 800L
            val step = 16L
            val steps = duration / step
            repeat(steps.toInt()) {
                if (!isHolding) return@repeat
                delay(step)
                progress += 1f / steps
                if (progress >= 0.99f) {
                    viewModel.resetFilters()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    showToast(context, "Suchfilter wurden zurückgesetzt")
                    isHolding = false
                }
            }
        } else {
            progress = 0f
        }
    }

    val gradientColor1 = MaterialTheme.colorScheme.primary
    val cardShape = RoundedCornerShape(8)

    Surface(
        modifier = Modifier.clip(cardShape),
        shape = cardShape,
    ) {
        Surface(
            modifier = Modifier
                .padding(2.dp)
                .drawWithContent {
                    if (isHolding) {
                        val scale = 1f + (sin(progress * 8 * PI.toFloat()) * 0.03f)
                        scale(scale) {
                            rotate(degrees = progress * 360f) {
                                drawCircle(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            gradientColor1
                                        )
                                    ),
                                    radius = size.width,
                                    blendMode = BlendMode.Difference,
                                )
                            }
                        }
                    }
                    drawContent()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = cardShape
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = cardShape,
                modifier = Modifier
                    .drawBehind {
                        Brush.horizontalGradient(
                            colors = listOf(
                                gradientColor1.copy(alpha = progress),
                                gradientColor1.copy(alpha = 0f)
                            ),
                            startX = 0f,
                            endX = size.width
                        )
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                        .height(60.dp)
                ) {
                    SearchBarTextField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = onSearch,
                        searchViewModel = searchViewModel
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SearchFilter(
                        viewModel = viewModel,
                        searchViewModel = searchViewModel,
                        filterExpanded = filterExpanded,
                        onFilterExpandedChange = { filterExpanded = it },
                        isHolding = isHolding,
                        onHoldingChanged = { isHolding = it },
                        progress = progress,
                        onSearch = onSearch
                    )
                }
            }
        }
    }
}


fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, duration).show()
        }
    }

}
