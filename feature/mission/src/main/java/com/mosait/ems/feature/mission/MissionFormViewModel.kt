package com.mosait.ems.feature.mission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.model.EinsatzArt
import com.mosait.ems.core.model.Mission
import com.mosait.ems.core.model.PersonalEntry
import com.mosait.ems.core.model.PersonalRolle
import com.mosait.ems.core.model.RettungsMittel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class MissionFormUiState(
    val einsatzArt: EinsatzArt = EinsatzArt.NOTFALLEINSATZ,
    val rettungsMittel: RettungsMittel = RettungsMittel.RTW,
    val einsatzNummer: String = "",
    val funkKennung: String = "",
    val personal: List<PersonalEntry> = emptyList(),
    val einsatzOrtStrasse: String = "",
    val einsatzOrtPlz: String = "",
    val einsatzOrtOrt: String = "",
    val transportZiel: String = "",
    val isSaving: Boolean = false,
    val savedMissionId: Long? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MissionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository
) : ViewModel() {

    private val editMissionId: Long? = savedStateHandle.get<Long>("missionId")

    private val _uiState = MutableStateFlow(
        MissionFormUiState(
            isEditMode = editMissionId != null,
            isLoading = true
        )
    )
    val uiState: StateFlow<MissionFormUiState> = _uiState.asStateFlow()

    private var existingMission: Mission? = null

    private var _initialState: MissionFormUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            if (editMissionId != null) {
                // Edit mode: load existing mission
                missionRepository.getMissionByIdOnce(editMissionId)?.let { mission ->
                    existingMission = mission
                    _uiState.update {
                        it.copy(
                            einsatzArt = mission.einsatzArt,
                            rettungsMittel = mission.rettungsMittel,
                            einsatzNummer = mission.einsatzNummer,
                            funkKennung = mission.funkKennung,
                            personal = mission.personal,
                            einsatzOrtStrasse = mission.einsatzOrtStrasse,
                            einsatzOrtPlz = mission.einsatzOrtPlz,
                            einsatzOrtOrt = mission.einsatzOrtOrt,
                            transportZiel = mission.transportZiel,
                            isLoading = false
                        )
                    }
                } ?: _uiState.update { it.copy(isLoading = false) }
            } else {
                // New mission: carry over from latest mission
                missionRepository.getLatestMission()?.let { latest ->
                    _uiState.update {
                        it.copy(
                            einsatzNummer = latest.einsatzNummer,
                            funkKennung = latest.funkKennung,
                            personal = latest.personal,
                            isLoading = false
                        )
                    }
                } ?: _uiState.update { it.copy(isLoading = false) }
            }
            _initialState = _uiState.value
        }
    }

    fun updateEinsatzArt(art: EinsatzArt) = _uiState.update { it.copy(einsatzArt = art) }
    fun updateRettungsMittel(mittel: RettungsMittel) = _uiState.update { it.copy(rettungsMittel = mittel) }
    fun updateEinsatzNummer(value: String) = _uiState.update { it.copy(einsatzNummer = value) }
    fun updateFunkKennung(value: String) = _uiState.update { it.copy(funkKennung = value) }
    fun updateEinsatzOrtStrasse(value: String) = _uiState.update { it.copy(einsatzOrtStrasse = value) }
    fun updateEinsatzOrtPlz(value: String) = _uiState.update { it.copy(einsatzOrtPlz = value) }
    fun updateEinsatzOrtOrt(value: String) = _uiState.update { it.copy(einsatzOrtOrt = value) }
    fun updateTransportZiel(value: String) = _uiState.update { it.copy(transportZiel = value) }

    fun addPersonal(entry: PersonalEntry) {
        _uiState.update { it.copy(personal = it.personal + entry) }
    }

    fun removePersonal(index: Int) {
        _uiState.update { state ->
            state.copy(personal = state.personal.filterIndexed { i, _ -> i != index })
        }
    }

    fun saveMission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            if (state.isEditMode && existingMission != null) {
                val updated = existingMission!!.copy(
                    einsatzArt = state.einsatzArt,
                    rettungsMittel = state.rettungsMittel,
                    einsatzNummer = state.einsatzNummer,
                    fahrzeugKennung = state.funkKennung,
                    funkKennung = state.funkKennung,
                    personal = state.personal,
                    einsatzOrtStrasse = state.einsatzOrtStrasse,
                    einsatzOrtPlz = state.einsatzOrtPlz,
                    einsatzOrtOrt = state.einsatzOrtOrt,
                    transportZiel = state.transportZiel
                )
                missionRepository.updateMission(updated)
                _uiState.update { it.copy(isSaving = false, savedMissionId = updated.id) }
            } else {
                val mission = Mission(
                    einsatzDatum = LocalDate.now(),
                    einsatzNummer = state.einsatzNummer,
                    einsatzArt = state.einsatzArt,
                    rettungsMittel = state.rettungsMittel,
                    fahrzeugKennung = state.funkKennung,
                    funkKennung = state.funkKennung,
                    personal = state.personal,
                    einsatzOrtStrasse = state.einsatzOrtStrasse,
                    einsatzOrtPlz = state.einsatzOrtPlz,
                    einsatzOrtOrt = state.einsatzOrtOrt,
                    transportZiel = state.transportZiel
                )
                val id = missionRepository.createMission(mission)
                _uiState.update { it.copy(isSaving = false, savedMissionId = id) }
            }
        }
    }
}
