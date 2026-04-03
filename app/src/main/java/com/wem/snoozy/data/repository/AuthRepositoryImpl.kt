package com.wem.snoozy.data.repository

import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.AuthResponse
import com.wem.snoozy.data.remote.dto.GoogleAuthRequest
import com.wem.snoozy.data.remote.dto.LoginRequest
import com.wem.snoozy.data.remote.dto.RegisterRequest
import com.wem.snoozy.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userPreferencesManager: UserPreferencesManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                userPreferencesManager.saveAccessToken(authResponse.accessToken)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                userPreferencesManager.saveAccessToken(authResponse.accessToken)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleAuth(request: GoogleAuthRequest): Result<AuthResponse> {
        return try {
            val response = apiService.googleAuth(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                userPreferencesManager.saveAccessToken(authResponse.accessToken)
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Google auth failed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        userPreferencesManager.clearAccessToken()
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return userPreferencesManager.hasToken()
    }
}
