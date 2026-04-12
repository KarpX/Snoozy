package com.wem.snoozy.data.api

import com.wem.snoozy.data.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AlarmApiService {

    @GET("/api/v1/alarms")
    suspend fun getMyAlarms(): Response<List<AlarmDto>>

    @GET("/api/v1/alarms/users/{userId}")
    suspend fun getUserAlarms(@Path("userId") userId: Long): Response<List<AlarmDto>>

    @POST("/api/v1/alarms")
    suspend fun createAlarm(@Body request: CreateAlarmRequest): Response<AlarmDto>

    @PATCH("/api/v1/alarms/{alarmId}")
    suspend fun updateAlarm(@Path("alarmId") alarmId: Long, @Body request: UpdateAlarmRequest): Response<AlarmDto>

    @DELETE("/api/v1/alarms/{alarmId}")
    suspend fun deleteAlarm(@Path("alarmId") alarmId: Long): Response<Unit>

    @GET("/api/v1/alarms/permissions")
    suspend fun getPermissions(): Response<List<AlarmPermissionDto>>

    @POST("/api/v1/alarms/permissions")
    suspend fun grantPermission(@Body request: GrantPermissionRequest): Response<AlarmPermissionDto>

    @POST("/api/v1/alarms/{alarmId}/trigger")
    suspend fun triggerAlarm(@Path("alarmId") alarmId: Long, @Body request: TriggerRequest): Response<AlarmActionDto>

    @POST("/api/v1/alarms/{alarmId}/enable")
    suspend fun enableAlarm(@Path("alarmId") alarmId: Long): Response<AlarmActionDto>

    @POST("/api/v1/alarms/{alarmId}/disable")
    suspend fun disableAlarm(@Path("alarmId") alarmId: Long): Response<AlarmActionDto>

    @GET("/api/v1/alarms/actions/incoming")
    suspend fun getIncomingActions(): Response<List<AlarmActionDto>>

    @GET("/api/v1/alarms/health")
    suspend fun healthCheck(): Response<HealthDto>
}