package com.brigadeiro.app.ui.screens.confirm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.usecase.ConfirmIngredientsUseCase
import com.brigadeiro.app.domain.usecase.ParseIngredientsUseCase
import com.brigadeiro.app.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmUiState(
    val loading: Boolean = true,
    val sourceText: String = "",
    val drafts: List<IngredientDraft> = emptyList(),
    val error: Boolean = false,
    val saved: Boolean = false,
)

class ConfirmViewModel(
    private val parseIngredients: ParseIngredientsUseCase,
    private val confirmIngredients: ConfirmIngredientsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val text: String = savedStateHandle.get<String>(Routes.ARG_TEXT).orEmpty()

    private val _state = MutableStateFlow(ConfirmUiState(sourceText = text))
    val state: StateFlow<ConfirmUiState> = _state.asStateFlow()

    init {
        parse()
    }

    fun parse() {
        _state.update { it.copy(loading = true, error = false) }
        viewModelScope.launch {
            val result = parseIngredients(text)
            _state.update {
                it.copy(
                    loading = false,
                    drafts = result.drafts,
                    error = false,
                )
            }
        }
    }

    fun updateName(index: Int, name: String) = updateDraft(index) { it.copy(name = name) }

    fun updateQuantity(index: Int, raw: String) = updateDraft(index) {
        val parsed = raw.replace(',', '.').trim().toDoubleOrNull()
        it.copy(quantity = parsed)
    }

    fun updateUnit(index: Int, unit: String) = updateDraft(index) { it.copy(unit = unit) }

    fun remove(index: Int) {
        _state.update { current ->
            val mutable = current.drafts.toMutableList()
            if (index in mutable.indices) mutable.removeAt(index)
            current.copy(drafts = mutable)
        }
    }

    fun addEmpty() {
        _state.update { it.copy(drafts = it.drafts + IngredientDraft(name = "")) }
    }

    fun confirm() {
        val drafts = _state.value.drafts.filter { it.name.trim().length >= 2 }
        if (drafts.isEmpty()) return
        viewModelScope.launch {
            confirmIngredients(drafts)
            _state.update { it.copy(saved = true) }
        }
    }

    private fun updateDraft(index: Int, transform: (IngredientDraft) -> IngredientDraft) {
        _state.update { current ->
            if (index !in current.drafts.indices) return@update current
            val mutable = current.drafts.toMutableList()
            mutable[index] = transform(mutable[index])
            current.copy(drafts = mutable)
        }
    }
}
