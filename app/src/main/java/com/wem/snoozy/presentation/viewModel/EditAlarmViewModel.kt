package com.wem.snoozy.presentation.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.repository.AlarmRepositoryImpl
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.CycleItem
import com.wem.snoozy.domain.entity.DayItem
import com.wem.snoozy.domain.entity.DaysName
import com.wem.snoozy.domain.usecase.EditAlarmUseCase
import com.wem.snoozy.presentation.utils.formatStringToDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@HiltViewModel(
    assistedFactory = EditAlarmViewModel.Factory::class
)
class EditAlarmViewModel @AssistedInject constructor(
    private val editAlarmUseCase: EditAlarmUseCase,
    private val userPreferencesManager: UserPreferencesManager,
    @Assisted("alarmItem") private val alarmItem: AlarmItem,
) : ViewModel() {


    private val cycleLength = MutableStateFlow("-1")
    private val sleepStartTime = MutableStateFlow("-1")

    // Edit alarm screen state
    private val _state = MutableStateFlow<EditAlarmState>(EditAlarmState.Initial)
    val editState = _state.asStateFlow()

    private val _cyclesList = MutableStateFlow<List<CycleItem>>(emptyList())

    private val initDaysList = listOf(
        DayItem(1, DaysName.MONDAY.getDisplayName(), false),
        DayItem(2, DaysName.TUESDAY.getDisplayName(), false),
        DayItem(3, DaysName.WEDNESDAY.getDisplayName(), false),
        DayItem(4, DaysName.THURSDAY.getDisplayName(), false),
        DayItem(5, DaysName.FRIDAY.getDisplayName(), false),
        DayItem(6, DaysName.SATURDAY.getDisplayName(), false),
        DayItem(7, DaysName.SUNDAY.getDisplayName(), false),
    )
    private val _daysList = MutableStateFlow(initDaysList)
    val editDaysList = _daysList.asStateFlow()

    val editSelectedCycleId = MutableStateFlow(-1)

    init {
        initializeState()
    }

    private fun initializeState() {
        viewModelScope.launch {
            cycleLength.value = userPreferencesManager.cycleLengthFlow.first()
            sleepStartTime.value = userPreferencesManager.sleepStartTimeFlow.first()
            applyCyclesList(
                LocalTime.of(
                    alarmItem.ringHours.split(":")[0].toInt(),
                    alarmItem.ringHours.split(":")[1].toInt(),
                )
            )
            _state.value = EditAlarmState.Content(
                selectedTime = LocalTime.of(
                    alarmItem.ringHours.split(":")[0].toInt(),
                    alarmItem.ringHours.split(":")[1].toInt(),
                ),
                cyclesList = _cyclesList.value,
                daysList = initDaysList,
                selectedDate = alarmItem.ringDay.formatStringToDate()
            )
        }
    }

    private fun toggleDay(id: Int) {
        Log.d("CyclesEdit", "toggle day")
        val currentList = _daysList.value.toMutableList()
        currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
        _daysList.value = currentList
    }

    private fun toggleCycle(id: Int) {
        val currentList = _cyclesList.value.toMutableList()
        if (currentList.find { it.checked && it.id == id } != null) {
            currentList.replaceAll { it.copy(checked = false) }
            Log.d("CyclesEdit", "All cycles unchecked")
            editSelectedCycleId.value = -1
        } else if (currentList.find { it.checked && it.id != id } != null) {
            currentList.replaceAll { it.copy(checked = false) }
            currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
            editSelectedCycleId.value = id
        } else {
            currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
            editSelectedCycleId.value = id
        }
        _cyclesList.value = currentList.sortedWith(
            compareByDescending<CycleItem> { it.checked }
                .thenByDescending { it.id }
        ).toMutableList()
    }

    private fun applyCyclesList(selectedTime: LocalTime) {
        Log.d("AddAlarm", "apply work ")
        var currentTime = selectedTime
        val newItems = mutableListOf<CycleItem>()

        for (i in 1..7) {
            val minusMinutes = currentTime
                .minusMinutes(cycleLength.value.toLong())
                .minusMinutes(sleepStartTime.value.toLong())

            val hours = minusMinutes.hour.toString()
            val minutes = minusMinutes.minute.toString().padStart(2, '0')
            val cycleItem = CycleItem(i, "$hours:$minutes", i.toString(), checked = false)
            newItems.add(cycleItem)
            currentTime = currentTime.minusMinutes(cycleLength.value.toLong())
        }

        if (_cyclesList.value.isNotEmpty() && newItems.toSet() == _cyclesList.value.map {
                it.copy(checked = false)
            }.toSet()) {
            return
        }

        _cyclesList.value = newItems.sortedByDescending { it.id }.toMutableList()
    }

    fun processCommand(command: EditAlarmCommand) {
        when (command) {
            is EditAlarmCommand.EditAlarm -> {
                viewModelScope.launch {
                    editAlarmUseCase(command.alarmItem)
                }
            }

            is EditAlarmCommand.SelectCycle -> {
                _state.update { prevState ->
                    toggleCycle(command.cycleId)
                    if (prevState is EditAlarmState.Content) {
                        prevState.copy(
                            cyclesList = _cyclesList.value
                        )
                    } else {
                        prevState
                    }
                }
            }

            is EditAlarmCommand.SelectDate -> {
                _state.update { prevState ->
                    if (prevState is EditAlarmState.Content) {
                        prevState.copy(
                            selectedDate = command.date,
                        )
                    } else {
                        prevState
                    }
                }
            }

            is EditAlarmCommand.SelectDay -> {
                _state.update { prevState ->
                    toggleDay(command.id)
                    if (prevState is EditAlarmState.Content) {
                        prevState.copy(
                            daysList = _daysList.value
                        ).also {
                            Log.d("CyclesEdit", _daysList.value.toString())
                        }
                    } else {
                        prevState
                    }
                }
            }

            is EditAlarmCommand.SelectTime -> {
                _state.update { prevState ->
                    applyCyclesList(command.time)
                    if (prevState is EditAlarmState.Content) {
                        prevState.copy(
                            selectedTime = command.time,
                            cyclesList = _cyclesList.value
                        )
                    } else {
                        prevState
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface Factory {

        fun create(
            @Assisted("alarmItem") alarmItem: AlarmItem
        ): EditAlarmViewModel
    }

}

sealed interface EditAlarmCommand {

    data class EditAlarm(
        val alarmItem: AlarmItem
    ) : EditAlarmCommand

    data class SelectCycle(
        val cycleId: Int
    ) : EditAlarmCommand

    data class SelectTime(
        val time: LocalTime
    ) : EditAlarmCommand

    data class SelectDay(
        val id: Int
    ) : EditAlarmCommand

    data class SelectDate(
        val date: LocalDate
    ) : EditAlarmCommand
}

sealed interface EditAlarmState {

    data object Initial : EditAlarmState


    data class Content(
        val selectedTime: LocalTime,
        val selectedDate: LocalDate,
        val daysList: List<DayItem>,
        val cyclesList: List<CycleItem>
    ) : EditAlarmState


    data object Loading : EditAlarmState
}