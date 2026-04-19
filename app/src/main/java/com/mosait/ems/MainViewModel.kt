package com.mosait.ems

import androidx.lifecycle.ViewModel
import com.mosait.ems.core.data.repository.SettingsRepository
import com.mosait.ems.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val themeMode: Flow<ThemeMode> = settingsRepository.themeMode
}
