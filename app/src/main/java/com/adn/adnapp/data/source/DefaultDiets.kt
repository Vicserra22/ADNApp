package com.adn.adnapp.data.source

import com.adn.adnapp.data.model.entity.Diet

object DefaultDiets {
    val values = listOf(
        Diet(
            id = "balanced",
            name = "Equilibrada",
            description = "Una distribución variada para el día a día.",
            proteins = 120.0,
            carbs = 250.0,
            lipids = 70.0,
            calories = 2_100.0, sugar = 50.0, water = 2_000.0
        ),
        Diet(
            id = "high_protein",
            name = "Alta en proteína",
            description = "Pensada para apoyar fuerza y recuperación.",
            proteins = 165.0,
            carbs = 220.0,
            lipids = 70.0,
            calories = 2_170.0, sugar = 45.0, water = 2_300.0
        ),
        Diet(
            id = "vegetarian",
            name = "Vegetariana",
            description = "Fuentes vegetales, huevos y lácteos.",
            proteins = 115.0,
            carbs = 270.0,
            lipids = 65.0,
            calories = 2_125.0, sugar = 50.0, water = 2_000.0
        ),
        Diet(
            id = "low_carb",
            name = "Baja en carbohidratos",
            description = "Menos carbohidratos y mayor aporte de grasas saludables.",
            proteins = 145.0,
            carbs = 120.0,
            lipids = 105.0,
            calories = 2_005.0, sugar = 40.0, water = 2_200.0
        )
    )
}
