package com.LuisS.menteoasis.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthdays")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val day: Int,
    val month: Int, // 1-12
    val year: Int? = null, // Optional birth year
    val reminderConfig: Int = 0 // Bitmask for reminders (1=SameDay, 2=1DayBefore, etc.)
)
