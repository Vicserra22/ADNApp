package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.RecoveryData
import com.adn.adnapp.domain.model.SleepLog
import com.adn.adnapp.domain.model.StepLog
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecoveryStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson(); private val lock = Any()
    private val _data = MutableStateFlow(read()); val data = _data.asStateFlow()
    fun saveSteps(log: StepLog) = update { it.copy(steps = (it.steps.filterNot { value -> value.date == log.date } + log)) }
    fun saveSleep(log: SleepLog) = update { it.copy(sleep = (it.sleep.filterNot { value -> value.date == log.date } + log)) }
    fun removeSteps(date: String) = update { it.copy(steps = it.steps.filterNot { value -> value.date == date }) }
    fun removeSleep(date: String) = update { it.copy(sleep = it.sleep.filterNot { value -> value.date == date }) }
    private fun update(transform: (RecoveryData) -> RecoveryData) { synchronized(lock) { val next = transform(_data.value); _data.value = next; preferences.edit().putString(KEY_DATA, gson.toJson(next)).apply() } }
    private fun read(): RecoveryData = runCatching { gson.fromJson(preferences.getString(KEY_DATA, null), RecoveryData::class.java) ?: RecoveryData() }.getOrDefault(RecoveryData())
    private companion object { const val FILE_NAME = "adn-recovery"; const val KEY_DATA = "logs" }
}
