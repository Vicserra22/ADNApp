package com.adn.adnapp.data.local

import android.content.Context
import com.google.gson.Gson
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WaterLog(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val date: String,
    val amountMl: Double,
    val remoteEntryId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class SunLog(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: String,
    val date: String,
    val minutes: Int,
    val createdAt: Long = System.currentTimeMillis()
)

data class WellnessData(
    val water: List<WaterLog> = emptyList(),
    val sun: List<SunLog> = emptyList()
)

class WellnessStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Any()
    private val _data = MutableStateFlow(read())
    val data = _data.asStateFlow()

    fun addWater(log: WaterLog) = update { it.copy(water = it.water + log) }

    fun attachRemoteEntry(logId: String, remoteEntryId: String) = update { data ->
        data.copy(water = data.water.map { if (it.id == logId) it.copy(remoteEntryId = remoteEntryId) else it })
    }

    fun removeWater(logId: String) = update { data -> data.copy(water = data.water.filterNot { it.id == logId }) }

    fun addSun(log: SunLog) = update { it.copy(sun = it.sun + log) }

    fun removeSun(logId: String) = update { data -> data.copy(sun = data.sun.filterNot { it.id == logId }) }

    private fun update(transform: (WellnessData) -> WellnessData) {
        synchronized(lock) {
            val next = transform(_data.value)
            _data.value = next
            preferences.edit().putString(KEY_DATA, gson.toJson(next)).apply()
        }
    }

    private fun read(): WellnessData = runCatching {
        gson.fromJson(preferences.getString(KEY_DATA, null), WellnessData::class.java) ?: WellnessData()
    }.getOrDefault(WellnessData())

    private companion object {
        const val FILE_NAME = "adn-wellness"
        const val KEY_DATA = "logs"
    }
}
