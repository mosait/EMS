package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class InitialAssessmentUiState(
    val bewusstseinslage: Bewusstseinslage = Bewusstseinslage.ORIENTIERT,
    val bewusstseinslageText: String = "",
    val kreislaufSchock: Boolean = false,
    val kreislaufStillstand: Boolean = false,
    val kreislaufReanimation: Boolean = false,
    val kreislaufSonstigesText: String = "",
    val rrSystolisch: Int? = null,
    val rrDiastolisch: Int? = null,
    val puls: Int? = null,
    val spO2: Int? = null,
    val atemfrequenz: Int? = null,
    val blutzucker: Int? = null,
    val messwertZeit: LocalDateTime? = null,
    val gcsAugen: Int = 4,
    val gcsVerbal: Int = 5,
    val gcsMotorik: Int = 6,
    val pupilleLinks: PupillenStatus = PupillenStatus.MITTEL,
    val pupilleRechts: PupillenStatus = PupillenStatus.MITTEL,
    val lichtreaktionLinks: Boolean = true,
    val lichtreaktionRechts: Boolean = true,
    val ekg: EkgRhythmus = EkgRhythmus.SINUS,
    val ekgSonstigesText: String = "",
    val schmerzSkala: Int = 0,
    val atmung: AtmungStatus = AtmungStatus.SPONTAN,
    val atmungSonstigesText: String = ""
)

