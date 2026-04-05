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
    avatarUri = this.avatarUri
)

// Группы (Remote)
fun GroupResponse.toGroupItem(): GroupItem {
    val rawUrl = this.avatarUrl ?: this.url
    val fixedAvatarUri = rawUrl.fixUrl("group")
    
    Log.d("Mappers", "Group [${this.name}] (id: ${this.id}): rawUrl=$rawUrl, fixed=$fixedAvatarUri")
    
    return GroupItem(
        id = this.id,
        name = this.name,
        ownerId = this.ownerId,
        avatarUri = fixedAvatarUri,
        membersCount = this.members.size,
        members = this.members.map { it.toMember() }
    )
}

fun MemberDto.toMember(): Member {
    val fixedAvatarUrl = this.avatarUrl.fixUrl("user")
    Log.d("Mappers", "  Member [${this.username}] (id: ${this.id}): avatarUrl=${this.avatarUrl}, fixed=$fixedAvatarUrl")
    
    return Member(
        id = this.id,
        username = this.username,
        avatarUrl = fixedAvatarUrl
    )
}

fun List<GroupItemModel>.toGroupItems() = this.map { it.toGroupItem() }

fun Flow<List<GroupItemModel>>.toGroupItemsFlow() = this.map { it.toGroupItems() }

fun String?.fixUrl(type: String = "file"): String? {
    if (this == null || this.isBlank()) return null
    
    // 1. Исправляем хост
    var result = if (this.contains("localhost")) {
        this.replace(Regex("localhost:\\d+"), "45.156.22.247:8081")
            .replace("localhost", "45.156.22.247")
    } else {
        this
    }

    // 2. Добавляем уникальный маркер типа в начало параметров, чтобы Coil не путал кэш
    result = if (result.contains("?")) {
        result.replace("?", "?t=$type&")
    } else {
        "$result?t=$type"
    }

    return result
}
