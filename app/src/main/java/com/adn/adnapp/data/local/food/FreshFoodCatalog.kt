package com.adn.adnapp.data.local.food

import com.adn.adnapp.data.model.entity.Product

/**
 * Catálogo básico sin marcas. Valores orientativos por 100 g de porción comestible;
 * deben mostrarse como estimaciones, ya que variedad y cocinado cambian la composición.
 */
object FreshFoodCatalog {
    val products: List<Product> = listOf(
        food("pechuga-pollo", "Pechuga de pollo", 120.0, 22.5, 0.0, 2.6, 0.0, "Carnes"),
        food("pavo", "Pechuga de pavo", 105.0, 24.0, 0.0, 1.0, 0.0, "Carnes"),
        food("ternera-magra", "Ternera magra", 131.0, 21.0, 0.0, 5.0, 0.0, "Carnes"),
        food("lomo-cerdo", "Lomo de cerdo", 143.0, 21.0, 0.0, 6.5, 0.0, "Carnes"),
        food("conejo", "Conejo", 133.0, 22.0, 0.0, 4.6, 0.0, "Carnes"),
        food("salmon", "Salmón", 208.0, 20.0, 0.0, 13.0, 0.0, "Pescados"),
        food("merluza", "Merluza", 86.0, 18.0, 0.0, 1.8, 0.0, "Pescados"),
        food("atun", "Atún fresco", 144.0, 23.0, 0.0, 4.9, 0.0, "Pescados"),
        food("sardina", "Sardina", 208.0, 25.0, 0.0, 11.5, 0.0, "Pescados"),
        food("bacalao", "Bacalao fresco", 82.0, 18.0, 0.0, 0.7, 0.0, "Pescados"),
        food("huevo", "Huevo de gallina", 143.0, 12.6, 0.7, 9.5, 0.4, "Huevos"),
        food("leche", "Leche semidesnatada", 46.0, 3.2, 4.8, 1.6, 4.8, "Lácteos"),
        food("yogur", "Yogur natural", 61.0, 3.5, 4.7, 3.3, 4.7, "Lácteos"),
        food("queso-fresco", "Queso fresco", 174.0, 12.4, 3.0, 12.0, 3.0, "Lácteos"),
        food("brocoli", "Brócoli", 34.0, 2.8, 6.6, 0.4, 1.7, "Verduras"),
        food("espinaca", "Espinaca", 23.0, 2.9, 3.6, 0.4, 0.4, "Verduras"),
        food("tomate", "Tomate", 18.0, 0.9, 3.9, 0.2, 2.6, "Verduras"),
        food("zanahoria", "Zanahoria", 41.0, 0.9, 9.6, 0.2, 4.7, "Verduras"),
        food("calabacin", "Calabacín", 17.0, 1.2, 3.1, 0.3, 2.5, "Verduras"),
        food("pimiento-rojo", "Pimiento rojo", 31.0, 1.0, 6.0, 0.3, 4.2, "Verduras"),
        food("manzana", "Manzana", 52.0, 0.3, 13.8, 0.2, 10.4, "Frutas"),
        food("platano", "Plátano", 89.0, 1.1, 22.8, 0.3, 12.2, "Frutas"),
        food("naranja", "Naranja", 47.0, 0.9, 11.8, 0.1, 9.4, "Frutas"),
        food("pera", "Pera", 57.0, 0.4, 15.2, 0.1, 9.8, "Frutas"),
        food("fresa", "Fresa", 32.0, 0.7, 7.7, 0.3, 4.9, "Frutas"),
        food("aguacate", "Aguacate", 160.0, 2.0, 8.5, 14.7, 0.7, "Frutas"),
        food("lenteja-cocida", "Lenteja cocida", 116.0, 9.0, 20.1, 0.4, 1.8, "Legumbres"),
        food("garbanzo-cocido", "Garbanzo cocido", 164.0, 8.9, 27.4, 2.6, 4.8, "Legumbres"),
        food("alubia-cocida", "Alubia cocida", 127.0, 8.7, 22.8, 0.5, 0.3, "Legumbres"),
        food("guisante", "Guisante", 81.0, 5.4, 14.5, 0.4, 5.7, "Legumbres"),
        food("arroz-integral", "Arroz integral cocido", 123.0, 2.7, 25.6, 1.0, 0.2, "Cereales"),
        food("avena", "Copos de avena", 379.0, 13.2, 67.7, 6.5, 1.0, "Cereales"),
        food("quinoa", "Quinoa cocida", 120.0, 4.4, 21.3, 1.9, 0.9, "Cereales"),
        food("pan-integral", "Pan integral", 247.0, 13.0, 41.0, 3.4, 6.0, "Cereales"),
        food("almendra", "Almendra", 579.0, 21.2, 21.6, 49.9, 4.4, "Frutos secos"),
        food("nuez", "Nuez", 654.0, 15.2, 13.7, 65.2, 2.6, "Frutos secos"),
        food("pistacho", "Pistacho", 562.0, 20.2, 27.2, 45.3, 7.7, "Frutos secos")
    )

    fun entities(updatedAt: Long = System.currentTimeMillis()): List<ProductCacheEntity> = products.map {
        ProductCacheMapper.toEntity(
            product = it,
            source = FoodSource.LOCAL_FRESH,
            sourceId = it.code.removePrefix("fresh:"),
            barcode = null,
            updatedAt = updatedAt
        )
    }

    private fun food(
        id: String,
        name: String,
        calories: Double,
        proteins: Double,
        carbs: Double,
        fats: Double,
        sugars: Double,
        category: String
    ) = Product(
        code = "fresh:$id",
        name = name,
        imageUrl = null,
        calories = calories,
        fats = fats,
        proteins = proteins,
        carbs = carbs,
        sugars = sugars,
        brands = "Alimento fresco",
        quantity = "100 g",
        servingSize = "100 g",
        categories = listOf(category, "Alimentos frescos")
    )
}
