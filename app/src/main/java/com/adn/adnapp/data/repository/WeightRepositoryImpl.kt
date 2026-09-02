package com.adn.adnapp.data.repository

import com.adn.adnapp.data.model.entity.WeightEntry
import com.adn.adnapp.data.remote.firebase.WeightDataSource
import com.adn.adnapp.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow

class WeightRepositoryImpl(
    private val dataSource: WeightDataSource
) : WeightRepository {
    override fun observeWeightHistory(uid: String): Flow<List<WeightEntry>> =
        dataSource.observeHistory(uid)

    override suspend fun saveWeight(uid: String, entry: WeightEntry): Result<Unit> =
        runCatching { dataSource.save(uid, entry) }
}
