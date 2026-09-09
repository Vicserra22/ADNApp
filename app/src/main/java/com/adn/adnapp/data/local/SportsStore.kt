package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.SportSession
import com.adn.adnapp.domain.model.SportsData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SportsStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Any()
    private val _data = MutableStateFlow(read())
    val data = _data.asStateFlow()

    fun save(session: SportSession) = update { data ->
        data.copy(sessions = (data.sessions.filterNot { it.id == session.id } + session).sortedByDescending { it.date + it.createdAt })
    }

    fun remove(id: String) = update { data -> data.copy(sessions = data.sessions.filterNot { it.id == id }) }

    private fun update(transform: (SportsData) -> SportsData) {
        synchronized(lock) {
            val next = transform(_data.value)
            _data.value = next
            preferences.edit().putString(KEY_DATA, gson.toJson(next)).apply()
        }
    }

    private fun read(): SportsData = runCatching {
        gson.fromJson(preferences.getString(KEY_DATA, null), SportsData::class.java) ?: SportsData()
    }.getOrDefault(SportsData())

    private companion object { const val FILE_NAME = "adn-sports"; const val KEY_DATA = "sessions" }
}
