package com.wem.snoozy.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.wem.snoozy.data.local.AlarmItemModel
import com.wem.snoozy.data.local.GroupItemModel
import com.wem.snoozy.data.remote.dto.GroupResponse
import com.wem.snoozy.data.remote.dto.MemberDto
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.domain.entity.Member
import com.wem.snoozy.presentation.utils.timeToMilli
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun AlarmItemModel.toAlarmItem() = AlarmItem(
    this.id,
    this.ringDay,
    this.ringHours,
    this.timeToBed,
    this.checked,
    this.repeatDays,
    this.isOverslept
)

@RequiresApi(Build.VERSION_CODES.O)
fun AlarmItem.toAlarmItemModel() = AlarmItemModel(
    this.id,
    this.ringDay,
    this.ringHours,
    timeToMilli(this.ringHours),
    this.timeToBed,
    this.checked,
    this.repeatDays,
    this.isOverslept
)

fun List<AlarmItemModel>.toAlarmItems() = this.map {
    it.toAlarmItem()
}

fun Flow<List<AlarmItemModel>>.toAlarmItemsFlow() = this.map { it.toAlarmItems() }

// Группы (Локальные)
fun GroupItemModel.toGroupItem() = GroupItem(
    id = this.id,
    name = this.name,
    membersCount = this.membersCount,
    contactIds = this.contactIds,
    avatarUri = this.avatarUri
)

fun GroupItem.toGroupItemModel() = GroupItemModel(
    id = this.id,
    name = this.name,
    membersCount = this.membersCount,
    contactIds = this.contactIds,
    avatarUri = this.avatarUri.fixUrl()
)

// Группы (Remote)
fun GroupResponse.toGroupItem() = GroupItem(
    id = this.id,
    name = this.name,
    ownerId = this.ownerId,
    avatarUri = this.url.fixUrl(),
    membersCount = this.members.size,
    members = this.members.map { it.toMember() }
)

fun MemberDto.toMember() = Member(
    id = this.id,
    username = this.username,
    avatarUrl = this.avatarUrl.fixUrl()
)

fun List<GroupItemModel>.toGroupItems() = this.map { it.toGroupItem() }

fun Flow<List<GroupItemModel>>.toGroupItemsFlow() = this.map { it.toGroupItems() }

fun String?.fixUrl(): String? {
    if (this == null) return null
    // Заменяем localhost на IP сервера из NetworkModule
    return if (this.contains("localhost")) {
        this.replace("localhost:8080", "45.156.22.247:8081")
            .replace("localhost", "45.156.22.247")
    } else {
        this
    }
}