@HiltViewModel
class InitialAssessmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(InitialAssessmentUiState())
    val uiState: StateFlow<InitialAssessmentUiState> = _uiState.asStateFlow()

    private var _initialState: InitialAssessmentUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val assessment = protocolRepository.getInitialAssessment(patientId).firstOrNull()
            if (assessment != null) {
                _uiState.value = InitialAssessmentUiState(
                    bewusstseinslage = assessment.bewusstseinslage,
                    bewusstseinslageText = assessment.bewusstseinslageText,
                    kreislaufSchock = assessment.kreislaufSchock,
                    kreislaufStillstand = assessment.kreislaufStillstand,
                    kreislaufReanimation = assessment.kreislaufReanimation,
                    kreislaufSonstigesText = assessment.kreislaufSonstigesText,
                    rrSystolisch = assessment.rrSystolisch,
                    rrDiastolisch = assessment.rrDiastolisch,
                    puls = assessment.puls,
                    spO2 = assessment.spO2,
                    atemfrequenz = assessment.atemfrequenz,
                    blutzucker = assessment.blutzucker,
                    messwertZeit = assessment.messwertZeit,
                    gcsAugen = assessment.gcsAugen,
                    gcsVerbal = assessment.gcsVerbal,
                    gcsMotorik = assessment.gcsMotorik,
                    pupilleLinks = assessment.pupilleLinks,
                    pupilleRechts = assessment.pupilleRechts,
                    lichtreaktionLinks = assessment.pupillenLichtreaktionLinks,
                    lichtreaktionRechts = assessment.pupillenLichtreaktionRechts,
                    ekg = assessment.ekg,
                    ekgSonstigesText = assessment.ekgSonstigesText,
                    schmerzSkala = assessment.schmerzSkala,
                    atmung = assessment.atmung,
                    atmungSonstigesText = assessment.atmungSonstigesText
                )
            }
            _initialState = _uiState.value
        }
    }

    fun updateBewusstseinslage(value: Bewusstseinslage) = _uiState.update { it.copy(bewusstseinslage = value) }
    fun updateBewusstseinslageText(value: String) = _uiState.update { it.copy(bewusstseinslageText = value) }
    fun toggleKreislaufSchock() = _uiState.update { it.copy(kreislaufSchock = !it.kreislaufSchock) }
    fun toggleKreislaufStillstand() = _uiState.update { it.copy(kreislaufStillstand = !it.kreislaufStillstand) }
    fun toggleKreislaufReanimation() = _uiState.update { it.copy(kreislaufReanimation = !it.kreislaufReanimation) }
    fun updateKreislaufSonstigesText(value: String) = _uiState.update { it.copy(kreislaufSonstigesText = value) }
    fun updateRrSystolisch(value: Int?) = _uiState.update {
        it.copy(rrSystolisch = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updateRrDiastolisch(value: Int?) = _uiState.update {
        it.copy(rrDiastolisch = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updatePuls(value: Int?) = _uiState.update {
        it.copy(puls = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updateSpO2(value: Int?) = _uiState.update {
        it.copy(spO2 = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updateAtemfrequenz(value: Int?) = _uiState.update {
        it.copy(atemfrequenz = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updateBlutzucker(value: Int?) = _uiState.update {
        it.copy(blutzucker = value, messwertZeit = if (value != null) LocalDateTime.now() else it.messwertZeit)
    }
    fun updateGcsAugen(value: Int) = _uiState.update { it.copy(gcsAugen = value) }
    fun updateGcsVerbal(value: Int) = _uiState.update { it.copy(gcsVerbal = value) }
    fun updateGcsMotorik(value: Int) = _uiState.update { it.copy(gcsMotorik = value) }
    fun updatePupilleLinks(value: PupillenStatus) = _uiState.update { it.copy(pupilleLinks = value) }
    fun updatePupilleRechts(value: PupillenStatus) = _uiState.update { it.copy(pupilleRechts = value) }
    fun updateLichtreaktionLinks(value: Boolean) = _uiState.update { it.copy(lichtreaktionLinks = value) }
    fun updateLichtreaktionRechts(value: Boolean) = _uiState.update { it.copy(lichtreaktionRechts = value) }
    fun updateEkg(value: EkgRhythmus) = _uiState.update { it.copy(ekg = value) }
    fun updateEkgSonstigesText(value: String) = _uiState.update { it.copy(ekgSonstigesText = value) }
    fun updateSchmerzSkala(value: Int) = _uiState.update { it.copy(schmerzSkala = value) }
    fun updateAtmung(value: AtmungStatus) = _uiState.update { it.copy(atmung = value) }
    fun updateAtmungSonstigesText(value: String) = _uiState.update { it.copy(atmungSonstigesText = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value

            // Preserve notfallgeschehen from existing record
            val existing = protocolRepository.getInitialAssessment(patientId).firstOrNull()
            val notfallgeschehen = existing?.notfallgeschehen ?: ""

            protocolRepository.saveInitialAssessment(
                InitialAssessment(
                    patientId = patientId,
                    notfallgeschehen = notfallgeschehen,
                    bewusstseinslage = state.bewusstseinslage,
                    bewusstseinslageText = state.bewusstseinslageText,
                    kreislaufSchock = state.kreislaufSchock,
                    kreislaufStillstand = state.kreislaufStillstand,
                    kreislaufReanimation = state.kreislaufReanimation,
                    kreislaufSonstigesText = state.kreislaufSonstigesText,
                    rrSystolisch = state.rrSystolisch,
                    rrDiastolisch = state.rrDiastolisch,
                    puls = state.puls,
                    spO2 = state.spO2,
                    atemfrequenz = state.atemfrequenz,
                    blutzucker = state.blutzucker,
                    messwertZeit = state.messwertZeit,
                    gcsAugen = state.gcsAugen,
                    gcsVerbal = state.gcsVerbal,
                    gcsMotorik = state.gcsMotorik,
                    pupilleLinks = state.pupilleLinks,
                    pupilleRechts = state.pupilleRechts,
                    pupillenLichtreaktionLinks = state.lichtreaktionLinks,
                    pupillenLichtreaktionRechts = state.lichtreaktionRechts,
                    ekg = state.ekg,
                    ekgSonstigesText = state.ekgSonstigesText,
                    schmerzSkala = state.schmerzSkala,
                    atmung = state.atmung,
                    atmungSonstigesText = state.atmungSonstigesText
                )
            )

            // Create VitalSign entry from Messwerte if any values exist
            val hasMesswerte = state.rrSystolisch != null || state.rrDiastolisch != null ||
                    state.puls != null || state.spO2 != null ||
                    state.atemfrequenz != null || state.blutzucker != null

            if (hasMesswerte) {
                val timestamp = state.messwertZeit ?: LocalDateTime.now()
                protocolRepository.addVitalSign(
                    VitalSign(
                        patientId = patientId,
                        timestamp = timestamp,
                        rrSystolisch = state.rrSystolisch,
                        rrDiastolisch = state.rrDiastolisch,
                        puls = state.puls,
                        spO2 = state.spO2,
                        atemfrequenz = state.atemfrequenz,
                        blutzucker = state.blutzucker,
                        gcs = state.gcsAugen + state.gcsVerbal + state.gcsMotorik,
                        schmerzSkala = state.schmerzSkala,
                        bemerkung = "Erstbefund"
                    )
                )
            }
        }
    }
}
