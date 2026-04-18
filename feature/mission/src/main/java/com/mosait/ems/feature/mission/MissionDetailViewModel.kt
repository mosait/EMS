package com.mosait.ems.feature.mission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MissionDetailUiState(
    val mission: Mission? = null,
    val patients: List<Patient> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MissionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val missionId: Long = savedStateHandle["missionId"] ?: 0L

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    val uiState: StateFlow<MissionDetailUiState> = combine(
        missionRepository.getMissionById(missionId),
        patientRepository.getPatientsByMission(missionId)
    ) { mission, patients ->
        MissionDetailUiState(
            mission = mission,
            patients = patients,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MissionDetailUiState()
    )

    fun deleteMission() {
        viewModelScope.launch {
            missionRepository.deleteMission(missionId)
            _isDeleted.value = true
        }
    }

    fun deletePatient(patientId: Long) {
        viewModelScope.launch {
            patientRepository.deletePatient(patientId)
        }
    }
}
