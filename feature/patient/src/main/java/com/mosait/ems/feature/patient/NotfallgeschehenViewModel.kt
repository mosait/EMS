package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.InitialAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotfallgeschehenUiState(
    val notfallgeschehen: String = ""
)

@HiltViewModel
class NotfallgeschehenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(NotfallgeschehenUiState())
    val uiState: StateFlow<NotfallgeschehenUiState> = _uiState.asStateFlow()

    private var _initialState: NotfallgeschehenUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val assessment = protocolRepository.getInitialAssessment(patientId).firstOrNull()
            if (assessment != null) {
                _uiState.value = NotfallgeschehenUiState(
                    notfallgeschehen = assessment.notfallgeschehen
                )
            }
            _initialState = _uiState.value
        }
    }

    fun updateNotfallgeschehen(value: String) = _uiState.update { it.copy(notfallgeschehen = value) }

    fun save() {
        viewModelScope.launch {
            val existing = protocolRepository.getInitialAssessment(patientId).firstOrNull()
            val assessment = (existing ?: InitialAssessment(patientId = patientId)).copy(
                notfallgeschehen = _uiState.value.notfallgeschehen
            )
            protocolRepository.saveInitialAssessment(assessment)
        }
    }
}
