package com.wem.snoozy.di

import android.content.Context
import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.local.AlarmDatabase
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.repository.AlarmRepositoryImpl
import com.wem.snoozy.data.repository.AuthRepositoryImpl
import com.wem.snoozy.data.repository.ContactRepositoryImpl
import com.wem.snoozy.data.repository.GroupsRepositoryImpl
import com.wem.snoozy.domain.repository.AlarmRepository
import com.wem.snoozy.domain.repository.AuthRepository
import com.wem.snoozy.domain.repository.ContactRepository
import com.wem.snoozy.domain.repository.GroupRepository
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

    @Binds
    @Singleton
    fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    fun bindGroupRepository(impl: GroupsRepositoryImpl): GroupRepository

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

        @Provides
        @Singleton
        fun provideAlarmScheduler(
            @ApplicationContext context: Context
        ): AlarmScheduler {
            return AlarmScheduler(context)
        }
    }
}