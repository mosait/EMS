package com.mosait.ems.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.model.Mission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OverviewUiState(
    val missions: List<Mission> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    val uiState: StateFlow<OverviewUiState> = missionRepository.getAllMissions()
        .map { missions ->
            OverviewUiState(missions = missions, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState()
        )

    fun deleteMission(id: Long) {
        viewModelScope.launch {
            missionRepository.deleteMission(id)
        }
    }
}
