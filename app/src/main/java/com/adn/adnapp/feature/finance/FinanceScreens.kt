package com.adn.adnapp.feature.finance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adn.adnapp.core.ui.CompactTopBar
import com.adn.adnapp.core.ui.LocalFloatingNavigationInset
import com.adn.adnapp.domain.model.FinanceGoal
import com.adn.adnapp.domain.model.InvestmentPosition
import com.adn.adnapp.domain.model.MoneyEntry
import com.adn.adnapp.domain.model.MoneyKind
import org.koin.androidx.compose.koinViewModel

@Composable
fun FinanceHomeScreen(viewModel: FinanceViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp, 38.dp, 16.dp, LocalFloatingNavigationInset.current + 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("Tu dinero en contexto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Registra, entiende y decide con cifras que distinguen lo real de lo previsto.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { FinanceSummary(state) }
            item { EntryForm(state, viewModel) }
            item { GoalForm(state, viewModel) }
            if (state.goals.isNotEmpty()) item { Text("Huchas dinámicas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(state.goals, key = { it.id }) { goal -> GoalCard(goal, viewModel::removeGoal) }
            item { InvestmentForm(state, viewModel) }
            items(state.investments, key = { it.id }) { position -> InvestmentCard(position, viewModel::removeInvestment) }
            if (state.entries.isNotEmpty()) item { Text("Movimientos recientes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(state.entries.take(12), key = { it.id }) { entry -> EntryCard(entry, viewModel::removeEntry) }
            state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            state.message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable private fun FinanceSummary(state: FinanceUiState) { Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) { Text("Balanza", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(moneyLabel(state.balanceCents), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ingresos ${moneyLabel(state.incomeCents)}"); Text("Gastos ${moneyLabel(state.expenseCents)}") }; Text("Futuros: ${moneyLabel(state.futureExpenseCents)} · Margen proyectado: ${moneyLabel(state.projectedCents)}", style = MaterialTheme.typography.bodySmall) } } }

@Composable private fun EntryForm(state: FinanceUiState, vm: FinanceViewModel) { Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Añadir movimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) { MoneyKind.entries.forEach { kind -> FilterChip(state.selectedKind == kind, { vm.selectKind(kind) }, label = { Text(kind.label) }) } }; OutlinedTextField(state.titleInput, vm::onTitleChanged, label = { Text("Concepto") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(state.amountInput, vm::onAmountChanged, label = { Text("Importe €") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)); OutlinedTextField(state.categoryInput, vm::onCategoryChanged, label = { Text("Categoría") }, singleLine = true, modifier = Modifier.weight(1f)) }; OutlinedTextField(state.dateInput, vm::onDateChanged, label = { Text("Fecha AAAA-MM-DD") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(state.planned, vm::setPlanned); Text("Es futuro / previsto", Modifier.weight(1f)); Checkbox(state.recurring, vm::setRecurring); Text("Recurrente") }; Button(vm::addEntry, Modifier.fillMaxWidth().semantics { contentDescription = "Guardar movimiento" }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Guardar movimiento") } } } }

@Composable private fun GoalForm(state: FinanceUiState, vm: FinanceViewModel) { Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Hucha y objetivos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); OutlinedTextField(state.goalNameInput, vm::onGoalNameChanged, label = { Text("Objetivo") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(state.goalTargetInput, vm::onGoalTargetChanged, label = { Text("Meta €") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)); OutlinedTextField(state.goalSavedInput, vm::onGoalSavedChanged, label = { Text("Ahorrado €") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)) }; Button(vm::addGoal, Modifier.fillMaxWidth()) { Text("Crear hucha") } } } }

@Composable private fun GoalCard(goal: FinanceGoal, onRemove: (FinanceGoal) -> Unit) { val progress = (goal.savedCents.toFloat() / goal.targetCents).coerceIn(0f, 1f); Card(shape = RoundedCornerShape(18.dp)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(goal.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); IconButton({ onRemove(goal) }, Modifier.semantics { contentDescription = "Eliminar objetivo ${goal.name}" }) { Icon(Icons.Default.Delete, "Eliminar") } }; LinearProgressIndicator({ progress }, Modifier.fillMaxWidth()); Text("${moneyLabel(goal.savedCents)} de ${moneyLabel(goal.targetCents)} · ${(progress * 100).toInt()} %") } } }

@Composable private fun InvestmentForm(state: FinanceUiState, vm: FinanceViewModel) { Card(shape = RoundedCornerShape(22.dp)) { Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Cartera manual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text("Añade posiciones y su valoración actual. La app separa aportación de resultado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); OutlinedTextField(state.investmentNameInput, vm::onInvestmentNameChanged, label = { Text("Activo o fondo") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(state.unitsInput, vm::onUnitsChanged, label = { Text("Unidades") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)); OutlinedTextField(state.averagePriceInput, vm::onAveragePriceChanged, label = { Text("Compra €") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)); OutlinedTextField(state.currentPriceInput, vm::onCurrentPriceChanged, label = { Text("Actual €") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f)) }; Button(vm::addInvestment, Modifier.fillMaxWidth()) { Text("Guardar posición") } } } }

@Composable private fun InvestmentCard(position: InvestmentPosition, onRemove: (InvestmentPosition) -> Unit) { Card(shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary); Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(position.name, fontWeight = FontWeight.Bold); Text("${position.units} uds · Valor ${moneyLabel((position.units * position.currentPriceCents).toLong())}"); val result = (position.currentPriceCents - position.averagePriceCents) * position.units; Text("Resultado no realizado: ${moneyLabel(result.toLong())}", color = if (result >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }; IconButton({ onRemove(position) }) { Icon(Icons.Default.Delete, "Eliminar") } } } }

@Composable private fun EntryCard(entry: MoneyEntry, onRemove: (MoneyEntry) -> Unit) { Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(entry.title, fontWeight = FontWeight.Medium); Text("${entry.date} · ${entry.category}${if (entry.planned) " · previsto" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Text((if (entry.kind == MoneyKind.INCOME) "+" else "−") + moneyLabel(entry.amountCents), fontWeight = FontWeight.Bold, color = if (entry.kind == MoneyKind.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error); IconButton({ onRemove(entry) }) { Icon(Icons.Default.Delete, "Eliminar") } } }

@Composable fun FinanceAnalysisScreen(viewModel: FinanceViewModel = koinViewModel()) { val state by viewModel.uiState.collectAsStateWithLifecycle(); Scaffold { padding -> Column(Modifier.fillMaxSize().padding(padding).padding(start = 16.dp, end = 16.dp, top = 38.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Qué margen te queda", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); FinanceSummary(state); Text("Distribución de gastos reales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); val categories = state.entries.filter { it.kind == MoneyKind.EXPENSE && !it.planned }.groupBy { it.category }.mapValues { values -> values.value.sumOf { it.amountCents } }.toList().sortedByDescending { it.second }; if (categories.isEmpty()) Text("Añade gastos para ver el análisis.", color = MaterialTheme.colorScheme.onSurfaceVariant) else categories.forEach { (category, amount) -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(category); Text(moneyLabel(amount)) }; LinearProgressIndicator({ (amount.toFloat() / state.expenseCents.coerceAtLeast(1)).coerceIn(0f, 1f) }, Modifier.fillMaxWidth()) } }; Card(shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Info, null); Text("La proyección resta gastos previstos una sola vez. Las transferencias entre cuentas y las cotizaciones externas requieren conciliación posterior.", Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodySmall) } } } } }
