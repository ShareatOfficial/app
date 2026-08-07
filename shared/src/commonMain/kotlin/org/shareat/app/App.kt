package org.shareat.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
private sealed interface Route : NavKey

@Serializable
private data object Home : Route

@Serializable
private data class Details(val id: String) : Route

@OptIn(ExperimentalSerializationApi::class)
private val navConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<Route>()
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        val backStack = rememberNavBackStack(navConfiguration, Home)
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(
                        onShowDetails = { backStack.add(Details(id = "123")) }
                    )
                }
                entry<Details> { route ->
                    DetailsScreen(
                        id = route.id,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}

// TODO remove when create the first screen
@Composable
private fun HomeScreen(onShowDetails: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Home", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onShowDetails) {
            Text("Show details")
        }
    }
}

// TODO remove when create the first sreen
@Composable
private fun DetailsScreen(id: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Details", style = MaterialTheme.typography.headlineMedium)
                Text("Item id: $id")
            }
        }
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
