package com.wem.snoozy.domain.entity

data class GroupItem(
    val id: Int = 0,
    val name: String,
    val ownerId: Int = 0,
    val membersCount: Int = 0,
    val contactIds: String = "", // Сохраняем для совместимости с локальной логикой
    val avatarUri: String? = null,
    val members: List<Member> = emptyList()
)

data class Member(
    val id: Int,
    val username: String,
    val avatarLink: String?,
    val upcomingAlarm: AlarmItem? = null,
    val missedAlarm: AlarmItem? = null
)
