package com.example.project_dex.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.project_dex.models.*
import com.example.project_dex.network.ApiResource
import com.example.project_dex.viewmodels.LocationDetailUiState
import com.example.project_dex.viewmodels.LocationViewModel
import java.util.Locale

/**
 * Detail screen for a specific location
 */
@Composable
fun LocationDetailScreen(
    location: ApiResource,
    locationViewModel: LocationViewModel,
    onBackPressed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = locationViewModel.detailUiState) {
            is LocationDetailUiState.Loading -> {
                LoadingView()
            }

            is LocationDetailUiState.Error -> {
                ErrorView()
            }

            is LocationDetailUiState.Success -> {
                LocationDetailContent(
                    locationData = state.location,
                    locationViewModel = locationViewModel,
                    onBackPressed = onBackPressed
                )
            }
        }
    }
}

/**
 * Loading view
 */
@Composable
private fun LoadingView() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error view
 */
@Composable
private fun ErrorView() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Failed to load location details.")
    }
}

/**
 * Main content for location details
 */
@Composable
private fun LocationDetailContent(
    locationData: Location,
    locationViewModel: LocationViewModel,
    onBackPressed: () -> Unit
) {
    LaunchedEffect(locationData.areas) {
        if (locationData.areas.isNotEmpty()) {
            locationViewModel.fetchAllAreaEncounters(locationData.areas)
        }
    }
    
    val englishName = locationData.names.find { 
        it.language.name == "en" 
    }?.name ?: locationData.name
        .replace("-", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        }

    val allPokemon = locationViewModel.encountersUiState.values
        .flatten()
        .distinctBy { it.pokemon.name }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Back button
        item {
            Button(
                onClick = onBackPressed,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("← Back to Locations")
            }
        }

        // Location info card
        item {
            LocationInfoCard(
                englishName = englishName,
                locationData = locationData,
                pokemonCount = allPokemon.size
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pokemon encounters
        if (allPokemon.isNotEmpty()) {
            item {
                PokemonEncountersCard(
                    allPokemon = allPokemon,
                    locationViewModel = locationViewModel
                )
            }
        } else if (locationData.areas.isNotEmpty()) {
            item {
                LoadingPokemonCard()
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Location information card
 */
@Composable
private fun LocationInfoCard(
    englishName: String,
    locationData: Location,
    pokemonCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = englishName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            locationData.region?.let { region ->
                Text(
                    text = "Region: ${
                        region.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }
                    }",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (locationData.gameIndices.isNotEmpty()) {
                Text(
                    text = "Appears in ${locationData.gameIndices.size} game(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (pokemonCount > 0) {
                Text(
                    text = "$pokemonCount different Pokémon found here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Pokemon encounters card
 */
@Composable
private fun PokemonEncountersCard(
    allPokemon: List<PokemonEncounter>,
    locationViewModel: LocationViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pokémon Found Here",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            allPokemon.forEachIndexed { index, encounter ->
                val pokemonDetails = locationViewModel.pokemonDetailsUiState[encounter.pokemon.name]
                
                PokemonEncounterItem(
                    encounter = encounter,
                    pokemonDetails = pokemonDetails
                )
                
                if (index < allPokemon.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

/**
 * Loading Pokemon card
 */
@Composable
private fun LoadingPokemonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading Pokémon encounters...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Individual Pokemon encounter item with image and details
 */
@Composable
private fun PokemonEncounterItem(
    encounter: PokemonEncounter,
    pokemonDetails: Pokemon?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pokemon image
        PokemonImage(pokemonDetails = pokemonDetails)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Pokemon details
        PokemonDetails(
            encounter = encounter,
            pokemonDetails = pokemonDetails
        )
    }
}

/**
 * Pokemon image with loading/error states
 */
@Composable
private fun PokemonImage(pokemonDetails: Pokemon?) {
    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        if (pokemonDetails != null) {
            val imageUrl = pokemonDetails.getBestSpriteUrl()
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pokemonDetails.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            // Loading placeholder
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Pokemon details (name, types, height, weight, encounter info)
 */
@Composable
private fun PokemonDetails(
    encounter: PokemonEncounter,
    pokemonDetails: Pokemon?
) {
    Column {
        // Pokemon name
        Text(
            text = encounter.pokemon.name
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                },
            style = MaterialTheme.typography.bodyLarge
        )
        
        // Types, Height, Weight
        if (pokemonDetails != null) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Types
                pokemonDetails.types.forEach { typeInfo ->
                    TypeBadge(typeName = typeInfo.type.name)
                }
            }
            
            // Height and Weight
            Text(
                text = "${pokemonDetails.getHeightInMeters()}m • ${String.format("%.1f", pokemonDetails.getWeightInKg())}kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Encounter details
        if (encounter.versionDetails.isNotEmpty()) {
            val firstVersion = encounter.versionDetails.first()
            if (firstVersion.encounterDetails.isNotEmpty()) {
                val detail = firstVersion.encounterDetails.first()
                Text(
                    text = "Level ${detail.minLevel}-${detail.maxLevel} • ${detail.chance}% chance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Type badge with color
 */
@Composable
private fun TypeBadge(typeName: String) {
    Surface(
        color = getTypeColor(typeName),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = typeName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Get color for Pokemon type
 */
@Composable
private fun getTypeColor(typeName: String): androidx.compose.ui.graphics.Color {
    return when (typeName.lowercase()) {
        "normal" -> androidx.compose.ui.graphics.Color(0xFFA8A878)
        "fire" -> androidx.compose.ui.graphics.Color(0xFFF08030)
        "water" -> androidx.compose.ui.graphics.Color(0xFF6890F0)
        "electric" -> androidx.compose.ui.graphics.Color(0xFFF8D030)
        "grass" -> androidx.compose.ui.graphics.Color(0xFF78C850)
        "ice" -> androidx.compose.ui.graphics.Color(0xFF98D8D8)
        "fighting" -> androidx.compose.ui.graphics.Color(0xFFC03028)
        "poison" -> androidx.compose.ui.graphics.Color(0xFFA040A0)
        "ground" -> androidx.compose.ui.graphics.Color(0xFFE0C068)
        "flying" -> androidx.compose.ui.graphics.Color(0xFFA890F0)
        "psychic" -> androidx.compose.ui.graphics.Color(0xFFF85888)
        "bug" -> androidx.compose.ui.graphics.Color(0xFFA8B820)
        "rock" -> androidx.compose.ui.graphics.Color(0xFFB8A038)
        "ghost" -> androidx.compose.ui.graphics.Color(0xFF705898)
        "dragon" -> androidx.compose.ui.graphics.Color(0xFF7038F8)
        "dark" -> androidx.compose.ui.graphics.Color(0xFF705848)
        "steel" -> androidx.compose.ui.graphics.Color(0xFFB8B8D0)
        "fairy" -> androidx.compose.ui.graphics.Color(0xFFEE99AC)
        else -> MaterialTheme.colorScheme.primary
    }
}