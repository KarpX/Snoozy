package com.wem.snoozy.domain.usecase

import com.wem.snoozy.domain.repository.AlarmRepository
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    suspend operator fun invoke(alarmId: Int) {
        repository.deleteAlarm(alarmId)
    }
}