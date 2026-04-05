package com.wem.snoozy.data.repository

import android.util.Log
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.data.remote.dto.UserResponse
import com.wem.snoozy.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {
    override suspend fun checkUserByPhone(phoneNumber: String): UserResponse? {
            val formattedPhone = formatPhoneNumber(phoneNumber)
            val response = apiService.checkPhone(formattedPhone)
            return if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
    }

    private fun formatPhoneNumber(phone: String): String {
        // Удаляем все нецифровые символы
        val digits = phone.filter { it.isDigit() }
        // Если начинается с 8, заменяем на 7
        return if (digits.startsWith("8") && digits.length == 11) {
            "7" + digits.substring(1)
        } else {
            digits
        }
    }
}
