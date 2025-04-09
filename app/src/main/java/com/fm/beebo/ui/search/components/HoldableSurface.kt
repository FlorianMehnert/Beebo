package com.fm.beebo.ui.search.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun HoldableFilterButton(
    onHoldComplete: () -> Unit,
    onClick: () -> Unit
) {
    var isHolding by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "progress"
    )

    LaunchedEffect(isHolding) {
        if (isHolding) {
            progress = 0f
            val duration = 1000L
            val step = 100L
            val steps = duration / step
            repeat(steps.toInt()) {
                delay(step)
                progress += 1f / steps
            }
            onHoldComplete()
            isHolding = false
        } else {
            progress = 0f
        }
    }

    Surface(
        shape = RoundedCornerShape(8),
        modifier = Modifier
            .height(50.dp)
            .offset(y = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        delay(200L)
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    },
                    onTap = { onClick() }
                )
            },
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
                .width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isHolding,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) +
                            scaleIn(initialScale = 0.8f, animationSpec = tween(220)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(120)) +
                                    scaleOut(targetScale = 0.8f, animationSpec = tween(120))
                        )
                },
                label = "IconTransition"
            ) { holding ->
                if (holding) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Filter",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeCap = StrokeCap.Round,
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}