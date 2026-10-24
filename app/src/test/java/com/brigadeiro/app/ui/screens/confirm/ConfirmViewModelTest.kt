package com.brigadeiro.app.ui.screens.confirm

import androidx.lifecycle.SavedStateHandle
import com.brigadeiro.app.MainDispatcherRule
import com.brigadeiro.app.ai.extractor.HeuristicIngredientExtractor
import com.brigadeiro.app.domain.FakeIngredientRepository
import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.usecase.ConfirmIngredientsUseCase
import com.brigadeiro.app.domain.usecase.ParseIngredientsUseCase
import com.brigadeiro.app.ui.navigation.Routes
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeIngredientRepository()

    private fun viewModel(text: String) = ConfirmViewModel(
        parseIngredients = ParseIngredientsUseCase(HeuristicIngredientExtractor()),
        confirmIngredients = ConfirmIngredientsUseCase(repository, TimeProvider { 1L }),
        savedStateHandle = SavedStateHandle(mapOf(Routes.ARG_TEXT to text)),
    )

    @Test
    fun parsesTextIntoDrafts() = runTest {
        val vm = viewModel("tenho arroz, dois tomates e queijo")
        advanceUntilIdle()

        val state = vm.state.value
        assertThat(state.loading).isFalse()
        assertThat(state.drafts.map { it.name }).containsExactly("arroz", "tomate", "queijo")
    }

    @Test
    fun editingAndRemovingDraftsWorks() = runTest {
        val vm = viewModel("tenho arroz e tomate")
        advanceUntilIdle()

        vm.updateName(0, "arroz integral")
        vm.remove(1)
        advanceUntilIdle()

        assertThat(vm.state.value.drafts).hasSize(1)
        assertThat(vm.state.value.drafts.first().name).isEqualTo("arroz integral")
    }

    @Test
    fun confirmPersistsAndSignalsSaved() = runTest {
        val vm = viewModel("tenho arroz e tomate")
        advanceUntilIdle()

        vm.confirm()
        advanceUntilIdle()

        assertThat(vm.state.value.saved).isTrue()
        assertThat(repository.getAll()).hasSize(2)
    }
}
