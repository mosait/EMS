package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.ErsthelferMassnahmen
import com.mosait.ems.core.model.Measures
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MeasuresUiState(
    val selectedMeasures: List<String> = emptyList(),
    val sonstigesTexte: Map<String, String> = emptyMap(),
    val sauerstoffText: String = "",
    val medikamente: String = "",
    val sonstige: String = "",
    val ersthelferSuffizient: Boolean = false,
    val ersthelferInsuffizient: Boolean = false,
    val ersthelferAed: Boolean = false,
    val ersthelferKeine: Boolean = false
)

@HiltViewModel
class MeasuresViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(MeasuresUiState())
    val uiState: StateFlow<MeasuresUiState> = _uiState.asStateFlow()

    private var _initialState: MeasuresUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val measures = protocolRepository.getMeasures(patientId).firstOrNull()
            if (measures != null) {
                _uiState.value = MeasuresUiState(
                    selectedMeasures = measures.selectedMeasures,
                    sonstigesTexte = measures.sonstigesTexte,
                    sauerstoffText = measures.sauerstoffLiterProMin?.toString() ?: "",
                    medikamente = measures.medikamente,
                    sonstige = measures.sonstige,
                    ersthelferSuffizient = measures.ersthelferMassnahmen.suffizient,
                    ersthelferInsuffizient = measures.ersthelferMassnahmen.insuffizient,
                    ersthelferAed = measures.ersthelferMassnahmen.aed,
                    ersthelferKeine = measures.ersthelferMassnahmen.keine
                )
            }
            _initialState = _uiState.value
        }
    }

    fun updateSelectedMeasures(measures: List<String>) = _uiState.update { it.copy(selectedMeasures = measures) }
    fun updateSonstigesText(category: String, text: String) = _uiState.update {
        it.copy(sonstigesTexte = it.sonstigesTexte + (category to text))
    }
    fun updateSauerstoff(value: String) = _uiState.update { it.copy(sauerstoffText = value) }
    fun updateMedikamente(value: String) = _uiState.update { it.copy(medikamente = value) }
    fun updateSonstige(value: String) = _uiState.update { it.copy(sonstige = value) }
    fun toggleErsthelferSuffizient() = _uiState.update { it.copy(ersthelferSuffizient = !it.ersthelferSuffizient) }
    fun toggleErsthelferInsuffizient() = _uiState.update { it.copy(ersthelferInsuffizient = !it.ersthelferInsuffizient) }
    fun toggleErsthelferAed() = _uiState.update { it.copy(ersthelferAed = !it.ersthelferAed) }
    fun toggleErsthelferKeine() = _uiState.update { it.copy(ersthelferKeine = !it.ersthelferKeine) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveMeasures(
                Measures(
                    patientId = patientId,
                    selectedMeasures = state.selectedMeasures,
                    sonstigesTexte = state.sonstigesTexte,
                    sauerstoffLiterProMin = state.sauerstoffText.toFloatOrNull(),
                    medikamente = state.medikamente,
                    sonstige = state.sonstige,
                    ersthelferMassnahmen = ErsthelferMassnahmen(
                        suffizient = state.ersthelferSuffizient,
                        insuffizient = state.ersthelferInsuffizient,
                        aed = state.ersthelferAed,
                        keine = state.ersthelferKeine
                    )
                )
            )
        }
    }
}
