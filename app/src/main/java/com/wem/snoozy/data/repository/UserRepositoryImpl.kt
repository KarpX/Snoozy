package com.wem.snoozy.data.repository

import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.UserResponse
import com.wem.snoozy.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userPreferencesManager: UserPreferencesManager
) : UserRepository {

    override suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val token = userPreferencesManager.accessTokenFlow.first()
            if (token == null) {
                return Result.failure(Exception("No access token found"))
            }
            
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch user data: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
