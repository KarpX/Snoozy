package com.wem.snoozy.di

import android.content.Context
import com.wem.snoozy.data.local.AlarmDatabase
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.repository.AlarmRepositoryImpl
import com.wem.snoozy.domain.repository.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    companion object {

        @Provides
        @Singleton
        fun provideAlarmDatabase(
            @ApplicationContext context: Context
        ): AlarmDatabase {
            return AlarmDatabase.getInstance(context)
        }

        @Provides
        @Singleton
        fun provideDao(alarmDatabase: AlarmDatabase): Dao {
            return alarmDatabase.dao()
        }

        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): UserPreferencesManager {
            return UserPreferencesManager(context)
        }
    }
}