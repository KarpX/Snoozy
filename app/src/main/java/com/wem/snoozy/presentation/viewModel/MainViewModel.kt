package com.wem.snoozy.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.domain.repository.AlarmRepository
import com.wem.snoozy.domain.usecase.DeleteAlarmUseCase
import com.wem.snoozy.domain.usecase.GetAllAlarmsUseCase
import com.wem.snoozy.domain.usecase.ToggleAlarmStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getAllAlarmsUseCase: GetAllAlarmsUseCase,
    private val toggleAlarmStateUseCase: ToggleAlarmStateUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val alarmRepository: AlarmRepository // Используем репозиторий для групп
) : ViewModel() {
    private val _state = MutableStateFlow<MainState>(MainState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = MainState.Loading
            combine(
                getAllAlarmsUseCase(),
                alarmRepository.getGroups()
            ) { alarms, groups ->
                MainState.Content(alarms, groups)
            }.collect {
                _state.value = it
            }
        }
    }

    fun processCommand(command: MainCommand) {
        when (command) {
            is MainCommand.SwitchAlarm -> {
                viewModelScope.launch {
                    toggleAlarmStateUseCase(command.alarmItem)
                }
            }
            is MainCommand.DeleteAlarm -> {
                viewModelScope.launch {
                    deleteAlarmUseCase(command.alarmId)
                }
            }
            is MainCommand.DeleteGroup -> {
                viewModelScope.launch {
                    alarmRepository.deleteGroup(command.groupId)
                }
            }
        }
    }
}

sealed interface MainState {
    data object Initial : MainState
    data class Content(
        val alarmList: List<AlarmItem>,
        val groupList: List<GroupItem> = emptyList()
    ) : MainState
    data object Loading : MainState
}

sealed interface MainCommand {
    data class DeleteAlarm(val alarmId: Int) : MainCommand
    data class SwitchAlarm(val alarmItem: AlarmItem) : MainCommand
    data class DeleteGroup(val groupId: Int) : MainCommand
}
