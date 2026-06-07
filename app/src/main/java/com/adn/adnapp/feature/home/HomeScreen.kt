package com.adn.adnapp.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adn.adnapp.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            // In a real app we would use a SnackbarHostState here, but for simplicity:
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_home)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_food)) },
                trailingIcon = {
                    IconButton(onClick = viewModel::searchFood) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.selectedProduct != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(uiState.selectedProduct!!.name, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Macros por 100g:")
                        Text("Calorías: ${uiState.selectedProduct!!.calories}")
                        Text("Proteínas: ${uiState.selectedProduct!!.proteins}")
                        Text("Carbos: ${uiState.selectedProduct!!.carbs}")
                        Text("Grasas: ${uiState.selectedProduct!!.fats}")

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = uiState.quantityToAdd,
                            onValueChange = viewModel::onQuantityChanged,
                            label = { Text(stringResource(R.string.quantity_grams)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::addFoodEntry,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.add))
                        }
                    }
                }
            } else if (uiState.isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.searchResults) { product ->
                        ListItem(
                            headlineContent = { Text(product.name) },
                            supportingContent = { Text("${product.calories} kcal/100g") },
                            modifier = Modifier.clickable { viewModel.onProductSelected(product) }
                        )
                        Divider()
                    }
                }
            }

            if (uiState.error != null) {
                Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.successMessage != null) {
                Text(text = uiState.successMessage!!, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
