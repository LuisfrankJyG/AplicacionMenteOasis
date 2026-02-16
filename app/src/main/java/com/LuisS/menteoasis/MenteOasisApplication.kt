package com.LuisS.menteoasis

import android.app.Application
import com.LuisS.menteoasis.data.AppDatabase
import com.LuisS.menteoasis.data.MenteOasisRepository

class MenteOasisApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        MenteOasisRepository(
            database.attendanceDao(),
            database.birthdayDao()
        ) 
    }
}
