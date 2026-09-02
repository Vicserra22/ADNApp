package com.adn.adnapp.feature.home

import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.DietRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val foods = mockk<FoodRepository>()
    private val auth = mockk<AuthRepository>()
    private val users = mockk<UserRepository>()
    private val nutrition = mockk<NutritionRepository>()
    private val diets = mockk<DietRepository>()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "uid"
        every { foods.observeDailyConsumption(eq("uid"), any()) } returns flowOf(DailyConsumption("today"))
        every { foods.observeFreshFoods() } returns flowOf(emptyList())
        every { foods.observeRecentFoods("uid") } returns flowOf(emptyList())
        every { foods.observeFavoriteFoods("uid") } returns flowOf(emptyList())
        coEvery { users.getUserProfile("uid") } returns Result.success(null)
        coEvery { nutrition.getNutritionProfile("uid") } returns Result.success(null)
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun firstScreenManualForm_savesAnIndependentEntry() = runTest {
        coEvery { foods.saveFoodEntry(eq("uid"), any(), any()) } returns Result.success(Unit)
        val viewModel = HomeViewModel(foods, auth, users, nutrition, diets)
        advanceUntilIdle()

        viewModel.onManualNameChanged("Cena manual")
        viewModel.onManualCaloriesChanged("520")
        viewModel.onManualProteinsChanged("31")
        viewModel.saveManualEntry()
        advanceUntilIdle()

        coVerify {
            foods.saveFoodEntry(
                "uid",
                match { it.kind == FoodEntry.KIND_MANUAL && it.name == "Cena manual" && it.calories == 520.0 },
                any()
            )
        }
        assertEquals("Datos añadidos al día de hoy", viewModel.uiState.value.successMessage)
    }

    @Test fun scannedBarcode_opensTheFoundProduct() = runTest {
        val product = Product("8410000000000", "Yogur", null, 60.0, 3.0, 4.0, 5.0, 4.0)
        coEvery { foods.getFoodByBarcode("8410000000000") } returns Result.success(product)
        val viewModel = HomeViewModel(foods, auth, users, nutrition, diets)
        advanceUntilIdle()

        viewModel.onBarcodeScanned("8410000000000")
        advanceUntilIdle()

        assertEquals(product, viewModel.uiState.value.selectedProduct)
    }
}
