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

fun Pokemon.getPrimaryType(): String? {
    return types.firstOrNull()?.type?.name
}

fun Pokemon.getTypeNames(): List<String> {
    return types.mapNotNull { it.type?.name }
}

fun Pokemon.getBestSpriteUrl(): String? {
    return sprites.other?.officialArtwork?.frontDefault
        ?: sprites.frontDefault
}

fun Pokemon.getHeightInMeters(): Double {
    return height / 10.0
}

fun Pokemon.getHeightInFeet(): String {
    val meters = getHeightInMeters()
    val totalInches = meters * 39.3701
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).toInt()
    return "${feet}'${inches}\""
}

fun Pokemon.getWeightInKg(): Double {
    return weight / 10.0
}

fun Pokemon.getWeightInLbs(): Double {
    return getWeightInKg() * 2.20462
}

fun Pokemon.getRegularAbilities(): List<String> {
    return abilities.filter { !it.isHidden }.mapNotNull { it.ability?.name }
}

fun Pokemon.getHiddenAbility(): String? {
    return abilities.find { it.isHidden }?.ability?.name
}

fun Pokemon.getStatByName(statName: String): Int? {
    return stats.find { it.stat?.name == statName }?.baseStat
}

fun Pokemon.getTotalStats(): Int {
    return stats.sumOf { it.baseStat }
}