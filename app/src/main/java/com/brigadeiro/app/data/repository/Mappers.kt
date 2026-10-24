package com.brigadeiro.app.data.repository

import com.brigadeiro.app.data.database.IngredientEntity
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory

internal fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    id = id,
    name = name,
    normalizedName = normalizedName,
    quantity = quantity,
    unit = unit,
    category = IngredientCategory.fromLabel(category),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun Ingredient.toEntity(): IngredientEntity = IngredientEntity(
    id = id,
    name = name,
    normalizedName = normalizedName,
    quantity = quantity,
    unit = unit,
    category = category.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
