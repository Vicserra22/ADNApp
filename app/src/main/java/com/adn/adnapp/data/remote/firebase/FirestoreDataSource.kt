package com.adn.adnapp.data.remote.firebase

import com.adn.adnapp.core.constants.FirestoreKeys
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.Diet
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.NutritionProfile
import com.adn.adnapp.data.model.entity.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.adn.adnapp.domain.model.BodyGoal
import com.adn.adnapp.domain.model.Importance
import com.adn.adnapp.domain.model.DailyActivityLevel
import com.adn.adnapp.domain.model.MacroTolerance
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
            FirestoreKeys.TARGET_WEIGHT to profile.targetWeight,
            FirestoreKeys.BODY_GOAL to profile.bodyGoal.name,
            FirestoreKeys.NUTRITION_IMPORTANCE to profile.nutritionImportance.name,
            FirestoreKeys.SPORTS_IMPORTANCE to profile.sportsImportance.name,
            FirestoreKeys.GOALS_IMPORTANCE to profile.goalsImportance.name,
            FirestoreKeys.PRIORITIES_COMPLETED to profile.prioritiesCompleted
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
            targetWeight = doc.getDouble(FirestoreKeys.TARGET_WEIGHT) ?: 0.0,
            bodyGoal = doc.getString(FirestoreKeys.BODY_GOAL).enumOr(BodyGoal.MAINTAIN),
            nutritionImportance = doc.getString(FirestoreKeys.NUTRITION_IMPORTANCE).enumOr(Importance.NORMAL),
            sportsImportance = doc.getString(FirestoreKeys.SPORTS_IMPORTANCE).enumOr(Importance.NORMAL),
            goalsImportance = doc.getString(FirestoreKeys.GOALS_IMPORTANCE).enumOr(Importance.NORMAL),
            prioritiesCompleted = doc.getBoolean(FirestoreKeys.PRIORITIES_COMPLETED) ?: false
        )
    }

    suspend fun getNutritionProfile(uid: String): NutritionProfile? {
        val doc = nutritionProfileRef(uid).get().await()
        if (!doc.exists()) return null
        return NutritionProfile(
            dietId = doc.getString(FirestoreKeys.DIET_ID) ?: "",
            onboardingCompleted = doc.getBoolean(FirestoreKeys.ONBOARDING_COMPLETED) ?: false,
            macroTolerance = doc.getString(FirestoreKeys.MACRO_TOLERANCE).enumOr(MacroTolerance.NORMAL)
        )
    }

    suspend fun saveNutritionProfile(uid: String, profile: NutritionProfile) {
        nutritionProfileRef(uid).set(
            mapOf(
                FirestoreKeys.DIET_ID to profile.dietId,
                FirestoreKeys.ONBOARDING_COMPLETED to profile.onboardingCompleted,
                FirestoreKeys.MACRO_TOLERANCE to profile.macroTolerance.name
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun updateField(uid: String, field: String, value: Any) {
        db.collection(FirestoreKeys.USERS).document(uid).update(field, value).await()
    }

    suspend fun getAvailableDiets(): List<Diet> {
        val snapshot = db.collection(FirestoreKeys.DIETS).get().await()
        return snapshot.documents.map { it.toDiet() }
    }

    suspend fun getDiet(dietId: String): Diet? {
        val doc = db.collection(FirestoreKeys.DIETS).document(dietId).get().await()
        if (!doc.exists()) return null
        return doc.toDiet()
    }

    suspend fun getCustomDiets(uid: String): List<Diet> =
        customDietsRef(uid).get().await().documents.map { it.toDiet() }

    suspend fun getCustomDiet(uid: String, dietId: String): Diet? {
        val doc = customDietsRef(uid).document(dietId).get().await()
        return if (doc.exists()) doc.toDiet() else null
    }

    suspend fun saveCustomDiet(uid: String, diet: Diet) {
        customDietsRef(uid).document(diet.id).set(diet.toDietMap()).await()
    }

    suspend fun saveFoodEntryAndAggregate(uid: String, entry: FoodEntry, dateKey: String) {
        val batch = db.batch()
        
        val userRef = db.collection(FirestoreKeys.USERS).document(uid)
        val consumptionRef = userRef.collection(FirestoreKeys.DAILY_CONSUMPTION).document(dateKey)
        val foodsRef = consumptionRef.collection(FirestoreKeys.INGESTED_FOODS).document()
        val storedEntry = entry.copy(id = foodsRef.id)

        batch.set(foodsRef, storedEntry.toFirestoreMap())

        batch.set(
            consumptionRef,
            mapOf(
                FirestoreKeys.CALORIES to FieldValue.increment(storedEntry.calories),
                FirestoreKeys.PROTEINS to FieldValue.increment(storedEntry.proteins),
                FirestoreKeys.CARBS to FieldValue.increment(storedEntry.carbs),
                FirestoreKeys.FATS to FieldValue.increment(storedEntry.fats),
                FirestoreKeys.SUGAR to FieldValue.increment(storedEntry.sugar),
                FirestoreKeys.WATER_ML to FieldValue.increment(storedEntry.waterMl)
            ),
            SetOptions.merge()
        )

        batch.commit().await()
    }

    suspend fun saveWaterEntryAndAggregate(uid: String, dateKey: String, amountMl: Double): String {
        val batch = db.batch()
        val userRef = db.collection(FirestoreKeys.USERS).document(uid)
        val consumptionRef = userRef.collection(FirestoreKeys.DAILY_CONSUMPTION).document(dateKey)
        val foodsRef = consumptionRef.collection(FirestoreKeys.INGESTED_FOODS).document()
        val entry = FoodEntry(
            id = foodsRef.id,
            name = "Agua",
            quantity = amountMl,
            waterMl = amountMl,
            kind = FoodEntry.KIND_WATER
        )
        batch.set(foodsRef, entry.toFirestoreMap())
        batch.set(
            consumptionRef,
            mapOf(FirestoreKeys.WATER_ML to FieldValue.increment(amountMl)),
            SetOptions.merge()
        )
        batch.commit().await()
        return foodsRef.id
    }

    suspend fun updateFoodEntryAndAggregate(uid: String, entry: FoodEntry, dateKey: String) {
        require(entry.id.isNotBlank()) { "La entrada no tiene identificador" }
        val consumptionRef = dailyConsumptionRef(uid, dateKey)
        val entryRef = consumptionRef.collection(FirestoreKeys.INGESTED_FOODS).document(entry.id)
        db.runTransaction { transaction ->
            val previous = transaction.get(entryRef)
            require(previous.exists()) { "La entrada ya no existe" }
            val oldEntry = previous.toFoodEntry()
            transaction.set(entryRef, entry.toFirestoreMap())
            transaction.set(consumptionRef, aggregateDelta(entry, oldEntry), SetOptions.merge())
        }.await()
    }

    suspend fun deleteFoodEntryAndAggregate(uid: String, entryId: String, dateKey: String) {
        require(entryId.isNotBlank()) { "La entrada no tiene identificador" }
        val consumptionRef = dailyConsumptionRef(uid, dateKey)
        val entryRef = consumptionRef.collection(FirestoreKeys.INGESTED_FOODS).document(entryId)
        db.runTransaction { transaction ->
            val previous = transaction.get(entryRef)
            require(previous.exists()) { "La entrada ya no existe" }
            val oldEntry = previous.toFoodEntry()
            transaction.delete(entryRef)
            transaction.set(consumptionRef, aggregateDelta(FoodEntry(), oldEntry), SetOptions.merge())
        }.await()
    }

    suspend fun setDailyActivityLevel(uid: String, dateKey: String, level: DailyActivityLevel) {
        dailyConsumptionRef(uid, dateKey).set(
            mapOf(FirestoreKeys.ACTIVITY_LEVEL to level.name),
            SetOptions.merge()
        ).await()
    }

    fun observeFoodEntries(uid: String, dateKey: String): Flow<List<FoodEntry>> = callbackFlow {
        val ref = dailyConsumptionRef(uid, dateKey).collection(FirestoreKeys.INGESTED_FOODS)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.toFoodEntry() }?.sortedByDescending { it.timestamp }.orEmpty())
        }
        awaitClose { listener.remove() }
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
                        fats = snapshot.getDouble(FirestoreKeys.FATS) ?: 0.0,
                        sugar = snapshot.getDouble(FirestoreKeys.SUGAR) ?: 0.0,
                        waterMl = snapshot.getDouble(FirestoreKeys.WATER_ML) ?: 0.0,
                        activityLevel = snapshot.getString(FirestoreKeys.ACTIVITY_LEVEL)
                            .enumOr(DailyActivityLevel.LIGHT)
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
                    fats = doc.getDouble(FirestoreKeys.FATS) ?: 0.0,
                    sugar = doc.getDouble(FirestoreKeys.SUGAR) ?: 0.0,
                    waterMl = doc.getDouble(FirestoreKeys.WATER_ML) ?: 0.0,
                    activityLevel = doc.getString(FirestoreKeys.ACTIVITY_LEVEL)
                        .enumOr(DailyActivityLevel.LIGHT)
                )
            }
            trySend(map)
        }
        awaitClose { listener.remove() }
    }

    private fun nutritionProfileRef(uid: String) = db.collection(FirestoreKeys.USERS)
        .document(uid)
        .collection(FirestoreKeys.AREAS)
        .document(FirestoreKeys.NUTRITION)

    private fun dailyConsumptionRef(uid: String, dateKey: String) = db.collection(FirestoreKeys.USERS)
        .document(uid).collection(FirestoreKeys.DAILY_CONSUMPTION).document(dateKey)

    private fun customDietsRef(uid: String) = db.collection(FirestoreKeys.USERS)
        .document(uid).collection(FirestoreKeys.CUSTOM_DIETS)
}

