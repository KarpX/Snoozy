package com.wem.snoozy.data.repository

import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.UserResponse
import com.wem.snoozy.domain.repository.UserRepository
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun checkUserByPhone(phoneNumber: String): UserResponse? {
        val digits = phoneNumber.filter { it.isDigit() }
        val formattedPhone = if (digits.startsWith("8") && digits.length == 11) {
            "7" + digits.substring(1)
        } else {
            digits
        }
        val response = apiService.checkPhone(formattedPhone)
        return if (response.isSuccessful) response.body() else null
    }

    override suspend fun getCurrentUser(): Result<UserResponse> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAvatar(file: MultipartBody.Part): Result<String> {
        return try {
            val response = apiService.uploadUserAvatar(file)
            if (response.isSuccessful) {
                Result.success(response.body()?.url ?: "")
            } else {
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserAvatar(): Result<ResponseBody> {
        return try {
            val response = apiService.getUserAvatar()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Download failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
