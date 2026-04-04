package com.wem.snoozy.domain.entity

data class GroupItem(
    val id: Int = 0,
    val name: String,
    val membersCount: Int,
    val contactIds: String,
    val avatarUri: String? = null
)
