package com.LuisS.menteoasis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.LuisS.menteoasis.data.dao.AttendanceDao
import com.LuisS.menteoasis.data.dao.BirthdayDao
import com.LuisS.menteoasis.data.entities.EmployeeEntity
import com.LuisS.menteoasis.data.entities.AttendanceRecordEntity
import com.LuisS.menteoasis.data.entities.BirthdayEntity
@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceRecordEntity::class,
        BirthdayEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mente_oasis_v2_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
