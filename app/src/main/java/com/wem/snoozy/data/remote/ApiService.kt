package com.wem.snoozy.data.remote

import com.wem.snoozy.data.remote.dto.AuthResponse
import com.wem.snoozy.data.remote.dto.GoogleAuthRequest
import com.wem.snoozy.data.remote.dto.LoginRequest
import com.wem.snoozy.data.remote.dto.RegisterRequest
import com.wem.snoozy.data.remote.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

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

    @GET("api/v1/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>
}
