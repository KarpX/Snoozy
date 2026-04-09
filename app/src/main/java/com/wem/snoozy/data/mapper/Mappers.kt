package com.wem.snoozy.data.mapper

import android.os.Build
import android.util.Log
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
    this.remoteId,
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
    this.remoteId,
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
    avatarUri = this.avatarUri
)

// Группы (Remote)
fun GroupResponse.toGroupItem(): GroupItem {
    val rawUrl = this.avatarUrl ?: this.url
    
    Log.d("Mappers", "Group [${this.name}] (id: ${this.id}): rawUrl=$rawUrl")
    
    return GroupItem(
        id = this.id,
        name = this.name,
        ownerId = this.ownerId,
        avatarUri = rawUrl,
        membersCount = this.members.size,
        members = this.members.map { it.toMember() }
    )
}

fun MemberDto.toMember(): Member {
    Log.d("Mappers", "  Member [${this.username}] (id: ${this.id}): avatarLink=${this.avatarUrl}")
    
    return Member(
        id = this.id,
        username = this.username,
        avatarLink = avatarUrl
    )
}

fun List<GroupItemModel>.toGroupItems() = this.map { it.toGroupItem() }

fun Flow<List<GroupItemModel>>.toGroupItemsFlow() = this.map { it.toGroupItems() }