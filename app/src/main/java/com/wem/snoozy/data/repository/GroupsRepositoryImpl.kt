package com.wem.snoozy.data.repository

import android.util.Log
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.fixUrl
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

    // Теперь получаем группы напрямую из API
    override fun getGroups(): Flow<List<GroupItem>> = flow {
        try {
            val response = apiService.getGroups()
            if (response.isSuccessful) {
                val remoteGroups = response.body()?.map { it.toGroupItem() } ?: emptyList()
                Log.d("GroupsRepo", "Fetched ${remoteGroups.size} groups directly from API")
                
                // Опционально: сохраняем в БД для кеша
                remoteGroups.forEach { dao.addGroup(it.toGroupItemModel()) }
                
                emit(remoteGroups)
            } else {
                // Если API упал, пытаемся взять из БД
                Log.e("GroupsRepo", "API error: ${response.code()}, falling back to DB")
                emit(dao.getGroupsOnce().map { it.toGroupItem() })
            }
        } catch (e: Exception) {
            Log.e("GroupsRepo", "Network error, falling back to DB", e)
            emit(dao.getGroupsOnce().map { it.toGroupItem() })
        }
    }

    override suspend fun syncGroups() {
        // Метод остается для принудительного обновления, если нужно
        try {
            val response = apiService.getGroups()
            if (response.isSuccessful) {
                response.body()?.forEach { 
                    dao.addGroup(it.toGroupItem().toGroupItemModel()) 
                }
            }
        } catch (e: Exception) {
            Log.e("GroupsRepo", "Sync error", e)
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
                val url = response.body()?.url?.fixUrl()
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

    override suspend fun deleteGroup(groupId: Int) {
        try {
            // Здесь в идеале должен быть вызов API для удаления на сервере
            dao.deleteGroup(groupId)
        } catch (e: Exception) {
            dao.deleteGroup(groupId)
        }
    }
}
