package com.fm.beebo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieSearchScreen()
        }
    }
}

@Composable
fun MovieSearchScreen(viewModel: MovieSearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var mediaType by remember { mutableStateOf("Movie") }
    var pages by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Enter search query") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Button(onClick = { expanded = true }) {
                Text(mediaType)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(onClick = {
                    mediaType = "Movie"
                    expanded = false
                }) {
                    Text("Movie")
                }
                DropdownMenuItem(onClick = {
                    mediaType = "Series"
                    expanded = false
                }) {
                    Text("Series")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = pages,
            onValueChange = { pages = it },
            label = { Text("Number of pages") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.searchMovies(query, mediaType, pages.toIntOrNull() ?: 1) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(viewModel.results) { movie ->
                    Text(movie, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

class MovieSearchViewModel : ViewModel() {
    var results by mutableStateOf(listOf<String>())
    var isLoading by mutableStateOf(false)

    fun searchMovies(query: String, type: String, pages: Int) {
        isLoading = true
        // Simulated search logic
        results = (1..pages).flatMap { page ->
            listOf("Result 1 - Page $page", "Result 2 - Page $page")
        }
        isLoading = false
    }
}
