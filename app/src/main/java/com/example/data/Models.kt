package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val location: String,
    val laborRate: Double,
    val rateType: String // "KG" or "SET"
)

@Entity(tableName = "outward_batches")
data class OutwardBatch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val batchNumber: String, // e.g. "JOB-2026-001"
    val workerId: Int,
    val workerName: String,
    val totalWeight: Double, // in KG
    val wastagePercentage: Double, // in %
    val allowedWastage: Double, // in KG
    val expectedNetWeight: Double, // in KG
    val totalSets: Int, // e.g. 30
    val grossWeightPerSet: Double, // e.g. 10.0
    val netWeightPerSet: Double, // e.g. 9.8
    val status: String, // "Pending", "Completed"
    val timestamp: Long,
    val notes: String = ""
)

@Entity(tableName = "barcode_items")
data class BarcodeItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val batchId: Int,
    val batchNumber: String,
    val workerId: Int,
    val workerName: String,
    val barcode: String, // unique e.g., "JOB-2026-001-SET01"
    val setIndex: Int, // 1 to 30
    val grossWeight: Double, // gross weight of this set e.g. 10.0
    val netWeight: Double, // net expected weight e.g. 9.8
    val status: String, // "Sent", "Received"
    val sentTimestamp: Long,
    val receivedTimestamp: Long? = null
)

@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: Int,
    val workerName: String,
    val batchId: Int? = null,
    val barcodeId: Int? = null,
    val barcodeString: String? = null,
    val type: String, // "Credit" (Worker Earned), "Debit" (Payment Made)
    val amount: Double,
    val paymentMethod: String? = null, // "Cash", "UPI"
    val description: String,
    val timestamp: Long
)
