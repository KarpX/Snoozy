package com.wem.snoozy.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.wem.snoozy.data.local.AlarmItemModel
import com.wem.snoozy.data.local.GroupItemModel
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.presentation.utils.timeToMilli
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun AlarmItemModel.toAlarmItem() = AlarmItem(
    this.id,
    this.ringDay,
    this.ringHours,
    this.timeToBed,
    this.checked,
    this.repeatDays
)

@RequiresApi(Build.VERSION_CODES.O)
fun AlarmItem.toAlarmItemModel() = AlarmItemModel(
    this.id,
    this.ringDay,
    this.ringHours,
    timeToMilli(this.ringHours),
    this.timeToBed,
    this.checked,
    this.repeatDays
)

fun List<AlarmItemModel>.toAlarmItems() = this.map {
    it.toAlarmItem()
}

fun Flow<List<AlarmItemModel>>.toAlarmItemsFlow() = this.map { it.toAlarmItems() }

// Группы
fun GroupItemModel.toGroupItem() = GroupItem(
    this.id,
    this.name,
    this.membersCount,
    this.contactIds
)

fun GroupItem.toGroupItemModel() = GroupItemModel(
    this.id,
    this.name,
    this.membersCount,
    this.contactIds
)

fun List<GroupItemModel>.toGroupItems() = this.map { it.toGroupItem() }

fun Flow<List<GroupItemModel>>.toGroupItemsFlow() = this.map { it.toGroupItems() }
