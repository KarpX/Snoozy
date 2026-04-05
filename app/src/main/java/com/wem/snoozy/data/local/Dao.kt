package com.wem.snoozy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    // Будильники
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAlarm(alarmItemModel: AlarmItemModel): Long

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Int): AlarmItemModel?

    @Query("SELECT * FROM alarms ORDER BY ringHoursMillis ASC")
    fun getAlarms(): Flow<List<AlarmItemModel>>

    @Query("UPDATE alarms SET checked = :isChecked WHERE id = :alarmId")
    suspend fun updateCheckedStatus(alarmId: Int, isChecked: Boolean)

    @Query("UPDATE alarms SET isOverslept = :isOverslept WHERE id = :alarmId")
    suspend fun updateOversleptStatus(alarmId: Int, isOverslept: Boolean)

    @Query("DELETE FROM alarms WHERE id= :alarmId")
    suspend fun deleteAlarm(alarmId: Int)

    // Группы
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGroup(groupItemModel: GroupItemModel): Long

    @Query("SELECT * FROM `groups` ORDER BY id DESC")
    fun getGroups(): Flow<List<GroupItemModel>>

    @Query("SELECT * FROM `groups` ORDER BY id DESC")
    suspend fun getGroupsOnce(): List<GroupItemModel>

    @Query("UPDATE `groups` SET avatarUri = :url WHERE id = :groupId")
    suspend fun updateGroupAvatar(groupId: Int, url: String)

    @Query("DELETE FROM `groups` WHERE id = :groupId")
    suspend fun deleteGroup(groupId: Int)
}
