package com.wem.snoozy.data.remote

import com.wem.snoozy.data.local.UserPreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {

    private const val BASE_URL = "http://45.156.22.247:8081/"
    
    // Временное решение для внедрения перехватчика без полноценного DI в этом объекте
    // В идеале всё должно быть через Hilt в NetworkModule
    private lateinit var authInterceptor: AuthInterceptor

    fun init(userPreferencesManager: UserPreferencesManager) {
        authInterceptor = AuthInterceptor(userPreferencesManager)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
        
        if (::authInterceptor.isInitialized) {
            builder.addInterceptor(authInterceptor)
        }
        
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
