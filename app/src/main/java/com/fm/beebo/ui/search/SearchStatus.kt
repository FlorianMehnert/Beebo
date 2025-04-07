package com.fm.beebo.ui.search

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun SearchStatus(
    isLoading: Boolean,
    progress: Float, // Progress in range [0,1] or -1 for indeterminate
    resultCount: Int,
    totalResults: Int,
    message: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (progress >= 0f) progress else 0f, // Avoid animating indeterminate state
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "Progress Animation"
    )

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulsating Alpha"
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Foreground Progress (Completed)
                LinearProgressIndicator(
                    progress = { animatedProgress }, // Use animated progress
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress > 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    trackColor = if (progress > 0f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                    strokeCap = StrokeCap.Round,
                )

                // Pulsating Animation Overlay for Indeterminate State
                if (progress < 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = pulseAlpha))
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
            val icon = if (isLoading) Icons.Default.Refresh else Icons.Default.CheckCircle
            val color = if (resultCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

            if (progress > 0f) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }else if(progress == 0f && isLoading){
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (progress > 0f && isLoading) "$resultCount Treffer von ungefähr $totalResults" else message,
                color = color
            )
        }
    }
}



