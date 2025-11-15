package com.example.project_dex.models

import com.example.project_dex.network.ApiResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- LOCATION DATA MODELS ---

/**
 * Location name in different languages
 */
@Serializable
data class LocationName(
    @SerialName("name") val name: String,
    @SerialName("language") val language: ApiResource
)

/**
 * Main location data from PokeAPI
 */
@Serializable
data class Location(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("region") val region: ApiResource? = null,
    @SerialName("game_indices") val gameIndices: List<GameIndex> = emptyList(),
    @SerialName("names") val names: List<LocationName> = emptyList(),
    @SerialName("areas") val areas: List<ApiResource> = emptyList()
)

/**
 * Game index showing which generation the location appeared in
 */
@Serializable
data class GameIndex(
    @SerialName("game_index") val gameIndex: Int,
    @SerialName("generation") val generation: ApiResource
)

// --- POKEMON ENCOUNTER MODELS ---

/**
 * Detailed area information with Pokemon encounters
 */
@Serializable
data class LocationAreaDetail(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("pokemon_encounters") val pokemonEncounters: List<PokemonEncounter> = emptyList()
)

/**
 * Pokemon that can be encountered at a location
 */
@Serializable
data class PokemonEncounter(
    @SerialName("pokemon") val pokemon: ApiResource,
    @SerialName("version_details") val versionDetails: List<VersionEncounterDetail> = emptyList()
)

/**
 * Encounter details for different game versions
 */
@Serializable
data class VersionEncounterDetail(
    @SerialName("max_chance") val maxChance: Int = 0,
    @SerialName("encounter_details") val encounterDetails: List<EncounterDetail> = emptyList()
)

/**
 * Specific encounter information (level, chance, method)
 */
@Serializable
data class EncounterDetail(
    @SerialName("min_level") val minLevel: Int,
    @SerialName("max_level") val maxLevel: Int,
    @SerialName("chance") val chance: Int,
    @SerialName("method") val method: ApiResource
)