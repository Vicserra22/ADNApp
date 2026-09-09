package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.AgendaData
import com.adn.adnapp.domain.model.AgendaItem
import com.adn.adnapp.domain.model.AgendaProject
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgendaStore(context: Context) {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val lock = Any()
    private val _data = MutableStateFlow(read())
    val data = _data.asStateFlow()

    fun saveItem(item: AgendaItem) = update { data ->
        data.copy(items = (data.items.filterNot { it.id == item.id } + item).sortedBy { it.createdAt })
    }

    fun removeItem(id: String) = update { data -> data.copy(items = data.items.filterNot { it.id == id }) }

    fun saveProject(project: AgendaProject) = update { data ->
        data.copy(projects = (data.projects.filterNot { it.id == project.id } + project).sortedBy { it.createdAt })
    }

    fun removeProject(id: String) = update { data ->
        data.copy(
            projects = data.projects.filterNot { it.id == id },
            items = data.items.map { if (it.projectId == id) it.copy(projectId = null) else it }
        )
    }

    private fun update(transform: (AgendaData) -> AgendaData) {
        synchronized(lock) {
            val next = transform(_data.value)
            _data.value = next
            preferences.edit().putString(KEY_DATA, gson.toJson(next)).apply()
        }
    }

    private fun read(): AgendaData = runCatching {
        gson.fromJson(preferences.getString(KEY_DATA, null), AgendaData::class.java) ?: AgendaData()
    }.getOrDefault(AgendaData())

    private companion object {
        const val FILE_NAME = "adn-agenda"
        const val KEY_DATA = "data"
    }
}
