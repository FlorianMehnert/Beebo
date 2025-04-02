package com.fm.beebo.ui.search

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SearchStatus(
    isLoading: Boolean,
    progress: Float, // Progress in range [0,1] or -1 for indeterminate
    resultCount: Int,
    totalResults: Int
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha = infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseIn),
            repeatMode = RepeatMode.Reverse
        )
    )


    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading){
            Box(modifier = Modifier.fillMaxWidth()) {
                // Foreground Progress (Completed)
                LinearProgressIndicator(
                    progress = { if (progress >= 0f) progress else -1f },
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha.value),
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = pulseAlpha.value),
                    strokeCap = StrokeCap.Round,
                )

                // Pulsating Animation Overlay
                if (progress < 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = pulseAlpha.value))
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val icon = when {
                resultCount > 0 -> Icons.Default.CheckCircle
                else -> Icons.Default.Info
            }

            val color = when {
                resultCount > 0 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }
            if (totalResults > 0) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }


            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (totalResults > 0)
                    "$resultCount Treffer von ungefähr $totalResults"
                else
                    "",
                color = color
            )
        }
    }
}


