package com.wem.snoozy.data.remote

import com.wem.snoozy.data.remote.dto.*
import okhttp3.MultipartBody
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

}
