package com.brigadeiro.app.domain

import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeIngredientRepository : IngredientRepository {

    private val items = MutableStateFlow<List<Ingredient>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<Ingredient>> =
        items.asStateFlow().map { list -> list.sortedBy { it.name } }

    override suspend fun getAll(): List<Ingredient> = items.value

    override suspend fun getById(id: Long): Ingredient? = items.value.firstOrNull { it.id == id }

    override suspend fun add(ingredient: Ingredient): Long {
        val id = nextId++
        items.value = items.value + ingredient.copy(id = id)
        return id
    }

    override suspend fun addAll(ingredients: List<Ingredient>): List<Long> =
        ingredients.map { add(it) }

    override suspend fun update(ingredient: Ingredient) {
        items.value = items.value.map { if (it.id == ingredient.id) ingredient else it }
    }

    override suspend fun delete(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        items.value = emptyList()
    }

    override suspend fun count(): Int = items.value.size
}
