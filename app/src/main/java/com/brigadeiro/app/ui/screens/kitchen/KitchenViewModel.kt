package com.brigadeiro.app.ui.screens.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.domain.TimeProvider
import com.brigadeiro.app.domain.model.Ingredient
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.diet.DietPolicy
import com.brigadeiro.app.domain.model.TextNormalizer
import com.brigadeiro.app.domain.repository.DietRepository
import com.brigadeiro.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class KitchenUiState(
    val loading: Boolean = true,
    val all: List<Ingredient> = emptyList(),
    val visible: List<Ingredient> = emptyList(),
    val query: String = "",
    val category: IngredientCategory? = null,
    val conflictingIds: Set<Long> = emptySet(),
)

class KitchenViewModel(
    private val repository: IngredientRepository,
    private val dietRepository: DietRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<IngredientCategory?>(null)

    val state: StateFlow<KitchenUiState> = combine(
        repository.observeAll(),
        query,
        category,
        dietRepository.profile,
    ) { all, q, cat, profile ->
        val normalizedQuery = TextNormalizer.normalize(q)
        val visible = all.filter { ingredient ->
            val matchesQuery = normalizedQuery.isBlank() ||
                ingredient.normalizedName.contains(normalizedQuery) ||
                TextNormalizer.normalize(ingredient.name).contains(normalizedQuery)
            val matchesCategory = cat == null || ingredient.category == cat
            matchesQuery && matchesCategory
        }
        val conflicting = all
            .filterNot { DietPolicy.isAllowed(it.name, profile) }
            .map { it.id }
            .toSet()
        KitchenUiState(
            loading = false,
            all = all,
            visible = visible,
            query = q,
            category = cat,
            conflictingIds = conflicting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = KitchenUiState(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setCategory(value: IngredientCategory?) {
        category.value = value
    }

    fun addManual(
        name: String,
        quantity: Double?,
        unit: String?,
        category: IngredientCategory,
    ) {
        val trimmed = name.trim()
        if (trimmed.length < 2) return
        val now = timeProvider.now()
        viewModelScope.launch {
            repository.add(
                Ingredient(
                    name = trimmed,
                    normalizedName = TextNormalizer.normalize(trimmed),
                    quantity = quantity,
                    unit = unit?.trim()?.takeIf { it.isNotEmpty() },
                    category = category,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun update(ingredient: Ingredient) {
        viewModelScope.launch {
            repository.update(ingredient.copy(updatedAt = timeProvider.now()))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
