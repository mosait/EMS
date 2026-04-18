package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.VitalSign
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class VitalsUiState(
    val vitalSigns: List<VitalSign> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class VitalsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(VitalsUiState())
    val uiState: StateFlow<VitalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            protocolRepository.getVitalSigns(patientId).collect { signs ->
                _uiState.value = VitalsUiState(vitalSigns = signs, isLoading = false)
            }
        }
    }

    fun addVitalSign(vitalSign: VitalSign) {
        viewModelScope.launch {
            protocolRepository.addVitalSign(
                vitalSign.copy(
                    patientId = patientId,
                    timestamp = LocalDateTime.now()
                )
            )
        }
    }

    fun updateVitalSign(vitalSign: VitalSign) {
        viewModelScope.launch {
            protocolRepository.updateVitalSign(vitalSign)
        }
    }

    fun deleteVitalSign(id: Long) {
        viewModelScope.launch {
            protocolRepository.deleteVitalSign(id)
        }
    }
}
