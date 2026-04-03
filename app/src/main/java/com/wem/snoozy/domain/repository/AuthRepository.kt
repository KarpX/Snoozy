package com.wem.snoozy.domain.repository

import com.wem.snoozy.data.remote.dto.AuthResponse
import com.wem.snoozy.data.remote.dto.GoogleAuthRequest
import com.wem.snoozy.data.remote.dto.LoginRequest
import com.wem.snoozy.data.remote.dto.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun googleAuth(request: GoogleAuthRequest): Result<AuthResponse>
    suspend fun logout()
    suspend fun isUserLoggedIn(): Boolean
}
