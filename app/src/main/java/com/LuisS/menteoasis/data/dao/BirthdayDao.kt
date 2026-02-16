package com.LuisS.menteoasis.data.dao

import androidx.room.*
import com.LuisS.menteoasis.data.entities.BirthdayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {
    @Query("SELECT * FROM birthdays ORDER BY month ASC, day ASC")
    fun getAllBirthdays(): Flow<List<BirthdayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: BirthdayEntity): Long

    @Delete
    suspend fun deleteBirthday(birthday: BirthdayEntity): Int
}