private fun com.google.firebase.firestore.DocumentSnapshot.toDiet() = Diet(
    id = id,
    name = getString(FirestoreKeys.NAME) ?: "",
    description = getString(FirestoreKeys.DESCRIPTION) ?: "",
    imageUrl = getString(FirestoreKeys.IMAGE_URL) ?: "",
    calories = getDouble(FirestoreKeys.CALORIES) ?: 0.0,
    proteins = getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
    carbs = getDouble(FirestoreKeys.CARBS) ?: 0.0,
    lipids = getDouble(FirestoreKeys.FATS) ?: 0.0,
    sugar = getDouble(FirestoreKeys.SUGAR) ?: 0.0,
    water = getDouble(FirestoreKeys.WATER_ML) ?: 0.0
)

private fun Diet.toDietMap(): Map<String, Any> = mapOf(
    FirestoreKeys.NAME to name,
    FirestoreKeys.DESCRIPTION to description,
    FirestoreKeys.IMAGE_URL to imageUrl,
    FirestoreKeys.CALORIES to calories,
    FirestoreKeys.PROTEINS to proteins,
    FirestoreKeys.CARBS to carbs,
    FirestoreKeys.FATS to lipids,
    FirestoreKeys.SUGAR to sugar,
    FirestoreKeys.WATER_ML to water
)

