package com.wem.snoozy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [AlarmItemModel::class, GroupItemModel::class],
    version = 7, // Поднимаем версию
    exportSchema = false
)
abstract class AlarmDatabase : RoomDatabase(){

    abstract fun dao(): Dao

    companion object {

        private var instance: AlarmDatabase? = null

        private val lock = Any()

        private const val DB_NAME = "AlarmDatabase"

        fun getInstance(context: Context): AlarmDatabase {

            instance?.let { return it }

            synchronized(lock) {
                instance?.let { return it }

                val db = Room.databaseBuilder(
                    context = context,
                    klass = AlarmDatabase::class.java,
                    name = DB_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                instance = db
                return db
            }
        }
    }
}
