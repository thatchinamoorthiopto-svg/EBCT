package com.example.data.repository

import com.example.data.local.BillDao
import com.example.data.model.BillRecord
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {

    fun getAllRecords(): Flow<List<BillRecord>> = billDao.getAllRecords()

    fun getLatestRecord(): Flow<BillRecord?> = billDao.getLatestRecord()

    suspend fun saveRecord(record: BillRecord): Long {
        return billDao.insertRecord(record)
    }

    suspend fun deleteRecord(record: BillRecord) {
        billDao.deleteRecord(record)
    }

    suspend fun deleteById(id: Long) {
        billDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        billDao.clearAll()
    }
}
