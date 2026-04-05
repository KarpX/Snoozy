package com.wem.snoozy.domain.repository

import com.wem.snoozy.domain.entity.GroupItem
import kotlinx.coroutines.flow.Flow

public interface GroupRepository
{
    suspend fun syncGroups()

    suspend fun addGroup(groupItem: GroupItem)

    fun getGroups(): Flow<List<GroupItem>>

    suspend fun deleteGroup(groupId: Int)
}
