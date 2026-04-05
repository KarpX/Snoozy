package com.wem.snoozy.domain.repository

import com.wem.snoozy.data.remote.dto.UserResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody

interface UserRepository {
    suspend fun checkUserByPhone(phoneNumber: String): UserResponse?
    suspend fun getCurrentUser(): Result<UserResponse>
    suspend fun uploadAvatar(file: MultipartBody.Part): Result<String>
    suspend fun getUserAvatar(): Result<ResponseBody>
}
