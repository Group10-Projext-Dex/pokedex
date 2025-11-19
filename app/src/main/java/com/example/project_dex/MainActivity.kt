package com.example.project_dex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.project_dex.ui.theme.Project_dexTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Headers

// ----------------------------------------------------------------------
// shared data models for list-style endpoints (pokemon, abilities, etc.)
// ----------------------------------------------------------------------

@Serializable
data class ApiResource(
    val name: String,
    val url: String
)

@Serializable
data class ApiResponse(
    val results: List<ApiResource>
)

// ----------------------------------------------------------------------
// location-related models (used for "Locations" flow)
// ----------------------------------------------------------------------

@Serializable
data class PokemonEncounter(
    val pokemon: ApiResource
)

@Serializable
data class LocationArea(
    val pokemon_encounters: List<PokemonEncounter>
)

@Serializable
data class LocationAreaResource(
    val name: String,
    val url: String
)

@Serializable
data class LocationDetails(
    val areas: List<LocationAreaResource>,
    val region: ApiResource?
)

// ----------------------------------------------------------------------
// ability details models (used for the ability detail screen I added)
// ----------------------------------------------------------------------

@Serializable
data class AbilityDetails(
    val name: String,
    val effect_entries: List<EffectEntry>
)

@Serializable
data class EffectEntry(
    val effect: String,
    val short_effect: String,
    val language: LanguageRef
)

