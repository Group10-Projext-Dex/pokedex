package com.example.project_dex.models

import com.example.project_dex.network.ApiResource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- POKEMON DATA MODELS ---
// These models are for detailed Pokemon information

/**
 * Full Pokemon data from PokeAPI
 */
@Serializable
data class Pokemon(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("height") val height: Int, // in decimeters (1 decimeter = 0.1 meters)
    @SerialName("weight") val weight: Int, // in hectograms (1 hectogram = 0.1 kg)
    @SerialName("types") val types: List<PokemonType> = emptyList(),
    @SerialName("sprites") val sprites: PokemonSprites,
    @SerialName("abilities") val abilities: List<PokemonAbility> = emptyList(),
    @SerialName("stats") val stats: List<PokemonStat> = emptyList()
)

/**
 * Pokemon type information (e.g., Fire, Water, Grass)
 */
@Serializable
data class PokemonType(
    @SerialName("slot") val slot: Int,
    @SerialName("type") val type: ApiResource
)

/**
 * Pokemon sprite URLs for images
 */
@Serializable
data class PokemonSprites(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
    @SerialName("back_default") val backDefault: String? = null,
    @SerialName("other") val other: OtherSprites? = null
)

/**
 * Additional high-quality sprites
 */
@Serializable
data class OtherSprites(
    @SerialName("official-artwork") val officialArtwork: OfficialArtwork? = null
)

@Serializable
data class OfficialArtwork(
    @SerialName("front_default") val frontDefault: String? = null
)

/**
 * Pokemon ability information
 */
@Serializable
data class PokemonAbility(
    @SerialName("ability") val ability: ApiResource,
    @SerialName("is_hidden") val isHidden: Boolean = false,
    @SerialName("slot") val slot: Int
)

/**
 * Pokemon stat information (HP, Attack, Defense, etc.)
 */
@Serializable
data class PokemonStat(
    @SerialName("stat") val stat: ApiResource,
    @SerialName("base_stat") val baseStat: Int,
    @SerialName("effort") val effort: Int
)

// --- HELPER EXTENSIONS ---

/**
 * Get the primary type (first type)
 */
fun Pokemon.getPrimaryType(): String? {
    return types.firstOrNull()?.type?.name
}

/**
 * Get all type names
 */
fun Pokemon.getTypeNames(): List<String> {
    return types.map { it.type.name }
}

/**
 * Get the best sprite URL available
 */
fun Pokemon.getBestSpriteUrl(): String? {
    return sprites.other?.officialArtwork?.frontDefault
        ?: sprites.frontDefault
}

/**
 * Convert height to meters
 */
fun Pokemon.getHeightInMeters(): Double {
    return height / 10.0
}

/**
 * Convert height to feet and inches
 */
fun Pokemon.getHeightInFeet(): String {
    val meters = getHeightInMeters()
    val totalInches = meters * 39.3701
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).toInt()
    return "${feet}'${inches}\""
}

/**
 * Convert weight to kilograms
 */
fun Pokemon.getWeightInKg(): Double {
    return weight / 10.0
}

/**
 * Convert weight to pounds
 */
fun Pokemon.getWeightInLbs(): Double {
    return getWeightInKg() * 2.20462
}

/**
 * Get non-hidden abilities
 */
fun Pokemon.getRegularAbilities(): List<String> {
    return abilities.filter { !it.isHidden }.map { it.ability.name }
}

/**
 * Get hidden ability if exists
 */
fun Pokemon.getHiddenAbility(): String? {
    return abilities.find { it.isHidden }?.ability.name
}

/**
 * Get stat by name (hp, attack, defense, etc.)
 */
fun Pokemon.getStatByName(statName: String): Int? {
    return stats.find { it.stat.name == statName }?.baseStat
}

/**
 * Get total stats (sum of all base stats)
 */
fun Pokemon.getTotalStats(): Int {
    return stats.sumOf { it.baseStat }
}