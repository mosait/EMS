package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.data.repository.MissionRepository
import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.data.repository.ProtocolRepository
import com.mosait.ems.core.model.TransportRefusal
import com.mosait.ems.core.ui.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TransportRefusalUiState(
    val enabled: Boolean = false,
    val patientName: String = "",
    val geburtsdatum: String = "",
    val geburtsort: String = "",
    val datum: LocalDate? = null,
    val uhrzeit: LocalTime? = null,
    val lehntBehandlungAb: Boolean = false,
    val lehntTransportAb: Boolean = false,
    val nichtAuszuschliessendeErkrankungen: String = "",
    val moeglicheFolgen: String = "",
    val nameZeugeAngehoeriger: String = "",
    val adresseZeugeAngehoeriger: String = "",
    val nameZeugeRettungsdienst: String = "",
    val nameRettungsdienstNotarzt: String = "",
    val signaturePatient: ByteArray? = null,
    val ort: String = ""
)

@HiltViewModel
class TransportRefusalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val protocolRepository: ProtocolRepository,
    private val patientRepository: PatientRepository,
    private val missionRepository: MissionRepository
) : ViewModel() {

    private val patientId: Long = savedStateHandle["patientId"] ?: 0L

    private val _uiState = MutableStateFlow(TransportRefusalUiState())
    val uiState: StateFlow<TransportRefusalUiState> = _uiState.asStateFlow()

    private var _initialState: TransportRefusalUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        viewModelScope.launch {
            val existing = protocolRepository.getTransportRefusal(patientId).firstOrNull()
            if (existing != null) {
                // Load saved data
                _uiState.value = TransportRefusalUiState(
                    enabled = existing.enabled,
                    patientName = existing.patientName,
                    geburtsdatum = existing.geburtsdatum,
                    geburtsort = existing.geburtsort,
                    datum = existing.datum,
                    uhrzeit = existing.uhrzeit,
                    lehntBehandlungAb = existing.lehntBehandlungAb,
                    lehntTransportAb = existing.lehntTransportAb,
                    nichtAuszuschliessendeErkrankungen = existing.nichtAuszuschliessendeErkrankungen,
                    moeglicheFolgen = existing.moeglicheFolgen,
                    nameZeugeAngehoeriger = existing.nameZeugeAngehoeriger,
                    adresseZeugeAngehoeriger = existing.adresseZeugeAngehoeriger,
                    nameZeugeRettungsdienst = existing.nameZeugeRettungsdienst,
                    nameRettungsdienstNotarzt = existing.nameRettungsdienstNotarzt,
                    signaturePatient = existing.signaturePatient,
                    ort = existing.ort
                )
            } else {
                // Default: disabled, empty
                _uiState.value = TransportRefusalUiState()
            }
            _initialState = _uiState.value
        }
    }

    fun toggleEnabled() {
        viewModelScope.launch {
            val wasEnabled = _uiState.value.enabled
            if (!wasEnabled) {
                // Auto-fill from patient and mission data when enabling
                val patient = patientRepository.getPatientByIdOnce(patientId)
                val mission = patient?.let { missionRepository.getMissionByIdOnce(it.missionId) }

                val patientName = patient?.let {
                    "${it.vorname} ${it.nachname}".trim()
                } ?: ""

                val gebDatum = patient?.geburtsdatum?.let { DateTimeUtil.formatDate(it) } ?: ""

                val einsatzOrt = mission?.let {
                    listOfNotNull(
                        it.einsatzOrtOrt.ifBlank { null }
                    ).joinToString(", ")
                } ?: ""

                val personal = mission?.personal ?: emptyList()
                val rettungsdienstName = personal.firstOrNull()?.name ?: ""

                _uiState.update {
                    it.copy(
                        enabled = true,
                        patientName = patientName,
                        geburtsdatum = gebDatum,
                        datum = mission?.einsatzDatum ?: LocalDate.now(),
                        uhrzeit = LocalTime.now(),
                        ort = einsatzOrt,
                        nameRettungsdienstNotarzt = rettungsdienstName
                    )
                }
            } else {
                _uiState.update { it.copy(enabled = false) }
            }
        }
    }

    fun updatePatientName(value: String) = _uiState.update { it.copy(patientName = value) }
    fun updateGeburtsdatum(value: String) = _uiState.update { it.copy(geburtsdatum = value) }
    fun updateGeburtsort(value: String) = _uiState.update { it.copy(geburtsort = value) }
    fun updateDatum(value: LocalDate?) = _uiState.update { it.copy(datum = value) }
    fun updateUhrzeit(value: LocalTime?) = _uiState.update { it.copy(uhrzeit = value) }
    fun toggleLehntBehandlungAb() = _uiState.update { it.copy(lehntBehandlungAb = !it.lehntBehandlungAb) }
    fun toggleLehntTransportAb() = _uiState.update { it.copy(lehntTransportAb = !it.lehntTransportAb) }
    fun updateNichtAuszuschliessendeErkrankungen(value: String) = _uiState.update { it.copy(nichtAuszuschliessendeErkrankungen = value) }
    fun updateMoeglicheFolgen(value: String) = _uiState.update { it.copy(moeglicheFolgen = value) }
    fun updateNameZeugeAngehoeriger(value: String) = _uiState.update { it.copy(nameZeugeAngehoeriger = value) }
    fun updateAdresseZeugeAngehoeriger(value: String) = _uiState.update { it.copy(adresseZeugeAngehoeriger = value) }
    fun updateNameZeugeRettungsdienst(value: String) = _uiState.update { it.copy(nameZeugeRettungsdienst = value) }
    fun updateNameRettungsdienstNotarzt(value: String) = _uiState.update { it.copy(nameRettungsdienstNotarzt = value) }
    fun updateOrt(value: String) = _uiState.update { it.copy(ort = value) }
    fun updateSignaturePatient(value: ByteArray?) = _uiState.update { it.copy(signaturePatient = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            protocolRepository.saveTransportRefusal(
                TransportRefusal(
                    patientId = patientId,
                    enabled = state.enabled,
                    patientName = state.patientName,
                    geburtsdatum = state.geburtsdatum,
                    geburtsort = state.geburtsort,
                    datum = state.datum,
                    uhrzeit = state.uhrzeit,
                    lehntBehandlungAb = state.lehntBehandlungAb,
                    lehntTransportAb = state.lehntTransportAb,
                    nichtAuszuschliessendeErkrankungen = state.nichtAuszuschliessendeErkrankungen,
                    moeglicheFolgen = state.moeglicheFolgen,
                    nameZeugeAngehoeriger = state.nameZeugeAngehoeriger,
                    adresseZeugeAngehoeriger = state.adresseZeugeAngehoeriger,
                    nameZeugeRettungsdienst = state.nameZeugeRettungsdienst,
                    nameRettungsdienstNotarzt = state.nameRettungsdienstNotarzt,
                    signaturePatient = state.signaturePatient,
                    ort = state.ort
                )
            )
        }
    }
}
