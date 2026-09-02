package com.adn.adnapp.data.remote.firebase

import com.adn.adnapp.data.model.entity.WeightEntry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WeightDataSource(private val db: FirebaseFirestore) {

    fun observeHistory(uid: String): Flow<List<WeightEntry>> = callbackFlow {
        val listener = history(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val entries = snapshot?.documents.orEmpty().mapNotNull { document ->
                document.getDouble(FIELD_KILOGRAMS)?.let { kilograms ->
                    WeightEntry(date = document.id, kilograms = kilograms)
                }
            }.sortedBy { it.date }
            trySend(entries)
        }
        awaitClose { listener.remove() }
    }

    suspend fun save(uid: String, entry: WeightEntry) {
        history(uid).document(entry.date).set(
            mapOf(
                FIELD_KILOGRAMS to entry.kilograms,
                FIELD_UPDATED_AT to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
    }

    private fun history(uid: String) = db.collection(USERS).document(uid).collection(WEIGHT_HISTORY)

    private companion object {
        const val USERS = "users"
        const val WEIGHT_HISTORY = "weightHistory"
        const val FIELD_KILOGRAMS = "kilograms"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
