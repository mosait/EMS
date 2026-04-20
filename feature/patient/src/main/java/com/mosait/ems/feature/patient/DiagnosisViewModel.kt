package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.Diagnosis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosisUiState(
    val keine: Boolean = false,
    val selectedConditions: List<String> = emptyList(),
    val sonstigesTexte: Map<String, String> = emptyMap(),
    val freitext: String = "",
    val validationTriggered: Boolean = false
) {
    val selectionError: Boolean get() = validationTriggered && !keine &&
        selectedConditions.isEmpty() && freitext.isBlank()
    val isValid: Boolean get() = keine || selectedConditions.isNotEmpty() || freitext.isNotBlank()
}

@HiltViewModel
class DiagnosisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(DiagnosisUiState())
    val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

    private var _initialState: DiagnosisUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val diagnosis = protocolRepository.getDiagnosis(patientId).firstOrNull()
            if (diagnosis != null) {
                _uiState.value = DiagnosisUiState(
                    keine = diagnosis.keine,
                    selectedConditions = diagnosis.selectedConditions,
                    sonstigesTexte = diagnosis.sonstigesTexte,
                    freitext = diagnosis.freitext
                )
            }
            _initialState = _uiState.value
        }
    }

    fun toggleKeine() = _uiState.update { it.copy(keine = !it.keine) }
    fun updateSelectedConditions(conditions: List<String>) = _uiState.update { it.copy(selectedConditions = conditions) }
    fun updateSonstigesText(category: String, text: String) = _uiState.update {
        it.copy(sonstigesTexte = it.sonstigesTexte + (category to text))
    }
    fun updateFreitext(value: String) = _uiState.update { it.copy(freitext = value) }

    fun save(): Boolean {
        _uiState.update { it.copy(validationTriggered = true) }
        if (!_uiState.value.isValid) return false

        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveDiagnosis(
                Diagnosis(
                    patientId = patientId,
                    keine = state.keine,
                    selectedConditions = state.selectedConditions,
                    sonstigesTexte = state.sonstigesTexte,
                    freitext = state.freitext
                )
            )
        }
        return true
    }
}
