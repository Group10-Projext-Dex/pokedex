package com.example.project_dex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project_dex.ui.theme.Project_dexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_dexTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("menu") }
    var isInDetailView by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (currentScreen != "menu" && !isInDetailView) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentScreen) {
                                "pokemon" -> "Pokemon List"
                                "move" -> "Move List"
                                "ability" -> "Ability List"
                                "type" -> "Type List"
                                "location" -> "Location List"
                                else -> ""
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            currentScreen = "menu"
                            isInDetailView = false
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Menu"
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "menu" -> MainMenuScreen(
                    onNavigate = { screen ->
                        currentScreen = screen
                        isInDetailView = false
                    }
                )

                "pokemon" -> ListingScreen(
                    resourceType = "pokemon",
                    searchHint = "Search for a Pokémon..."
                )

                "move" -> ListingScreen(
                    resourceType = "move",
                    searchHint = "Search for a Move..."
                )

                "ability" -> ListingScreen(
                    resourceType = "ability",
                    searchHint = "Search for an Ability..."
                )

                "type" -> ListingScreen(
                    resourceType = "type",
                    searchHint = "Search for a Pokémon Type..."
                )

                "location" -> LocationScreen(
                    onDetailViewChange = { isDetail ->
                        isInDetailView = isDetail
                    }
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Pokédex Main Menu",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        MenuButton(text = "Pokémon", onClick = { onNavigate("pokemon") })
        MenuButton(text = "Moves", onClick = { onNavigate("move") })
        MenuButton(text = "Abilities", onClick = { onNavigate("ability") })
        MenuButton(text = "Types", onClick = { onNavigate("type") })
        MenuButton(text = "Locations", onClick = { onNavigate("location") })
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp)
            .height(56.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium
        )
    }
}