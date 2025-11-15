package com.example.project_dex.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project_dex.network.ApiResource
import com.example.project_dex.viewmodels.LocationUiState
import com.example.project_dex.viewmodels.LocationViewModel
import java.util.Locale

/**
 * Main location screen showing list of all locations
 */
@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel = viewModel(),
    onDetailViewChange: (Boolean) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<ApiResource?>(null) }

    LaunchedEffect(Unit) {
        locationViewModel.fetchLocationList()
    }

    LaunchedEffect(selectedLocation) {
        onDetailViewChange(selectedLocation != null)
    }

    if (selectedLocation != null) {
        LocationDetailScreen(
            location = selectedLocation!!,
            locationViewModel = locationViewModel,
            onBackPressed = {
                selectedLocation = null
                locationViewModel.resetDetailState()
            }
        )
    } else {
        LocationListView(
            modifier = modifier,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            locationViewModel = locationViewModel,
            onLocationClick = { location ->
                selectedLocation = location
                locationViewModel.fetchLocationDetail(location.url)
            }
        )
    }
}

/**
 * Location list view with search
 */
@Composable
private fun LocationListView(
    modifier: Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    locationViewModel: LocationViewModel,
    onLocationClick: (ApiResource) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search for a Location...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when (val state = locationViewModel.uiState) {
            is LocationUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LocationUiState.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load locations.")
                }
            }

            is LocationUiState.Success -> {
                val filteredLocations = state.locations.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(filteredLocations) { location ->
                        LocationListItem(
                            location = location,
                            onClick = { onLocationClick(location) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual list item for a location
 */
@Composable
private fun LocationListItem(
    location: ApiResource,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = location.name
                .replace("-", " ")
                .split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}