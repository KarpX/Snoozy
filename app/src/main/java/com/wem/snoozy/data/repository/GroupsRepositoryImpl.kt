package com.wem.snoozy.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toGroupItem
import com.wem.snoozy.data.mapper.toGroupItemModel
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.CreateGroupRequest
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.domain.repository.GroupRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject

class GroupsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: Dao
) : GroupRepository {

    override fun getGroups(): Flow<List<GroupItem>> = flow {
        try {
            val response = apiService.getGroups()
            if (response.isSuccessful) {
                val remoteGroups = response.body()?.map { it.toGroupItem() } ?: emptyList()
                Log.d("GroupsRepo", "Fetched ${remoteGroups.size} groups, enriching with alarms...")

                val enrichedGroups = coroutineScope {
                    remoteGroups.map { group ->
                        val enrichedMembers = group.members.map { member ->
                            async {
                                try {
                                    val alarmsResponse = apiService.getUserAlarms(member.id.toLong())
                                    val nearestAlarm = if (alarmsResponse.isSuccessful) {
                                        alarmsResponse.body()
                                            ?.filter { it.enabled }
                                            ?.minByOrNull { it.alarmTime }
                                            ?.let { 
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    it.toAlarmItem()
                                                } else {
                                                    null // Fallback for older versions if needed
                                                }
                                            }
                                    } else null
                                    member.copy(upcomingAlarm = nearestAlarm)
                                } catch (e: Exception) {
                                    Log.e("GroupsRepo", "Error fetching alarms for member ${member.id}", e)
                                    member
                                }
                            }
                        }.awaitAll()
                        group.copy(members = enrichedMembers)
                    }
                }
                
                emit(enrichedGroups)
                
                // Опционально: сохраняем в БД для кеша (но БД не поддерживает members с алармами сейчас)
                enrichedGroups.forEach { dao.addGroup(it.toGroupItemModel()) }
            } else {
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
                val url = response.body()?.url
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
            dao.deleteGroup(groupId)
        } catch (e: Exception) {
            dao.deleteGroup(groupId)
        }
    }

    override suspend fun getMemberAlarms(userId: Int): List<AlarmItem> {
        return try {
            val response = apiService.getUserAlarms(userId.toLong())
            if (response.isSuccessful) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    response.body()?.map { it.toAlarmItem() } ?: emptyList()
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("GroupsRepo", "Error fetching member alarms for user $userId", e)
            emptyList()
        }
    }
}
