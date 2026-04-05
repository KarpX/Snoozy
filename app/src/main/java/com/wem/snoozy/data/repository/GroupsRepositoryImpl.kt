package com.wem.snoozy.data.repository

import android.util.Log
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toGroupItem
import com.wem.snoozy.data.mapper.toGroupItemModel
import com.wem.snoozy.data.mapper.toGroupItems
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.CreateGroupRequest
import com.wem.snoozy.domain.entity.GroupItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

class GroupsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: Dao
) {
    fun getGroups(): Flow<List<GroupItem>> = flow {
        // Сначала отдаем локальные данные
        val localGroups = dao.getGroupsOnce().toGroupItems()
        emit(localGroups)

        try {
            val response = apiService.getGroups()
            if (response.isSuccessful) {
                val remoteGroups = response.body()?.map { it.toGroupItem() } ?: emptyList()
                
                // Обновляем БД
                remoteGroups.forEach { group ->
                    dao.addGroup(group.toGroupItemModel())
                }
                
                emit(remoteGroups)
            }
        } catch (e: Exception) {
            Log.e("GroupsRepo", "Error fetching groups", e)
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
}