@Serializable
data class LanguageRef(
    val name: String
)

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Project_dexTheme {

                // currentScreen = which list/menu we are on ("menu", "pokemon", "ability", etc.)
                var currentScreen by remember { mutableStateOf("menu") }

                // these nullable urls mean "we are looking at a detail screen for X"
                var selectedPokemonUrl by remember { mutableStateOf<String?>(null) }
                var selectedLocationUrl by remember { mutableStateOf<String?>(null) }

                // JUAN: new state for ability details – when this is not null we show AbilityDetailScreen
                var selectedAbilityUrl by remember { mutableStateOf<String?>(null) }

                // simple back behavior – clear the most specific detail first, then fall back to menu
                val navigateBack = {
                    when {
                        selectedPokemonUrl != null -> selectedPokemonUrl = null
                        selectedLocationUrl != null -> selectedLocationUrl = null
                        selectedAbilityUrl != null -> selectedAbilityUrl = null
                        currentScreen != "menu" -> currentScreen = "menu"
                    }
                }

                // top app bar title updates based on what we’re currently showing
                val topBarTitle = when {
                    selectedPokemonUrl != null -> "Pokémon Details"
                    selectedLocationUrl != null -> "Pokémon in Area"
                    selectedAbilityUrl != null -> "Ability Details"
                    currentScreen != "menu" -> currentScreen.replaceFirstChar { it.titlecase() } + " List"
                    else -> "PokéDex"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // only show a top bar when we’re not on the root menu
                        if (
                            currentScreen != "menu" ||
                            selectedPokemonUrl != null ||
                            selectedLocationUrl != null ||
                            selectedAbilityUrl != null
                        ) {
                            TopAppBar(
                                title = { Text(topBarTitle) },
                                navigationIcon = {
                                    IconButton(onClick = navigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->

                    val screenModifier = Modifier.padding(innerPadding)

                    // high-level navigation:
                    // if a "selected*Url" is active we go to a detail screen,
                    // otherwise we show one of the list/menu screens
                    when {
                        selectedPokemonUrl != null -> {
                            PokemonDetailScreen(
                                pokemonUrl = selectedPokemonUrl!!,
                                modifier = screenModifier
                            )
                        }

                        selectedLocationUrl != null -> {
                            LocationDetailScreen(
                                locationUrl = selectedLocationUrl!!,
                                modifier = screenModifier,
                                onPokemonSelected = { url -> selectedPokemonUrl = url }
                            )
                        }

                        // JUAN: this is the ability detail screen hook-up
                        selectedAbilityUrl != null -> {
                            AbilityDetailScreen(
                                abilityUrl = selectedAbilityUrl!!,
                                modifier = screenModifier
                            )
                        }

                        else -> {
                            // list / menu level
                            when (currentScreen) {
                                "menu" -> MainMenuScreen(
                                    modifier = screenModifier,
                                    onNavigate = { screen -> currentScreen = screen }
                                )

                                "pokemon" -> ListingScreen(
                                    resourceType = "pokemon",
                                    searchHint = "Search by name or Pokédex ID...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedPokemonUrl = url }
                                )

                                "location" -> ListingScreen(
                                    resourceType = "location",
                                    searchHint = "Search for a location...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedLocationUrl = url }
                                )

                                // JUAN: abilities reuse the generic ListingScreen, but when you
                                // tap one we store its URL and open AbilityDetailScreen.
                                "ability" -> ListingScreen(
                                    resourceType = "ability",
                                    searchHint = "Search for a(n) ability...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedAbilityUrl = url }
                                )

                                // these are wired to the list UI but we’re not doing detail views yet
                                "type" -> ListingScreen(
                                    resourceType = "type",
                                    searchHint = "Search for a(n) type...",
                                    modifier = screenModifier,
                                    onResourceSelected = { /* no-op for now */ }
                                )

                                "item" -> ListingScreen(
                                    resourceType = "item",
                                    searchHint = "Search for a(n) item...",
                                    modifier = screenModifier,
                                    onResourceSelected = { /* no-op for now */ }
                                )

                                "move" -> ListingScreen(
                                    resourceType = "move",
                                    searchHint = "Search for a(n) move...",
                                    modifier = screenModifier,
                                    onResourceSelected = { /* no-op for now */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// main menu – just routing to the different list screens
// ----------------------------------------------------------------------

@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Pokedex Menu", modifier = Modifier.padding(bottom = 24.dp), fontSize = 32.sp)
        MenuCard(title = "Pokémon", onClick = { onNavigate("pokemon") })
        MenuCard(title = "Types", onClick = { onNavigate("type") })
        MenuCard(title = "Abilities", onClick = { onNavigate("ability") })
        MenuCard(title = "Locations", onClick = { onNavigate("location") })
        MenuCard(title = "Moves", onClick = { onNavigate("move") })
    }
}

@Composable
fun MenuCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.hsv(277f, 1f, 0.5f))
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )
    }
}

// ----------------------------------------------------------------------
// location detail flow (used when you drill into a location)
// ----------------------------------------------------------------------

@Composable
fun LocationInfoCard(locationName: String, regionName: String?, pokemonCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = locationName.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (regionName != null) {
                Text(
                    text = "Region: ${regionName.replaceFirstChar { it.titlecase() }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Pokémon Species: $pokemonCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LocationDetailScreen(
    locationUrl: String,
    modifier: Modifier = Modifier,
    onPokemonSelected: (String) -> Unit
) {
    var pokemonList by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var locationDetails by remember { mutableStateOf<LocationDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val json = Json { ignoreUnknownKeys = true }

    LaunchedEffect(locationUrl) {
        isLoading = true
        val client = AsyncHttpClient()

        // step 1: get the location so we can grab its first area URL
        client.get(locationUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                locationDetails =
                    json.decodeFromString(LocationDetails.serializer(), jsonResponse.jsonObject.toString())
                val areaUrl = locationDetails?.areas?.firstOrNull()?.url

                if (areaUrl != null) {
                    // step 2: hit the area endpoint and get pokemon_encounters
                    client.get(areaUrl, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                            try {
                                val locationArea =
                                    json.decodeFromString(LocationArea.serializer(), jsonResponse.jsonObject.toString())
                                val uniquePokemon = locationArea.pokemon_encounters
                                    .map { it.pokemon }
                                    .toSet()
                                    .toList()
                                pokemonList = uniquePokemon
                            } catch (e: Exception) {
                                // parsing error – leaving silent for now
                            } finally {
                                isLoading = false
                            }
                        }

                        override fun onFailure(
                            statusCode: Int,
                            headers: Headers?,
                            response: String?,
                            throwable: Throwable?
                        ) {
                            isLoading = false
                        }
                    })
                } else {
                    // no areas for this location, nothing to show
                    isLoading = false
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                isLoading = false
            }
        })
    }

    if (isLoading) {
        Text("Loading Pokémon...", modifier = modifier.padding(16.dp))
    } else if (pokemonList.isEmpty()) {
        Text("No Pokémon found in this area.", modifier = modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = modifier) {
            item {
                locationDetails?.let {
                    LocationInfoCard(
                        locationName = it.areas.first().name,
                        regionName = it.region?.name,
                        pokemonCount = pokemonList.size
                    )
                }
            }
            items(pokemonList) { pokemon ->
                Button(
                    onClick = { onPokemonSelected(pokemon.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pokemonId = pokemon.url.split("/").dropLast(1).last()
                        val spriteUrl =
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

                        AsyncImage(
                            model = spriteUrl,
                            contentDescription = "${pokemon.name} sprite",
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = pokemon.name.replaceFirstChar { it.titlecase() },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// generic listing screen – reused for pokemon, abilities, types, etc.
// ----------------------------------------------------------------------

@Composable
fun ListingScreen(
    resourceType: String,
    searchHint: String,
    modifier: Modifier = Modifier,
    onResourceSelected: (String) -> Unit
) {
    var allItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val json = Json { ignoreUnknownKeys = true }

    // every time we switch resourceType ("pokemon" -> "ability" etc) we refetch the correct endpoint
    LaunchedEffect(resourceType) {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/$resourceType?limit=2000"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val response =
                        json.decodeFromString(ApiResponse.serializer(), jsonResponse.jsonObject.toString())
                    allItems = response.results
                    filteredItems = response.results
                } catch (e: Exception) {
                    // parsing error – keeping it quiet, list will just be empty
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                // request failed – same deal, list stays empty
            }
        })
    }

    // basic in-memory filter for search bar
    LaunchedEffect(searchQuery, allItems) {
        filteredItems = if (searchQuery.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                val nameMatch = item.name.contains(searchQuery, ignoreCase = true)
                if (resourceType == "pokemon") {
                    val id = item.url.split("/").dropLast(1).last()
                    val idMatch = id.contains(searchQuery)
                    nameMatch || idMatch
                } else {
                    nameMatch
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(searchHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredItems) { item ->
                val id = item.url.split("/").dropLast(1).last()
                Button(
                    onClick = { onResourceSelected(item.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name.replaceFirstChar { it.titlecase() },
                            modifier = Modifier.weight(1f)
                        )
                        // id tag only really makes sense for pokemon, so I left it scoped there
                        if (resourceType == "pokemon") {
                            Text(text = "ID: $id")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// ability detail screen – this is the new screen I added for my part
// it takes the ability URL from the list and shows its english effect.
// ----------------------------------------------------------------------

@Composable
fun AbilityDetailScreen(
    abilityUrl: String,
    modifier: Modifier = Modifier
) {
    var abilityDetails by remember { mutableStateOf<AbilityDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val json = Json { ignoreUnknownKeys = true }

    LaunchedEffect(abilityUrl) {
        val client = AsyncHttpClient()

        client.get(abilityUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val details =
                        json.decodeFromString(AbilityDetails.serializer(), jsonResponse.jsonObject.toString())
                    abilityDetails = details
                    isLoading = false
                } catch (e: Exception) {
                    error = "Failed to parse ability data."
                    isLoading = false
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                error = "Failed to fetch ability. Status: $statusCode"
                isLoading = false
            }
        })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            isLoading -> Text("Loading ability...")
            error != null -> Text(error!!)
            abilityDetails != null -> {
                val details = abilityDetails!!

                // grab the english entry if it exists, otherwise just show a fallback
                val englishEntry =
                    details.effect_entries.firstOrNull { it.language.name == "en" }
                val effectText = englishEntry?.short_effect ?: "No effect available."

                val displayName = details.name
                    .replace("-", " ")
                    .replaceFirstChar { it.titlecase() }

                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(text = effectText, fontSize = 16.sp)
            }
        }
    }
}
