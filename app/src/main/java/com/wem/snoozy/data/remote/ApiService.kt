package com.wem.snoozy.data.remote

import com.wem.snoozy.data.dto.*
import com.wem.snoozy.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/v1/auth/basic/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<AuthResponse>

    @POST("api/v1/auth/basic/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AuthResponse>

    @POST("api/v1/auth/google")
    suspend fun googleAuth(
        @Body request: GoogleAuthRequest
    ): Response<AuthResponse>

    // Groups
    @GET("api/v1/groups")
    suspend fun getGroups(): Response<List<GroupResponse>>

    @GET("api/v1/groups/{id}")
    suspend fun getGroupById(
        @Path("id") id: Int
    ): Response<GroupResponse>

    @POST("api/v1/groups")
    suspend fun createGroup(
        @Body request: CreateGroupRequest
    ): Response<GroupResponse>

    @Multipart
    @POST("api/v1/groups/avatar/{id}")
    suspend fun uploadGroupAvatar(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<AvatarResponse>

    @GET("api/v1/users/phone")
    suspend fun checkPhone(
        @Query("phoneNumber") phoneNumber: String
    ): Response<UserResponse>

    // User Profile
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @Multipart
    @POST("api/v1/users/avatar")
    suspend fun uploadUserAvatar(
        @Part file: MultipartBody.Part
    ): Response<AvatarResponse>

    @GET("api/v1/users/avatar")
    suspend fun getUserAvatar(): Response<ResponseBody>

    // Alarm API
    @GET("api/v1/alarms")
    suspend fun getMyAlarms(): Response<List<AlarmDto>>

    @GET("api/v1/alarms/users/{userId}")
    suspend fun getUserAlarms(@Path("userId") userId: Long): Response<List<AlarmDto>>

    @POST("api/v1/alarms")
    suspend fun createAlarm(@Body request: CreateAlarmRequest): Response<AlarmDto>

    @PATCH("api/v1/alarms/{alarmId}")
    suspend fun updateAlarm(@Path("alarmId") alarmId: Long, @Body request: UpdateAlarmRequest): Response<AlarmDto>

    @DELETE("api/v1/alarms/{alarmId}")
    suspend fun deleteAlarm(@Path("alarmId") alarmId: Long): Response<Unit>

    @GET("api/v1/alarms/permissions")
    suspend fun getPermissions(): Response<List<AlarmPermissionDto>>

    @POST("api/v1/alarms/permissions")
    suspend fun grantPermission(@Body request: GrantPermissionRequest): Response<AlarmPermissionDto>

    @POST("api/v1/alarms/{alarmId}/trigger")
    suspend fun triggerAlarm(@Path("alarmId") alarmId: Long, @Body request: TriggerRequest): Response<AlarmActionDto>

    @POST("api/v1/alarms/{alarmId}/enable")
    suspend fun enableAlarm(@Path("alarmId") alarmId: Long): Response<AlarmActionDto>

    @POST("api/v1/alarms/{alarmId}/disable")
    suspend fun disableAlarm(@Path("alarmId") alarmId: Long): Response<AlarmActionDto>

    @GET("api/v1/alarms/actions/incoming")
    suspend fun getIncomingActions(): Response<List<AlarmActionDto>>

    @GET("api/v1/alarms/health")
    suspend fun healthCheck(): Response<HealthDto>

}
