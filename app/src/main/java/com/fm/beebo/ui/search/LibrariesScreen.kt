package com.fm.beebo.ui.search

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(onBackPress: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verwendete Bibliotheken") },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LibrariesContainer(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

