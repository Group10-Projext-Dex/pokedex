package com.example.project_dex.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@Composable
fun LocationDetailScreen(
    location: ApiResource,
    locationViewModel: LocationViewModel,
    onBackPressed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = locationViewModel.detailUiState) {
            is LocationDetailUiState.Loading -> LoadingView()
            is LocationDetailUiState.Error -> ErrorView()
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

@Composable
private fun LoadingView() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView() {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Failed to load location details.")
    }
}

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

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Locations"
                )
            }
            Text(
                text = "Location Details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LocationInfoCard(
                    englishName = englishName,
                    locationData = locationData,
                    pokemonCount = allPokemon.size
                )
            }

            if (allPokemon.isNotEmpty()) {
                item {
                    Text(
                        text = "Pokémon Found Here",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(allPokemon.size) { index ->
                    val encounter = allPokemon[index]
                    val pokemonDetails = locationViewModel.pokemonDetailsUiState[encounter.pokemon.name]

                    PokemonEncounterCard(
                        encounter = encounter,
                        pokemonDetails = pokemonDetails
                    )
                }
            } else if (locationData.areas.isNotEmpty()) {
                item {
                    LoadingPokemonCard()
                }
            }
        }
    }
}

@Composable
private fun LocationInfoCard(
    englishName: String,
    locationData: Location,
    pokemonCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = englishName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            locationData.region?.let { region ->
                InfoRow(
                    label = "Region:",
                    value = region.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                )
            }

            if (locationData.gameIndices.isNotEmpty()) {
                InfoRow(
                    label = "Games:",
                    value = "${locationData.gameIndices.size} game(s)"
                )
            }

            if (pokemonCount > 0) {
                InfoRow(
                    label = "Pokémon:",
                    value = "$pokemonCount different species",
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
private fun PokemonEncounterCard(
    encounter: PokemonEncounter,
    pokemonDetails: Pokemon?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PokemonImage(pokemonDetails = pokemonDetails)
            Spacer(modifier = Modifier.width(16.dp))
            PokemonDetails(
                encounter = encounter,
                pokemonDetails = pokemonDetails
            )
        }
    }
}

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

@Composable
private fun PokemonImage(pokemonDetails: Pokemon?) {
    Surface(
        modifier = Modifier.size(90.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun PokemonDetails(
    encounter: PokemonEncounter,
    pokemonDetails: Pokemon?
) {
    Column {
        Text(
            text = encounter.pokemon.name
                .replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                },
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (pokemonDetails != null) {
            Row(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pokemonDetails.types.forEach { typeInfo ->
                    TypeBadge(typeName = typeInfo.type.name)
                }
            }

            Text(
                text = "${pokemonDetails.getHeightInMeters()}m • ${String.format("%.1f", pokemonDetails.getWeightInKg())}kg",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (encounter.versionDetails.isNotEmpty()) {
            val firstVersion = encounter.versionDetails.first()
            if (firstVersion.encounterDetails.isNotEmpty()) {
                val detail = firstVersion.encounterDetails.first()
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Level ${detail.minLevel}-${detail.maxLevel} • ${detail.chance}% chance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TypeBadge(typeName: String) {
    Surface(
        color = getTypeColor(typeName),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = typeName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            },
            style = MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

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