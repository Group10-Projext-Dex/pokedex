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
    @SerialName("height") val height: Int,
    @SerialName("weight") val weight: Int,
    @SerialName("types") val types: List<PokemonType> = emptyList(),
    @SerialName("sprites") val sprites: PokemonSprites,
    @SerialName("abilities") val abilities: List<PokemonAbility> = emptyList(),
    @SerialName("stats") val stats: List<PokemonStat> = emptyList()
)

/**
 * Pokemon type information
 */
@Serializable
data class PokemonType(
    @SerialName("slot") val slot: Int,
    @SerialName("type") val type: ApiResource
)

/**
 * Pokemon sprite URLs
 */
@Serializable
data class PokemonSprites(
    @SerialName("front_default") val frontDefault: String? = null,
    @SerialName("front_shiny") val frontShiny: String? = null,
    @SerialName("back_default") val backDefault: String? = null,
    @SerialName("back_shiny") val backShiny: String? = null
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