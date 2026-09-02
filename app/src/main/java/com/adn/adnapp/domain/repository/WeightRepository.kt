package com.adn.adnapp.domain.repository

import com.adn.adnapp.data.model.entity.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeWeightHistory(uid: String): Flow<List<WeightEntry>>
    suspend fun saveWeight(uid: String, entry: WeightEntry): Result<Unit>
}
