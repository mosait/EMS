package com.mosait.ems.feature.injury

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.BodyRegion
import com.mosait.ems.core.model.BodyRegionEntry
import com.mosait.ems.core.model.Injury
import com.mosait.ems.core.model.InjurySeverity
import com.mosait.ems.core.model.InjuryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InjuryUiState(
    val keine: Boolean = false,
    val selectedTypes: List<InjuryType> = emptyList(),
    val regionSeverities: Map<BodyRegion, InjurySeverity> = emptyMap(),
    val freitext: String = ""
) {
    val selectedRegions: List<BodyRegion> get() = regionSeverities.keys.toList()
}

@HiltViewModel
class InjuryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(InjuryUiState())
    val uiState: StateFlow<InjuryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val injury = protocolRepository.getInjury(patientId).firstOrNull()
            if (injury != null) {
                _uiState.value = InjuryUiState(
                    keine = injury.keine,
                    selectedTypes = injury.injuryTypes,
                    regionSeverities = injury.bodyRegions.associate { it.region to it.severity },
                    freitext = injury.freitext
                )
            }
        }
    }

    fun toggleKeine() = _uiState.update { it.copy(keine = !it.keine) }
    fun updateSelectedTypes(types: List<InjuryType>) = _uiState.update { it.copy(selectedTypes = types) }

    fun updateSelectedRegions(regions: List<BodyRegion>) = _uiState.update { state ->
        val updated = regions.associateWith { region ->
            state.regionSeverities[region] ?: InjurySeverity.LEICHT
        }
        state.copy(regionSeverities = updated)
    }

    fun updateSeverityForRegion(region: BodyRegion, severity: InjurySeverity) = _uiState.update { state ->
        state.copy(regionSeverities = state.regionSeverities + (region to severity))
    }

    fun updateFreitext(value: String) = _uiState.update { it.copy(freitext = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveInjury(
                Injury(
                    patientId = patientId,
                    keine = state.keine,
                    injuryTypes = state.selectedTypes,
                    bodyRegions = state.regionSeverities.map { (region, severity) ->
                        BodyRegionEntry(
                            region = region,
                            severity = severity
                        )
                    },
                    freitext = state.freitext
                )
            )
        }
    }
}
