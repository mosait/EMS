package com.mosait.ems.core.data.repository

import com.mosait.ems.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<String>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: String)
}