private fun FoodEntry.toFirestoreMap(): Map<String, Any> = mapOf(
    "id" to id,
    FirestoreKeys.PRODUCT_ID to productId,
    FirestoreKeys.NAME to name,
    FirestoreKeys.QUANTITY to quantity,
    FirestoreKeys.CALORIES to calories,
    FirestoreKeys.PROTEINS to proteins,
    FirestoreKeys.CARBS to carbs,
    FirestoreKeys.FATS to fats,
    FirestoreKeys.SUGAR to sugar,
    FirestoreKeys.WATER_ML to waterMl,
    FirestoreKeys.KIND to kind,
    FirestoreKeys.TIMESTAMP to timestamp
)

private fun com.google.firebase.firestore.DocumentSnapshot.toFoodEntry() = FoodEntry(
    id = id,
    productId = getString(FirestoreKeys.PRODUCT_ID) ?: "",
    name = getString(FirestoreKeys.NAME) ?: "Alimento",
    quantity = getDouble(FirestoreKeys.QUANTITY) ?: 0.0,
    calories = getDouble(FirestoreKeys.CALORIES) ?: 0.0,
    proteins = getDouble(FirestoreKeys.PROTEINS) ?: 0.0,
    carbs = getDouble(FirestoreKeys.CARBS) ?: 0.0,
    fats = getDouble(FirestoreKeys.FATS) ?: 0.0,
    sugar = getDouble(FirestoreKeys.SUGAR) ?: 0.0,
    waterMl = getDouble(FirestoreKeys.WATER_ML) ?: 0.0,
    kind = getString(FirestoreKeys.KIND) ?: FoodEntry.KIND_FOOD,
    timestamp = getLong(FirestoreKeys.TIMESTAMP) ?: 0L
)

private fun aggregateDelta(new: FoodEntry, old: FoodEntry): Map<String, Any> = mapOf(
    FirestoreKeys.CALORIES to FieldValue.increment(new.calories - old.calories),
    FirestoreKeys.PROTEINS to FieldValue.increment(new.proteins - old.proteins),
    FirestoreKeys.CARBS to FieldValue.increment(new.carbs - old.carbs),
    FirestoreKeys.FATS to FieldValue.increment(new.fats - old.fats),
    FirestoreKeys.SUGAR to FieldValue.increment(new.sugar - old.sugar),
    FirestoreKeys.WATER_ML to FieldValue.increment(new.waterMl - old.waterMl)
)

private inline fun <reified T : Enum<T>> String?.enumOr(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default
