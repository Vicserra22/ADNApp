package com.adn.adnapp.data.local

import com.adn.adnapp.domain.model.AppArea
import org.junit.Assert.assertEquals
import org.junit.Test

class AreaCycleTest {
    @Test fun customCycle_respectsOrderAndWraps() {
        val cycle = listOf(AppArea.FINANCE, AppArea.NUTRITION, AppArea.SPORTS)
        assertEquals(AppArea.NUTRITION, nextArea(AppArea.FINANCE, cycle))
        assertEquals(AppArea.SPORTS, nextArea(AppArea.NUTRITION, cycle))
        assertEquals(AppArea.FINANCE, nextArea(AppArea.SPORTS, cycle))
        assertEquals(AppArea.FINANCE, nextArea(AppArea.AGENDA, cycle))
    }
}

