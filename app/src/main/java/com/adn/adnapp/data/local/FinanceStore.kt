package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.FinanceData
import com.adn.adnapp.domain.model.FinanceGoal
import com.adn.adnapp.domain.model.InvestmentPosition
import com.adn.adnapp.domain.model.MoneyEntry
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FinanceStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson(); private val lock = Any()
    private val _data = MutableStateFlow(read()); val data = _data.asStateFlow()
    fun saveEntry(entry: MoneyEntry) = update { it.copy(entries = (it.entries.filterNot { value -> value.id == entry.id } + entry).sortedByDescending { value -> value.date + value.createdAt }) }
    fun removeEntry(id: String) = update { it.copy(entries = it.entries.filterNot { value -> value.id == id }) }
    fun saveGoal(goal: FinanceGoal) = update { it.copy(goals = (it.goals.filterNot { value -> value.id == goal.id } + goal).sortedBy { value -> value.createdAt }) }
    fun removeGoal(id: String) = update { it.copy(goals = it.goals.filterNot { value -> value.id == id }) }
    fun saveInvestment(position: InvestmentPosition) = update { it.copy(investments = (it.investments.filterNot { value -> value.id == position.id } + position).sortedBy { value -> value.createdAt }) }
    fun removeInvestment(id: String) = update { it.copy(investments = it.investments.filterNot { value -> value.id == id }) }
    private fun update(transform: (FinanceData) -> FinanceData) { synchronized(lock) { val next = transform(_data.value); _data.value = next; preferences.edit().putString(KEY_DATA, gson.toJson(next)).apply() } }
    private fun read(): FinanceData = runCatching { gson.fromJson(preferences.getString(KEY_DATA, null), FinanceData::class.java) ?: FinanceData() }.getOrDefault(FinanceData())
    private companion object { const val FILE_NAME = "adn-finance"; const val KEY_DATA = "data" }
}
