package com.wem.snoozy.domain.usecase

import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.repository.AlarmRepository
import javax.inject.Inject

class ToggleAlarmStateUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    suspend operator fun invoke(alarmItem: AlarmItem) {
        repository.toggleAlarmState(alarmItem)
    }
}