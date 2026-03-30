package com.wem.snoozy.domain.usecase

import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    operator fun invoke(): Flow<List<AlarmItem>> {
        return repository.getAllAlarms()
    }
}