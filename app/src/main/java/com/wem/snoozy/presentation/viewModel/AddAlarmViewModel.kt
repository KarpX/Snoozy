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
import com.wem.snoozy.domain.usecase.AddNewAlarmUseCase
import com.wem.snoozy.domain.usecase.EditAlarmUseCase
import com.wem.snoozy.presentation.utils.formatStringToDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for add alarm bottom sheet
 *
 * @param userPreferencesManager manager for working with local storage
 */
@HiltViewModel
open class AddAlarmViewModel @Inject constructor(
    private val addNewAlarmUseCase: AddNewAlarmUseCase,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    // TODO: ДОБАВИТЬ СЕРИАЛИЗАЦИЮ ДНЕЙ ПРОЗВОНА БУДИЛЬНИКА ЧЕРЕЗ JSON

    private val cycleLength = MutableStateFlow(INIT_CYCLE_LENGTH)
    private val sleepStartTime = MutableStateFlow(INIT_SLEEP_START_TIME)

    // Add alarm screen state
    private val _state = MutableStateFlow<AddAlarmState>(AddAlarmState.Loading)
    val state = _state.asStateFlow()

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
    val daysList = _daysList.asStateFlow()

    val selectedCycleId = MutableStateFlow(INIT_SELECTED_ITEM_ID)

    init {
        initializeState()
    }

    private fun initializeState() {
        viewModelScope.launch {
            cycleLength.value = userPreferencesManager.cycleLengthFlow.first()
            sleepStartTime.value = userPreferencesManager.sleepStartTimeFlow.first()
            applyCyclesList(LocalTime.now())
            _state.value = AddAlarmState.Content(
                selectedTime = LocalTime.now(),
                cyclesList = _cyclesList.value,
                daysList = initDaysList,
                selectedDate = LocalDate.now()
            )
        }
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

    fun processCommand(command: AddAlarmCommand) {
        when (command) {
            is AddAlarmCommand.SaveAlarm -> {
                viewModelScope.launch {
                    addNewAlarmUseCase(command.alarmItem)
                }
            }

            is AddAlarmCommand.SelectCycle -> {
                _state.update { prevState ->
                    toggleCycle(command.cycleId)
                    if (prevState is AddAlarmState.Content) {
                        prevState.copy(
                            cyclesList = _cyclesList.value
                        )
                    } else {
                        prevState
                    }
                }
            }

            is AddAlarmCommand.SelectTime -> {
                _state.update { prevState ->
                    Log.d("MainViewModel", (prevState is AddAlarmState.Content).toString())
                    applyCyclesList(command.time)
                    if (prevState is AddAlarmState.Content) {
                        prevState.copy(
                            selectedTime = command.time,
                            cyclesList = _cyclesList.value
                        )
                    } else {
                        prevState
                    }
                }
            }

            is AddAlarmCommand.SelectDay -> {
                _state.update { prevState ->
                    toggleDay(command.id)
                    if (prevState is AddAlarmState.Content) {
                        prevState.copy(
                            daysList = _daysList.value
                        )
                    } else {
                        prevState
                    }
                }
            }

            is AddAlarmCommand.SelectDate -> {
                _state.update { prevState ->
                    if (prevState is AddAlarmState.Content) {
                        prevState.copy(
                            selectedDate = command.date,
                        )
                    } else {
                        prevState
                    }
                }
            }
        }
    }

    private fun toggleDay(id: Int) {
        val currentList = _daysList.value.toMutableList()
        currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
        _daysList.value = currentList
    }

    private fun toggleCycle(id: Int) {
        val currentList = _cyclesList.value.toMutableList()
        if (currentList.find { it.checked && it.id == id } != null) {
            currentList.replaceAll { it.copy(checked = false) }
            Log.d("Cycles", "All cycles unchecked")
            selectedCycleId.value = -1
        } else if (currentList.find { it.checked && it.id != id } != null) {
            currentList.replaceAll { it.copy(checked = false) }
            currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
            selectedCycleId.value = id
        } else {
            currentList.replaceAll { if (it.id == id) it.copy(checked = !it.checked) else it }
            selectedCycleId.value = id
        }
        _cyclesList.value = currentList.sortedWith(
            compareByDescending<CycleItem> { it.checked }
                .thenByDescending { it.id }
        ).toMutableList()
    }

    companion object {

        const val INIT_CYCLE_LENGTH = "-1"
        const val INIT_SLEEP_START_TIME = "-1"

        const val INIT_SELECTED_ITEM_ID = -1
    }
}

sealed interface AddAlarmCommand {

    data class SaveAlarm(
        val alarmItem: AlarmItem
    ) : AddAlarmCommand

    data class SelectCycle(
        val cycleId: Int
    ) : AddAlarmCommand

    data class SelectTime(
        val time: LocalTime
    ) : AddAlarmCommand

    data class SelectDay(
        val id: Int
    ) : AddAlarmCommand

    data class SelectDate(
        val date: LocalDate
    ) : AddAlarmCommand

}

sealed interface AddAlarmState {

    data object Initial : AddAlarmState

    data class Content(
        val selectedTime: LocalTime,
        val selectedDate: LocalDate,
        val daysList: List<DayItem>,
        val cyclesList: List<CycleItem>
    ) : AddAlarmState

    data object Loading : AddAlarmState
}



