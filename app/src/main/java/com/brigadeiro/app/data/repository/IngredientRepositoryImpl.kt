package com.brigadeiro.app.data.repository

import com.brigadeiro.app.data.database.IngredientDao
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepositoryImpl(
    private val dao: IngredientDao,
) : IngredientRepository {

    override fun observeAll(): Flow<List<Ingredient>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAll(): List<Ingredient> = dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Ingredient? = dao.getById(id)?.toDomain()

    override suspend fun add(ingredient: Ingredient): Long = dao.insert(ingredient.toEntity())

    override suspend fun addAll(ingredients: List<Ingredient>): List<Long> =
        dao.insertAll(ingredients.map { it.toEntity() })

    override suspend fun update(ingredient: Ingredient) = dao.update(ingredient.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun count(): Int = dao.count()
}
