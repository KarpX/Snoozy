package com.wem.snoozy.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.local.UserPreferencesManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *  ViewModel for settings screen
 *
 *  @param userPreferencesManager manager for working with local storage
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // Settings screen state
    private val _state = MutableStateFlow<SettingsState>(SettingsState.Initial)
    val state = _state.asStateFlow()

    val themeState = MutableStateFlow(false)

    fun processCommand(command: SettingsCommand) {
        when (command) {
            is SettingsCommand.InputCycleLength -> {
                _state.update { prevState ->
                    if (prevState is SettingsState.Content) {
                        var newValue = command.newValue
                        if (newValue.isNotEmpty()) {
                            if (!(newValue.all { it in '0'..'9' })) {
                                newValue = "0"
                            } else if (newValue.toInt() > 150) {
                                newValue = "150"
                            }
                        }
                        prevState.copy(cycleLength = newValue).also {
                            viewModelScope.launch {
                                userPreferencesManager.saveCycleLength(newValue)
                            }
                        }
                    } else {
                        prevState
                    }
                }
            }

            is SettingsCommand.InputSleepStartTime -> {
                _state.update { prevState ->
                    if (prevState is SettingsState.Content) {
                        var newValue = command.newValue
                        if (newValue.isNotEmpty()) {
                            if (!(newValue.all { it in '0'..'9' })) {
                                newValue = "0"
                            } else if (newValue.toInt() > 60) {
                                newValue = "60"
                            }
                        }
                        prevState.copy(sleepStartTime = newValue).also {
                            viewModelScope.launch {
                                userPreferencesManager.saveSleepStartTime(newValue)
                            }
                        }
                    } else {
                        prevState
                    }
                }
            }

            is SettingsCommand.ToggleTheme -> {
                _state.update { previousState ->
                    if (previousState is SettingsState.Content) {
                        previousState.copy(isDarkTheme = command.isDarkTheme).also {
                            themeState.value = command.isDarkTheme
                            viewModelScope.launch {
                                userPreferencesManager.saveTheme(command.isDarkTheme)
                            }
                        }
                    } else {
                        previousState
                    }
                }
            }

            SettingsCommand.SaveSettings -> {
                viewModelScope.launch {
                    val currentState = _state.value
                    if (currentState is SettingsState.Content) {
                        val cycleLength = currentState.cycleLength
                        val sleepStartTime = currentState.sleepStartTime
                        if (cycleLength.isEmpty()) {
                            userPreferencesManager.saveCycleLength("90")
                        }
                        if (sleepStartTime.isEmpty()) {
                            userPreferencesManager.saveSleepStartTime("0")
                        }
                    }
                }
            }
        }
    }


    init {
        observePreferences()
    }

    private fun observePreferences() {
        combine(
            userPreferencesManager.darkThemeFlow,
            userPreferencesManager.cycleLengthFlow,
            userPreferencesManager.sleepStartTimeFlow
        ) { theme, cycleLength, sleepStartTime ->

            themeState.value = theme

            SettingsState.Content(
                cycleLength = cycleLength ?: "90",
                sleepStartTime = sleepStartTime ?: "0",
                isDarkTheme = theme
            )
        }.onEach { settingsState ->
            _state.value = settingsState
        }.launchIn(viewModelScope)
    }
}

sealed interface SettingsState {

    data object Initial : SettingsState

    data class Content(
        val cycleLength: String,
        val sleepStartTime: String,
        val isDarkTheme: Boolean
    ) : SettingsState
}

sealed interface SettingsCommand {

    data class InputCycleLength(
        val newValue: String
    ) : SettingsCommand

    data class InputSleepStartTime(
        val newValue: String
    ) : SettingsCommand

    data class ToggleTheme(
        val isDarkTheme: Boolean
    ) : SettingsCommand

    data object SaveSettings : SettingsCommand

}
