package com.wem.snoozy.data.remote

import com.wem.snoozy.data.remote.dto.AuthResponse
import com.wem.snoozy.data.remote.dto.GoogleAuthRequest
import com.wem.snoozy.data.remote.dto.LoginRequest
import com.wem.snoozy.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
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
}
