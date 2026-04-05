package com.wem.snoozy.data.remote.dto

data class CreateGroupRequest(
    val name: String,
    val membersId: List<Int>
)
