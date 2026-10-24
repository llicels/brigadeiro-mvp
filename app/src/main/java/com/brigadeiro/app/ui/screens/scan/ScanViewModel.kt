package com.brigadeiro.app.ui.screens.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brigadeiro.app.ai.receipt.ReceiptOcr
import com.brigadeiro.app.ai.receipt.ReceiptParser
import com.brigadeiro.app.domain.model.IngredientCategory
import com.brigadeiro.app.domain.model.IngredientDraft
import com.brigadeiro.app.domain.usecase.ConfirmIngredientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanPhase { Choosing, Processing, Review }

data class ScanUiState(
    val phase: ScanPhase = ScanPhase.Choosing,
    val drafts: List<IngredientDraft> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
) {
    val itemCount: Int get() = drafts.size
}

class ScanViewModel(
    private val confirmIngredients: ConfirmIngredientsUseCase,
    private val ocr: ReceiptOcr = ReceiptOcr(),
    private val parser: ReceiptParser = ReceiptParser,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    fun process(context: Context, uri: Uri) {
        _state.update { it.copy(phase = ScanPhase.Processing, error = null) }
        viewModelScope.launch {
            runCatching {
                val lines = ocr.recognize(context, uri)
                parser.parse(lines)
            }.onSuccess { drafts ->
                _state.update {
                    if (drafts.isEmpty()) {
                        it.copy(
                            phase = ScanPhase.Choosing,
                            error = "Não encontrei itens de comida nessa nota. " +
                                "Tente uma foto mais nítida ou adicione manualmente.",
                        )
                    } else {
                        it.copy(phase = ScanPhase.Review, drafts = drafts, error = null)
                    }
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        phase = ScanPhase.Choosing,
                        error = "Não consegui ler a nota. Tente novamente ou adicione manualmente.",
                    )
                }
            }
        }
    }

    fun retry() {
        _state.update { it.copy(phase = ScanPhase.Choosing, error = null, drafts = emptyList()) }
    }

    fun updateName(index: Int, name: String) = updateDraft(index) { it.copy(name = name) }

    fun updateQuantity(index: Int, raw: String) = updateDraft(index) {
        it.copy(quantity = raw.replace(',', '.').trim().toDoubleOrNull())
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
        _state.update {
            it.copy(drafts = it.drafts + IngredientDraft(name = "", category = IngredientCategory.Others))
        }
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
