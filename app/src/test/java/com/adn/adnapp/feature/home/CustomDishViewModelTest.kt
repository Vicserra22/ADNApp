package com.adn.adnapp.feature.home

import androidx.lifecycle.SavedStateHandle
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CustomDishViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val foods = mockk<FoodRepository>()
    private val auth = mockk<AuthRepository>()
    private val carrot = Product("fresh:carrot", "Zanahoria", null, 41.0, .2, .9, 10.0, 4.7)

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { auth.getCurrentUserId() } returns "user"
        every { foods.observeFreshFoods() } returns flowOf(listOf(carrot))
        every { foods.observeFavoriteFoods("user") } returns flowOf(emptyList())
    }

    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun rejectsInvalidDraftBeforeWritingAndPreservesFailedSave() = runTest {
        val vm = CustomDishViewModel(foods, auth, SavedStateHandle())
        vm.saveDish()
        advanceUntilIdle()
        coVerify(exactly = 0) { foods.saveCustomDish(any(), any()) }
        vm.onNameChanged("Ensalada")
        vm.addIngredient(carrot, "100")
        vm.onWeightChanged("100")
        coEvery { foods.saveCustomDish("user", any()) } returns Result.failure(IllegalStateException())
        vm.saveDish()
        advanceUntilIdle()
        assertEquals("Ensalada", vm.uiState.value.draft.name)
        assertEquals(1, vm.uiState.value.draft.ingredients.size)
        assertFalse(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.saved)
        assertNotNull(vm.uiState.value.error)
    }

    @Test fun doubleTapWritesOnceAndRestoresDraftIdentity() = runTest {
        val saved = SavedStateHandle()
        val vm = CustomDishViewModel(foods, auth, saved)
        vm.onNameChanged("Ensalada")
        vm.addIngredient(carrot, "100,5")
        vm.onWeightChanged("100,5")
        coEvery { foods.saveCustomDish("user", any()) } coAnswers { kotlinx.coroutines.delay(100); Result.success(Unit) }
        vm.saveDish()
        vm.saveDish()
        advanceUntilIdle()
        coVerify(exactly = 1) { foods.saveCustomDish("user", match { it.name == "Ensalada" }) }
        assertTrue(vm.uiState.value.saved)
        val restored = CustomDishViewModel(foods, auth, saved)
        assertEquals(vm.uiState.value.draft, restored.uiState.value.draft)
    }
}
