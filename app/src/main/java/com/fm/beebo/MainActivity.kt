package com.fm.beebo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fm.beebo.ui.LibrarySearchScreen
import com.fm.beebo.ui.theme.BeeboTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeeboTheme {
                LibrarySearchScreen()
            }
        }
    }
}