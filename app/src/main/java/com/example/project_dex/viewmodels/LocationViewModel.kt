package com.example.project_dex.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.project_dex.models.*
import com.example.project_dex.network.ApiResource
import com.example.project_dex.network.PagedResponse
import kotlinx.serialization.json.Json
import okhttp3.Headers

// --- UI STATES ---

sealed interface LocationUiState {
    data class Success(val locations: List<ApiResource>) : LocationUiState
    object Error : LocationUiState
    object Loading : LocationUiState
}

sealed interface LocationDetailUiState {
    data class Success(val location: Location) : LocationDetailUiState
    object Error : LocationDetailUiState
    object Loading : LocationDetailUiState
}

// --- VIEWMODEL ---

/**
 * ViewModel for managing Pokemon Location data
 * Handles fetching location list, location details, and Pokemon encounters
 */
class LocationViewModel : ViewModel() {
    var uiState: LocationUiState by mutableStateOf(LocationUiState.Loading)
        private set

    var detailUiState: LocationDetailUiState by mutableStateOf(LocationDetailUiState.Loading)
        private set

    var encountersUiState: Map<String, List<PokemonEncounter>> by mutableStateOf(emptyMap())
        private set

    var pokemonDetailsUiState: Map<String, Pokemon> by mutableStateOf(emptyMap())
        private set

    private val jsonParser = Json { ignoreUnknownKeys = true }

    /**
     * Fetch ALL locations from PokeAPI
     */
    fun fetchLocationList() {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/location?limit=10000"

        uiState = LocationUiState.Loading

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val jsonString = json.jsonObject.toString()
                    val pagedResponse = jsonParser.decodeFromString<PagedResponse>(jsonString)
                    uiState = LocationUiState.Success(pagedResponse.results)
                } catch (e: Exception) {
                    uiState = LocationUiState.Error
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                uiState = LocationUiState.Error
            }
        })
    }

    /**
     * Fetch detailed information for a specific location
     */
    fun fetchLocationDetail(url: String) {
        val client = AsyncHttpClient()
        detailUiState = LocationDetailUiState.Loading

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val jsonString = json.jsonObject.toString()
                    val location = jsonParser.decodeFromString<Location>(jsonString)
                    detailUiState = LocationDetailUiState.Success(location)
                } catch (e: Exception) {
                    detailUiState = LocationDetailUiState.Error
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                detailUiState = LocationDetailUiState.Error
            }
        })
    }

    /**
     * Fetch Pokemon encounters for a specific area
     */
    fun fetchAreaEncounters(areaUrl: String) {
        val client = AsyncHttpClient()
        
        client.get(areaUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val jsonString = json.jsonObject.toString()
                    val areaDetail = jsonParser.decodeFromString<LocationAreaDetail>(jsonString)
                    
                    encountersUiState = encountersUiState + (areaUrl to areaDetail.pokemonEncounters)
                    
                    // Fetch details for each Pokemon
                    areaDetail.pokemonEncounters.forEach { encounter ->
                        fetchPokemonDetails(encounter.pokemon.url)
                    }
                } catch (e: Exception) {
                    // Silently fail for individual areas
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                // Silently fail
            }
        })
    }

    /**
     * Fetch detailed Pokemon information
     */
    fun fetchPokemonDetails(pokemonUrl: String) {
        val client = AsyncHttpClient()
        
        client.get(pokemonUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val jsonString = json.jsonObject.toString()
                    val pokemon = jsonParser.decodeFromString<Pokemon>(jsonString)
                    pokemonDetailsUiState = pokemonDetailsUiState + (pokemon.name to pokemon)
                } catch (e: Exception) {
                    // Silently fail
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                // Silently fail
            }
        })
    }

    /**
     * Fetch encounters for all areas in a location
     */
    fun fetchAllAreaEncounters(areas: List<ApiResource>) {
        encountersUiState = emptyMap()
        pokemonDetailsUiState = emptyMap()
        areas.forEach { area ->
            fetchAreaEncounters(area.url)
        }
    }

    /**
     * Reset state when navigating back
     */
    fun resetDetailState() {
        detailUiState = LocationDetailUiState.Loading
        encountersUiState = emptyMap()
        pokemonDetailsUiState = emptyMap()
    }
}