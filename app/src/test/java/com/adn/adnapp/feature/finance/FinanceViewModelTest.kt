package com.adn.adnapp.feature.finance

import com.adn.adnapp.data.local.FinanceStore
import com.adn.adnapp.domain.model.MoneyKind
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = mockk<FinanceStore>(relaxed = true)
    private val data = MutableStateFlow(com.adn.adnapp.domain.model.FinanceData())
    @Before fun setUp() { Dispatchers.setMain(dispatcher); every { store.data } returns data }
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun expenseAndFutureFlagsAreRecordedSeparately() = runTest {
        val viewModel = FinanceViewModel(store)
        viewModel.selectKind(MoneyKind.EXPENSE); viewModel.onTitleChanged("Alquiler"); viewModel.onAmountChanged("1100"); viewModel.addEntry()
        viewModel.onTitleChanged("Ocio previsto"); viewModel.onAmountChanged("450"); viewModel.setPlanned(true); viewModel.addEntry()
        verify { store.saveEntry(match { it.title == "Alquiler" && it.amountCents == 110000L && !it.planned }) }
        verify { store.saveEntry(match { it.title == "Ocio previsto" && it.amountCents == 45000L && it.planned }) }
        assertEquals("Movimiento guardado", viewModel.uiState.value.message)
    }

    @Test fun incomeIsSavedAsPositiveCents() = runTest {
        val viewModel = FinanceViewModel(store)
        viewModel.selectKind(MoneyKind.INCOME); viewModel.onTitleChanged("Nómina"); viewModel.onAmountChanged("2200,50"); viewModel.addEntry()
        verify { store.saveEntry(match { it.kind == MoneyKind.INCOME && it.amountCents == 220050L }) }
    }

    @Test fun goalAndInvestmentNeedCompleteNumbers() = runTest {
        val viewModel = FinanceViewModel(store)
        viewModel.onGoalNameChanged("Colchón"); viewModel.onGoalTargetChanged("1200"); viewModel.onGoalSavedChanged("300"); viewModel.addGoal()
        verify { store.saveGoal(match { it.name == "Colchón" && it.targetCents == 120000L && it.savedCents == 30000L }) }
        viewModel.onInvestmentNameChanged("Fondo indexado"); viewModel.onUnitsChanged("2.5"); viewModel.onAveragePriceChanged("100"); viewModel.onCurrentPriceChanged("105"); viewModel.addInvestment()
        verify { store.saveInvestment(match { it.name == "Fondo indexado" && it.units == 2.5 && it.currentPriceCents == 10500L }) }
    }

    @Test fun blankMovementExplainsTheMissingConcept() = runTest {
        val viewModel = FinanceViewModel(store); viewModel.onAmountChanged("20"); viewModel.addEntry()
        assertTrue(viewModel.uiState.value.error!!.contains("concepto"))
        verify(exactly = 0) { store.saveEntry(any()) }
    }
}
