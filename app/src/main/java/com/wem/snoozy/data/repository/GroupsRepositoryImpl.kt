package com.wem.snoozy.data.repository

import android.os.Build
import android.util.Log
import com.wem.snoozy.data.dto.GrantPermissionRequest
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.local.UserPreferencesManager
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import java.time.LocalDateTime
import javax.inject.Inject

class GroupsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val dao: Dao,
    private val userPreferencesManager: UserPreferencesManager
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
                                    val alarmsResponse =
                                        apiService.getUserAlarms(member.id.toLong())
                                    if (alarmsResponse.isSuccessful) {
                                        val alarms = alarmsResponse.body() ?: emptyList()

                                        Log.d("GroupsRepo", alarms.toString())

                                        // 1. Ищем последний пропущенный будильник (overslept)
                                        val missedAlarm = alarms
                                            .filter { it.overslept }
                                            .maxByOrNull { it.alarmTime }
                                            ?.let {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) it.toAlarmItem() else null
                                            }

                                        // 2. Ищем ближайший активный будущий будильник
                                        val now = LocalDateTime.now().toString()
                                        val upcomingAlarm = alarms
                                            .filter { it.enabled && !it.overslept }
                                            .filter { it.alarmTime >= now }
                                            .minByOrNull { it.alarmTime }
                                            ?.toAlarmItem()

                                        member.copy(
                                            upcomingAlarm = upcomingAlarm,
                                            missedAlarm = missedAlarm
                                        )
                                    } else {
                                        member
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        "GroupsRepo",
                                        "Error fetching alarms for member ${member.id}",
                                        e
                                    )
                                    member
                                }
                            }
                        }.awaitAll()
                        group.copy(members = enrichedMembers)
                    }
                }

                emit(enrichedGroups)

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
                val groupResponse = response.body()
                val group = groupResponse?.toGroupItem()
                group?.let { dao.addGroup(it.toGroupItemModel()) }
                
                // Автоматически выдаем права всем участникам группы (кроме себя)
                val currentUserId = userPreferencesManager.userIdFlow.first()
                groupResponse?.members?.forEach { member ->
                    if (member.id != currentUserId) {
                        try {
                            apiService.grantPermission(
                                GrantPermissionRequest(
                                    targetUserId = member.id.toLong(),
                                    permissionType = "TRIGGER"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("GroupsRepo", "Failed to grant permission to user ${member.id}", e)
                        }
                    }
                }

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
                    response.body()
                        ?.filter { it.enabled }
                        ?.map { it.toAlarmItem() } ?: emptyList()
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
