package com.brigadeiro.app.domain.repository

import com.brigadeiro.app.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun observeAll(): Flow<List<Ingredient>>
    suspend fun getAll(): List<Ingredient>
    suspend fun getById(id: Long): Ingredient?
    suspend fun add(ingredient: Ingredient): Long
    suspend fun addAll(ingredients: List<Ingredient>): List<Long>
    suspend fun update(ingredient: Ingredient)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
    suspend fun count(): Int
}
