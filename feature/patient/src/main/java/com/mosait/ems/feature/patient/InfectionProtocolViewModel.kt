package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.InfectionProtocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InfectionProtocolUiState(
    val bekannteInfektionen: List<String> = emptyList(),
    val infektionFreitext: String = "",
    val schutzHandschuhe: Boolean = false,
    val schutzMundschutz: Boolean = false,
    val schutzSchutzbrille: Boolean = false,
    val schutzSchutzkittel: Boolean = false,
    val schutzFFP2: Boolean = false,
    val schutzSonstiges: String = "",
    val expositionStichverletzung: Boolean = false,
    val expositionSchleimhaut: Boolean = false,
    val expositionHautkontakt: Boolean = false,
    val expositionKeine: Boolean = true,
    val fahrzeugDesinfiziert: Boolean = false,
    val geraeteDesinfiziert: Boolean = false,
    val waescheGewechselt: Boolean = false,
    val desinfektionsmittel: String = "",
    val desinfektionDurchgefuehrtVon: String = "",
    val bemerkungen: String = ""
)

@HiltViewModel
class InfectionProtocolViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(InfectionProtocolUiState())
    val uiState: StateFlow<InfectionProtocolUiState> = _uiState.asStateFlow()

    private var _initialState: InfectionProtocolUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val protocol = protocolRepository.getInfectionProtocol(patientId).firstOrNull()
            if (protocol != null) {
                _uiState.value = InfectionProtocolUiState(
                    bekannteInfektionen = protocol.bekannteInfektionen,
                    infektionFreitext = protocol.infektionFreitext,
                    schutzHandschuhe = protocol.schutzHandschuhe,
                    schutzMundschutz = protocol.schutzMundschutz,
                    schutzSchutzbrille = protocol.schutzSchutzbrille,
                    schutzSchutzkittel = protocol.schutzSchutzkittel,
                    schutzFFP2 = protocol.schutzFFP2,
                    schutzSonstiges = protocol.schutzSonstiges,
                    expositionStichverletzung = protocol.expositionStichverletzung,
                    expositionSchleimhaut = protocol.expositionSchleimhaut,
                    expositionHautkontakt = protocol.expositionHautkontakt,
                    expositionKeine = protocol.expositionKeine,
                    fahrzeugDesinfiziert = protocol.fahrzeugDesinfiziert,
                    geraeteDesinfiziert = protocol.geraeteDesinfiziert,
                    waescheGewechselt = protocol.waescheGewechselt,
                    desinfektionsmittel = protocol.desinfektionsmittel,
                    desinfektionDurchgefuehrtVon = protocol.desinfektionDurchgefuehrtVon,
                    bemerkungen = protocol.bemerkungen
                )
            }
            _initialState = _uiState.value
        }
    }

    fun updateBekannteInfektionen(infektionen: List<String>) =
        _uiState.update { it.copy(bekannteInfektionen = infektionen) }

    fun updateInfektionFreitext(value: String) =
        _uiState.update { it.copy(infektionFreitext = value) }

    fun toggleSchutzHandschuhe() =
        _uiState.update { it.copy(schutzHandschuhe = !it.schutzHandschuhe) }

    fun toggleSchutzMundschutz() =
        _uiState.update { it.copy(schutzMundschutz = !it.schutzMundschutz) }

    fun toggleSchutzSchutzbrille() =
        _uiState.update { it.copy(schutzSchutzbrille = !it.schutzSchutzbrille) }

    fun toggleSchutzSchutzkittel() =
        _uiState.update { it.copy(schutzSchutzkittel = !it.schutzSchutzkittel) }

    fun toggleSchutzFFP2() =
        _uiState.update { it.copy(schutzFFP2 = !it.schutzFFP2) }

    fun updateSchutzSonstiges(value: String) =
        _uiState.update { it.copy(schutzSonstiges = value) }

    fun toggleExpositionStichverletzung() =
        _uiState.update { it.copy(expositionStichverletzung = !it.expositionStichverletzung, expositionKeine = false) }

    fun toggleExpositionSchleimhaut() =
        _uiState.update { it.copy(expositionSchleimhaut = !it.expositionSchleimhaut, expositionKeine = false) }

    fun toggleExpositionHautkontakt() =
        _uiState.update { it.copy(expositionHautkontakt = !it.expositionHautkontakt, expositionKeine = false) }

    fun toggleExpositionKeine() =
        _uiState.update {
            it.copy(
                expositionKeine = !it.expositionKeine,
                expositionStichverletzung = false,
                expositionSchleimhaut = false,
                expositionHautkontakt = false
            )
        }

    fun toggleFahrzeugDesinfiziert() =
        _uiState.update { it.copy(fahrzeugDesinfiziert = !it.fahrzeugDesinfiziert) }

    fun toggleGeraeteDesinfiziert() =
        _uiState.update { it.copy(geraeteDesinfiziert = !it.geraeteDesinfiziert) }

    fun toggleWaescheGewechselt() =
        _uiState.update { it.copy(waescheGewechselt = !it.waescheGewechselt) }

    fun updateDesinfektionsmittel(value: String) =
        _uiState.update { it.copy(desinfektionsmittel = value) }

    fun updateDesinfektionDurchgefuehrtVon(value: String) =
        _uiState.update { it.copy(desinfektionDurchgefuehrtVon = value) }

    fun updateBemerkungen(value: String) =
        _uiState.update { it.copy(bemerkungen = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveInfectionProtocol(
                InfectionProtocol(
                    patientId = patientId,
                    bekannteInfektionen = state.bekannteInfektionen,
                    infektionFreitext = state.infektionFreitext,
                    schutzHandschuhe = state.schutzHandschuhe,
                    schutzMundschutz = state.schutzMundschutz,
                    schutzSchutzbrille = state.schutzSchutzbrille,
                    schutzSchutzkittel = state.schutzSchutzkittel,
                    schutzFFP2 = state.schutzFFP2,
                    schutzSonstiges = state.schutzSonstiges,
                    expositionStichverletzung = state.expositionStichverletzung,
                    expositionSchleimhaut = state.expositionSchleimhaut,
                    expositionHautkontakt = state.expositionHautkontakt,
                    expositionKeine = state.expositionKeine,
                    fahrzeugDesinfiziert = state.fahrzeugDesinfiziert,
                    geraeteDesinfiziert = state.geraeteDesinfiziert,
                    waescheGewechselt = state.waescheGewechselt,
                    desinfektionsmittel = state.desinfektionsmittel,
                    desinfektionDurchgefuehrtVon = state.desinfektionDurchgefuehrtVon,
                    bemerkungen = state.bemerkungen
                )
            )
        }
    }
}
