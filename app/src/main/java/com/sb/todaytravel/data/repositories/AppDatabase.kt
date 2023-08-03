package com.sb.todaytravel.data.repositories

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sb.todaytravel.data.repositories.dao.TravelHistoryDao
import com.sb.todaytravel.data.repositories.dao.TravelLocationDao
import com.sb.todaytravel.data.repositories.entity.TravelHistory
import com.sb.todaytravel.data.repositories.entity.TravelLocation

@Database(entities = [TravelHistory::class, TravelLocation::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getTravelHistoryDao(): TravelHistoryDao

    abstract fun getTravelLocationDao(): TravelLocationDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }
        private const val DATABASE_NAME = "com.sb.todaytravel"
    }
}