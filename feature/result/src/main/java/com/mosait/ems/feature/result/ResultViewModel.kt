package com.mosait.ems.feature.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.MissionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val zustandVerbessert: Boolean = false,
    val zustandUnveraendert: Boolean = false,
    val zustandVerschlechtert: Boolean = false,
    val transportNichtErforderlich: Boolean = false,
    val patientLehntTransportAb: Boolean = false,
    val notarztNachgefordert: Boolean = false,
    val notarztAbbestellt: Boolean = false,
    val hausarztInformiert: Boolean = false,
    val todAmNotfallort: Boolean = false,
    val todWaehrendTransport: Boolean = false,
    val nacaScore: Int? = null,
    val uebergabeAn: String = "",
    val wertsachen: String = "",
    val verlaufsbeschreibung: String = ""
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = protocolRepository.getMissionResult(patientId).firstOrNull()
            if (result != null) {
                _uiState.value = ResultUiState(
                    zustandVerbessert = result.zustandVerbessert,
                    zustandUnveraendert = result.zustandUnveraendert,
                    zustandVerschlechtert = result.zustandVerschlechtert,
                    transportNichtErforderlich = result.transportNichtErforderlich,
                    patientLehntTransportAb = result.patientLehntTransportAb,
                    notarztNachgefordert = result.notarztNachgefordert,
                    notarztAbbestellt = result.notarztAbbestellt,
                    hausarztInformiert = result.hausarztInformiert,
                    todAmNotfallort = result.todAmNotfallort,
                    todWaehrendTransport = result.todWaehrendTransport,
                    nacaScore = result.nacaScore,
                    uebergabeAn = result.uebergabeAn,
                    wertsachen = result.wertsachen,
                    verlaufsbeschreibung = result.verlaufsbeschreibung
                )
            }
        }
    }

    fun toggleZustandVerbessert() = _uiState.update { it.copy(zustandVerbessert = !it.zustandVerbessert) }
    fun toggleZustandUnveraendert() = _uiState.update { it.copy(zustandUnveraendert = !it.zustandUnveraendert) }
    fun toggleZustandVerschlechtert() = _uiState.update { it.copy(zustandVerschlechtert = !it.zustandVerschlechtert) }
    fun toggleTransportNichtErforderlich() = _uiState.update { it.copy(transportNichtErforderlich = !it.transportNichtErforderlich) }
    fun togglePatientLehntTransportAb() = _uiState.update { it.copy(patientLehntTransportAb = !it.patientLehntTransportAb) }
    fun toggleNotarztNachgefordert() = _uiState.update { it.copy(notarztNachgefordert = !it.notarztNachgefordert) }
    fun toggleNotarztAbbestellt() = _uiState.update { it.copy(notarztAbbestellt = !it.notarztAbbestellt) }
    fun toggleHausarztInformiert() = _uiState.update { it.copy(hausarztInformiert = !it.hausarztInformiert) }
    fun toggleTodAmNotfallort() = _uiState.update { it.copy(todAmNotfallort = !it.todAmNotfallort) }
    fun toggleTodWaehrendTransport() = _uiState.update { it.copy(todWaehrendTransport = !it.todWaehrendTransport) }
    fun updateNacaScore(value: Int) = _uiState.update { it.copy(nacaScore = value) }
    fun updateUebergabeAn(value: String) = _uiState.update { it.copy(uebergabeAn = value) }
    fun updateWertsachen(value: String) = _uiState.update { it.copy(wertsachen = value) }
    fun updateVerlaufsbeschreibung(value: String) = _uiState.update { it.copy(verlaufsbeschreibung = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveMissionResult(
                MissionResult(
                    patientId = patientId,
                    zustandVerbessert = state.zustandVerbessert,
                    zustandUnveraendert = state.zustandUnveraendert,
                    zustandVerschlechtert = state.zustandVerschlechtert,
                    transportNichtErforderlich = state.transportNichtErforderlich,
                    patientLehntTransportAb = state.patientLehntTransportAb,
                    notarztNachgefordert = state.notarztNachgefordert,
                    notarztAbbestellt = state.notarztAbbestellt,
                    hausarztInformiert = state.hausarztInformiert,
                    todAmNotfallort = state.todAmNotfallort,
                    todWaehrendTransport = state.todWaehrendTransport,
                    nacaScore = state.nacaScore,
                    uebergabeAn = state.uebergabeAn,
                    wertsachen = state.wertsachen,
                    verlaufsbeschreibung = state.verlaufsbeschreibung
                )
            )
        }
    }
}
