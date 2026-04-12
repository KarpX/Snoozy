package com.wem.snoozy.domain.repository

import com.wem.snoozy.domain.entity.GroupItem
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun syncGroups()

    suspend fun addGroup(groupItem: GroupItem)

    fun getGroups(): Flow<List<GroupItem>>

    suspend fun deleteGroup(groupId: Int)
    
    suspend fun getMemberAlarms(userId: Int): List<com.wem.snoozy.domain.entity.AlarmItem>
}
