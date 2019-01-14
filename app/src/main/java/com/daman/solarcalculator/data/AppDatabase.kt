package com.daman.solarcalculator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.daman.solarcalculator.data.dao.UserLocationsDao
import com.daman.solarcalculator.data.entities.UserLocations

@Database(entities = [(UserLocations::class)], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userLocationsDao() : UserLocationsDao

    companion object {

        private var sInstance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (sInstance == null) {
                sInstance = Room
                        .databaseBuilder(context.applicationContext, AppDatabase::class.java, "solarcalc_db")
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return sInstance!!
        }
    }
}