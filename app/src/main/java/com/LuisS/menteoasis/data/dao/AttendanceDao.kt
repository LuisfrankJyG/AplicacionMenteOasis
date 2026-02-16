package com.LuisS.menteoasis.data.dao

import androidx.room.*
import com.LuisS.menteoasis.data.entities.AttendanceRecordEntity
import com.LuisS.menteoasis.data.entities.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM employees WHERE isActive = 1")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Insert
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY timestamp DESC")
    fun getRecordsForEmployee(employeeId: Int): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getRecordsByDateRange(start: Long, end: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>
}
