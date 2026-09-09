package com.adn.adnapp.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.local.FinanceStore
import com.adn.adnapp.domain.model.FinanceGoal
import com.adn.adnapp.domain.model.InvestmentPosition
import com.adn.adnapp.domain.model.MoneyEntry
import com.adn.adnapp.domain.model.MoneyKind
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinanceUiState(
    val selectedKind: MoneyKind = MoneyKind.EXPENSE, val entries: List<MoneyEntry> = emptyList(), val goals: List<FinanceGoal> = emptyList(), val investments: List<InvestmentPosition> = emptyList(),
    val dateInput: String = LocalDate.now().toString(), val titleInput: String = "", val amountInput: String = "", val categoryInput: String = "", val planned: Boolean = false, val recurring: Boolean = false,
    val goalNameInput: String = "", val goalTargetInput: String = "", val goalSavedInput: String = "", val investmentNameInput: String = "", val unitsInput: String = "", val averagePriceInput: String = "", val currentPriceInput: String = "", val error: String? = null, val message: String? = null
) {
    val incomeCents get() = entries.filter { it.kind == MoneyKind.INCOME && !it.planned }.sumOf { it.amountCents }
    val expenseCents get() = entries.filter { it.kind == MoneyKind.EXPENSE && !it.planned }.sumOf { it.amountCents }
    val futureExpenseCents get() = entries.filter { it.kind == MoneyKind.EXPENSE && it.planned }.sumOf { it.amountCents }
    val balanceCents get() = incomeCents - expenseCents
    val projectedCents get() = balanceCents - futureExpenseCents
    val portfolioCents get() = investments.sumOf { (it.units * it.currentPriceCents).toLong() }
}

class FinanceViewModel(private val store: FinanceStore) : ViewModel() {
    private val _uiState = MutableStateFlow(FinanceUiState()); val uiState = _uiState.asStateFlow()
    init { viewModelScope.launch { store.data.collect { data -> _uiState.update { it.copy(entries = data.entries, goals = data.goals, investments = data.investments) } } } }
    fun selectKind(kind: MoneyKind) = _uiState.update { it.copy(selectedKind = kind, error = null) }
    fun onDateChanged(v: String) = _uiState.update { it.copy(dateInput = v, error = null) }
    fun onTitleChanged(v: String) = _uiState.update { it.copy(titleInput = v, error = null) }
    fun onAmountChanged(v: String) = _uiState.update { it.copy(amountInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onCategoryChanged(v: String) = _uiState.update { it.copy(categoryInput = v, error = null) }
    fun setPlanned(v: Boolean) = _uiState.update { it.copy(planned = v) }
    fun setRecurring(v: Boolean) = _uiState.update { it.copy(recurring = v) }
    fun addEntry() {
        val state = _uiState.value; val date = runCatching { LocalDate.parse(state.dateInput) }.getOrNull() ?: return fail("La fecha debe tener formato AAAA-MM-DD")
        val amount = parseCents(state.amountInput) ?: return fail("Introduce un importe mayor que cero")
        if (state.titleInput.isBlank()) return fail("Pon un concepto al movimiento")
        store.saveEntry(MoneyEntry(title = state.titleInput.trim(), amountCents = amount, date = date.toString(), kind = state.selectedKind, category = state.categoryInput.trim().ifBlank { "General" }, planned = state.planned, recurring = state.recurring))
        _uiState.update { it.copy(titleInput = "", amountInput = "", categoryInput = "", planned = false, recurring = false, error = null, message = "Movimiento guardado") }
    }
    fun removeEntry(entry: MoneyEntry) { store.removeEntry(entry.id); _uiState.update { it.copy(message = "Movimiento eliminado") } }
    fun onGoalNameChanged(v: String) = _uiState.update { it.copy(goalNameInput = v, error = null) }
    fun onGoalTargetChanged(v: String) = _uiState.update { it.copy(goalTargetInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onGoalSavedChanged(v: String) = _uiState.update { it.copy(goalSavedInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun addGoal() { val state = _uiState.value; val target = parseCents(state.goalTargetInput) ?: return fail("Indica el objetivo de la hucha"); if (state.goalNameInput.isBlank()) return fail("Pon un nombre al objetivo"); store.saveGoal(FinanceGoal(name = state.goalNameInput.trim(), targetCents = target, savedCents = parseCents(state.goalSavedInput) ?: 0)); _uiState.update { it.copy(goalNameInput = "", goalTargetInput = "", goalSavedInput = "", message = "Objetivo guardado", error = null) } }
    fun removeGoal(goal: FinanceGoal) { store.removeGoal(goal.id); _uiState.update { it.copy(message = "Objetivo eliminado") } }
    fun onInvestmentNameChanged(v: String) = _uiState.update { it.copy(investmentNameInput = v, error = null) }
    fun onUnitsChanged(v: String) = _uiState.update { it.copy(unitsInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onAveragePriceChanged(v: String) = _uiState.update { it.copy(averagePriceInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun onCurrentPriceChanged(v: String) = _uiState.update { it.copy(currentPriceInput = v.filter { it.isDigit() || it == '.' || it == ',' }, error = null) }
    fun addInvestment() { val state = _uiState.value; val units = parseDecimal(state.unitsInput); val average = parseCents(state.averagePriceInput); val current = parseCents(state.currentPriceInput); if (state.investmentNameInput.isBlank() || units == null || average == null || current == null || units <= 0) return fail("Completa nombre, unidades y precios de la cartera"); store.saveInvestment(InvestmentPosition(name = state.investmentNameInput.trim(), units = units, averagePriceCents = average, currentPriceCents = current)); _uiState.update { it.copy(investmentNameInput = "", unitsInput = "", averagePriceInput = "", currentPriceInput = "", error = null, message = "Posición guardada") } }
    fun removeInvestment(position: InvestmentPosition) { store.removeInvestment(position.id); _uiState.update { it.copy(message = "Posición eliminada") } }
    private fun parseCents(v: String): Long? = parseDecimal(v)?.let { (it * 100).toLong() }?.takeIf { it > 0 }
    private fun parseDecimal(v: String): Double? = v.replace(',', '.').toDoubleOrNull()
    private fun fail(v: String) { _uiState.update { it.copy(error = v, message = null) } }
}

fun moneyLabel(cents: Long): String = "${cents / 100},${(cents % 100).toString().padStart(2, '0')} €"
