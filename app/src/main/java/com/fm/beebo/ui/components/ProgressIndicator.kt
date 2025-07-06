package com.fm.beebo.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ehsanmsz.mszprogressindicator.progressindicator.BallSpinFadeLoaderProgressIndicator

@Composable
fun BallIndicator (
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    minBallDiameter: Dp = 3.dp,
    maxBallDiameter: Dp = 4.dp,
    diameter: Dp = 18.dp,
){
    BallSpinFadeLoaderProgressIndicator(
        modifier = modifier,
        color = color,
        animationDuration = 800,
        minBallDiameter = minBallDiameter,
        maxBallDiameter = maxBallDiameter,
        diameter = diameter,
        isClockwise = true
    )
}