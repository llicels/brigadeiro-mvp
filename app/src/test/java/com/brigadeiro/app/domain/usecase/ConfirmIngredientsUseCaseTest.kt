package com.brigadeiro.app.domain.usecase

import com.brigadeiro.app.domain.FakeIngredientRepository
import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.IngredientDraft
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ConfirmIngredientsUseCaseTest {

    private val repository = FakeIngredientRepository()
    private val useCase = ConfirmIngredientsUseCase(repository, TimeProvider { 1_000L })

    @Test
    fun insertsNewIngredients() = runTest {
        val inserted = useCase(
            listOf(
                IngredientDraft("tomate", 2.0, "un", IngredientCategory.Produce),
                IngredientDraft("arroz", null, null, IngredientCategory.Grains),
            ),
        )

        assertThat(inserted).isEqualTo(2)
        assertThat(repository.getAll().map { it.name }).containsExactly("tomate", "arroz")
    }

    @Test
    fun mergesWithExistingByName() = runTest {
        useCase(listOf(IngredientDraft("tomate", 2.0, "un", IngredientCategory.Produce)))
        useCase(listOf(IngredientDraft("Tomates", 3.0, "un", IngredientCategory.Produce)))

        val all = repository.getAll()
        assertThat(all).hasSize(1)
        assertThat(all.first().quantity).isEqualTo(5.0)
    }

    @Test
    fun ignoresDraftsWithInvalidNames() = runTest {
        val inserted = useCase(listOf(IngredientDraft(""), IngredientDraft("a")))

        assertThat(inserted).isEqualTo(1)
    }
}
