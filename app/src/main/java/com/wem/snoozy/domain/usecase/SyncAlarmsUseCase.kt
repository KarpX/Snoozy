package com.wem.snoozy.domain.usecase

import com.wem.snoozy.domain.repository.AlarmRepository
import javax.inject.Inject

class SyncAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke() {
        repository.syncRemoteAlarms()
    }
}
