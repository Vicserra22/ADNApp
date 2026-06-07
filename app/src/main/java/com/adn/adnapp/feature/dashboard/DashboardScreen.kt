package com.adn.adnapp.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adn.adnapp.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentConsumption = uiState.history[uiState.selectedDate]

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_dashboard)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Fecha seleccionada: ${uiState.selectedDate}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            // ComposeCalendar implementation will go here in the future
            // For now just display the stats
            
            if (currentConsumption != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Consumo", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Calorías: ${currentConsumption.calories}")
                        Text("Proteínas: ${currentConsumption.proteins}")
                        Text("Carbos: ${currentConsumption.carbs}")
                        Text("Grasas: ${currentConsumption.fats}")
                    }
                }
            } else {
                Text("No hay datos para esta fecha.")
            }
        }
    }
}
