package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BillRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bill_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<BillRecord>>

    @Query("SELECT * FROM bill_history ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRecord(): Flow<BillRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BillRecord): Long

    @Delete
    suspend fun deleteRecord(record: BillRecord)

    @Query("DELETE FROM bill_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bill_history")
    suspend fun clearAll()
}
