package com.wem.snoozy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAlarm(alarmItemModel: AlarmItemModel): Long

    @Query("SELECT * FROM alarms ORDER BY ringHoursMillis ASC")
    fun getAlarms(): Flow<List<AlarmItemModel>>

    @Query("UPDATE alarms SET checked = :isChecked WHERE id = :alarmId")
    suspend fun updateCheckedStatus(alarmId: Int, isChecked: Boolean)

    @Query("DELETE FROM alarms WHERE id= :alarmId")
    suspend fun deleteAlarm(alarmId: Int)
}