package com.wem.snoozy.data.repository

import android.util.Log
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toGroupItem
import com.wem.snoozy.data.mapper.toGroupItemModel
import com.wem.snoozy.data.mapper.toGroupItems
import com.wem.snoozy.data.mapper.toGroupItemsFlow
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.CreateGroupRequest
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

class GroupsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: Dao
) : GroupRepository {
    override suspend fun syncGroups() {
        try {
            val response = apiService.getGroups()
            if (response.isSuccessful) {
                val remoteGroups = response.body()?.map { it.toGroupItem() } ?: emptyList()

                // Сохраняем в БД.
                // Поскольку getGroups() ниже слушает БД, UI обновится сам.
                remoteGroups.forEach { group ->
                    dao.addGroup(group.toGroupItemModel())
                }
            }
        } catch (e: Exception) {
            Log.e("GroupsRepo", "Error syncing groups", e)
        }
    }

    suspend fun createGroup(name: String, memberIds: List<Int>): GroupItem? {
        return try {
            val response = apiService.createGroup(CreateGroupRequest(name, memberIds))
            if (response.isSuccessful) {
                val group = response.body()?.toGroupItem()
                group?.let { dao.addGroup(it.toGroupItemModel()) }
                group
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun uploadAvatar(groupId: Int, file: MultipartBody.Part): String? {
        return try {
            val response = apiService.uploadGroupAvatar(groupId, file)
            if (response.isSuccessful) {
                val url = response.body()?.url
                // Обновляем URL в локальной БД
                url?.let { dao.updateGroupAvatar(groupId, it) }
                url
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addGroup(groupItem: GroupItem) {
        dao.addGroup(groupItem.toGroupItemModel())
    }

    override fun getGroups(): Flow<List<GroupItem>> {
        return dao.getGroups().toGroupItemsFlow()
    }

    override suspend fun deleteGroup(groupId: Int) {
        dao.deleteGroup(groupId)
    }
}
