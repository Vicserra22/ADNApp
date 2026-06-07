package com.adn.adnapp.data.remote.firebase

import com.adn.adnapp.core.constants.FirestoreKeys
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreDataSource(private val db: FirebaseFirestore) {

    suspend fun saveUserProfile(uid: String, profile: UserProfile) {
        val data = mapOf(
            FirestoreKeys.NAME to profile.name,
            FirestoreKeys.AGE to profile.age,
            FirestoreKeys.WEIGHT to profile.weight,
            FirestoreKeys.HEIGHT to profile.height,
            FirestoreKeys.GENDER to profile.gender,
            FirestoreKeys.DIET to profile.dietId
        )
        db.collection(FirestoreKeys.USERS).document(uid).set(data).await()
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        val doc = db.collection(FirestoreKeys.USERS).document(uid).get().await()
        if (!doc.exists()) return null
        return UserProfile(
            uid = uid,
            name = doc.getString(FirestoreKeys.NAME) ?: "",
            age = doc.getLong(FirestoreKeys.AGE)?.toInt() ?: 0,
            weight = doc.getDouble(FirestoreKeys.WEIGHT) ?: 0.0,
            height = doc.getDouble(FirestoreKeys.HEIGHT) ?: 0.0,
            gender = doc.getString(FirestoreKeys.GENDER) ?: "",
            dietId = doc.getString(FirestoreKeys.DIET) ?: ""
        )
    }

    suspend fun updateField(uid: String, field: String, value: Any) {
        db.collection(FirestoreKeys.USERS).document(uid).update(field, value).await()
    }

    suspend fun getAvailableDiets(): List<Diet> {
        val snapshot = db.collection(FirestoreKeys.DIET).get().await()
        return snapshot.documents.map { doc ->
            Diet(
                id = doc.id,
                name = doc.getString(FirestoreKeys.NAME) ?: "",
                calories = doc.getDouble(FirestoreKeys.CALORIES) ?: 0.0,
                proteins = doc.getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
                carbs = doc.getDouble(FirestoreKeys.CARBS) ?: 0.0,
                lipids = doc.getDouble(FirestoreKeys.FATS) ?: 0.0,
                sugar = doc.getDouble(FirestoreKeys.SUGAR) ?: 0.0
            )
        }
    }

    suspend fun getDiet(dietId: String): Diet? {
        val doc = db.collection(FirestoreKeys.DIET).document(dietId).get().await()
        if (!doc.exists()) return null
        return Diet(
            id = doc.id,
            name = doc.getString(FirestoreKeys.NAME) ?: "",
            calories = doc.getDouble(FirestoreKeys.CALORIES) ?: 0.0,
            proteins = doc.getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
            carbs = doc.getDouble(FirestoreKeys.CARBS) ?: 0.0,
            lipids = doc.getDouble(FirestoreKeys.FATS) ?: 0.0,
            sugar = doc.getDouble(FirestoreKeys.SUGAR) ?: 0.0
        )
    }

    suspend fun saveFoodEntryAndAggregate(uid: String, entry: FoodEntry, dateKey: String) {
        val batch = db.batch()
        
        val userRef = db.collection(FirestoreKeys.USERS).document(uid)
        val consumptionRef = userRef.collection(FirestoreKeys.DAILY_CONSUMPTION).document(dateKey)
        val foodsRef = consumptionRef.collection(FirestoreKeys.INGESTED_FOODS).document()

        batch.set(foodsRef, entry)

        val consumptionDoc = consumptionRef.get().await()
        if (consumptionDoc.exists()) {
            val currentCalories = consumptionDoc.getDouble(FirestoreKeys.CALORIES) ?: 0.0
            val currentProteins = consumptionDoc.getDouble(FirestoreKeys.PROTEINS) ?: 0.0
            val currentCarbs = consumptionDoc.getDouble(FirestoreKeys.CARBS) ?: 0.0
            val currentFats = consumptionDoc.getDouble(FirestoreKeys.FATS) ?: 0.0
            
            batch.update(
                consumptionRef,
                mapOf(
                    FirestoreKeys.CALORIES to (currentCalories + entry.calories),
                    FirestoreKeys.PROTEINS to (currentProteins + entry.proteins),
                    FirestoreKeys.CARBS to (currentCarbs + entry.carbs),
                    FirestoreKeys.FATS to (currentFats + entry.fats)
                )
            )
        } else {
            val newData = mapOf(
                FirestoreKeys.CALORIES to entry.calories,
                FirestoreKeys.PROTEINS to entry.proteins,
                FirestoreKeys.CARBS to entry.carbs,
                FirestoreKeys.FATS to entry.fats
            )
            batch.set(consumptionRef, newData)
        }

        batch.commit().await()
    }

    fun observeDailyConsumption(uid: String, dateKey: String): Flow<DailyConsumption> = callbackFlow {
        val ref = db.collection(FirestoreKeys.USERS).document(uid)
            .collection(FirestoreKeys.DAILY_CONSUMPTION).document(dateKey)
        
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(
                    DailyConsumption(
                        date = dateKey,
                        calories = snapshot.getDouble(FirestoreKeys.CALORIES) ?: 0.0,
                        proteins = snapshot.getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
                        carbs = snapshot.getDouble(FirestoreKeys.CARBS) ?: 0.0,
                        fats = snapshot.getDouble(FirestoreKeys.FATS) ?: 0.0
                    )
                )
            } else {
                trySend(DailyConsumption(date = dateKey))
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeConsumptionHistory(uid: String): Flow<Map<String, DailyConsumption>> = callbackFlow {
        val ref = db.collection(FirestoreKeys.USERS).document(uid).collection(FirestoreKeys.DAILY_CONSUMPTION)
        
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val map = mutableMapOf<String, DailyConsumption>()
            snapshot?.documents?.forEach { doc ->
                map[doc.id] = DailyConsumption(
                    date = doc.id,
                    calories = doc.getDouble(FirestoreKeys.CALORIES) ?: 0.0,
                    proteins = doc.getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
                    carbs = doc.getDouble(FirestoreKeys.CARBS) ?: 0.0,
                    fats = doc.getDouble(FirestoreKeys.FATS) ?: 0.0
                )
            }
            trySend(map)
        }
        awaitClose { listener.remove() }
    }
}
