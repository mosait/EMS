package com.mosait.ems.feature.patient

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mosait.ems.core.ui.util.DateTimeUtil
import com.mosait.ems.core.data.repository.PatientRepository
import com.mosait.ems.core.model.Geschlecht
import com.mosait.ems.core.model.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientFormUiState(
    val nachname: String = "",
    val vorname: String = "",
    val geburtsdatumText: String = "",
    val geschlecht: Geschlecht = Geschlecht.UNBEKANNT,
    val krankenkasse: String = "",
    val versichertenNummer: String = "",
    val isSaving: Boolean = false,
    val savedPatientId: Long? = null,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class PatientFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val editPatientId: Long? = savedStateHandle.get<Long>("patientId")

    private val _uiState = MutableStateFlow(
        PatientFormUiState(
            isEditMode = editPatientId != null,
            isLoading = editPatientId != null
        )
    )
    val uiState: StateFlow<PatientFormUiState> = _uiState.asStateFlow()

    private var existingPatient: Patient? = null

    private var _initialState: PatientFormUiState? = null
    val hasUnsavedChanges: Boolean get() = _initialState != null && _uiState.value != _initialState

    init {
        editPatientId?.let { id ->
            viewModelScope.launch {
                patientRepository.getPatientByIdOnce(id)?.let { patient ->
                    existingPatient = patient
                    _uiState.update {
                        it.copy(
                            nachname = patient.nachname,
                            vorname = patient.vorname,
                            geburtsdatumText = DateTimeUtil.formatDate(patient.geburtsdatum),
                            geschlecht = patient.geschlecht,
                            krankenkasse = patient.krankenkasse,
                            versichertenNummer = patient.versichertenNummer,
                            isLoading = false
                        )
                    }
                }
                _initialState = _uiState.value
            }
        } ?: run { _initialState = _uiState.value }
    }

    fun updateNachname(value: String) = _uiState.update { it.copy(nachname = value) }
    fun updateVorname(value: String) = _uiState.update { it.copy(vorname = value) }
    fun updateGeburtsdatum(value: String) = _uiState.update { it.copy(geburtsdatumText = value) }
    fun updateGeschlecht(value: Geschlecht) = _uiState.update { it.copy(geschlecht = value) }
    fun updateKrankenkasse(value: String) = _uiState.update { it.copy(krankenkasse = value) }
    fun updateVersichertenNummer(value: String) = _uiState.update { it.copy(versichertenNummer = value) }

    fun savePatient(missionId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            if (state.isEditMode && existingPatient != null) {
                val updated = existingPatient!!.copy(
                    nachname = state.nachname,
                    vorname = state.vorname,
                    geburtsdatum = DateTimeUtil.parseDate(state.geburtsdatumText),
                    geschlecht = state.geschlecht,
                    krankenkasse = state.krankenkasse,
                    versichertenNummer = state.versichertenNummer
                )
                patientRepository.updatePatient(updated)
                _uiState.update { it.copy(isSaving = false, savedPatientId = updated.id) }
            } else {
                val patient = Patient(
                    missionId = missionId,
                    nachname = state.nachname,
                    vorname = state.vorname,
                    geburtsdatum = DateTimeUtil.parseDate(state.geburtsdatumText),
                    geschlecht = state.geschlecht,
                    krankenkasse = state.krankenkasse,
                    versichertenNummer = state.versichertenNummer
                )
                val id = patientRepository.createPatient(patient)
                _uiState.update { it.copy(isSaving = false, savedPatientId = id) }
            }
        }
    }
}
