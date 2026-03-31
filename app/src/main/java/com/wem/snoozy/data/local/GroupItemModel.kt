package com.wem.snoozy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupItemModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val membersCount: Int,
    val contactIds: String,
    val avatarUri: String? = null
)
