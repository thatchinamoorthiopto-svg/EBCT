package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_history")
data class BillRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val state: String = "Tamil Nadu",
    val connectionType: String,
    val billingPeriod: String,
    val previousReading: Double,
    val currentReading: Double,
    val previousReadingDate: String,
    val currentReadingDate: String,
    val unitsConsumed: Double,
    val energyCharges: Double,
    val fixedCharges: Double,
    val adjustments: Double,
    val subsidyAmount: Double,
    val estimatedTotal: Double,
    val meterPhotoUri: String? = null,
    val notes: String? = null
)
