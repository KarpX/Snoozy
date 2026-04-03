package com.wem.snoozy.domain.repository

import com.wem.snoozy.data.remote.dto.UserResponse

interface UserRepository {
    suspend fun getCurrentUser(): Result<UserResponse>
}
