package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    suspend fun getWorkerById(id: Int): Worker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)
}

@Dao
interface OutwardBatchDao {
    @Query("SELECT * FROM outward_batches ORDER BY timestamp DESC")
    fun getAllBatches(): Flow<List<OutwardBatch>>

    @Query("SELECT * FROM outward_batches WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Int): OutwardBatch?

    @Query("SELECT * FROM outward_batches WHERE batchNumber = :batchNumber LIMIT 1")
    suspend fun getBatchByNumber(batchNumber: String): OutwardBatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: OutwardBatch): Long

    @Update
    suspend fun updateBatch(batch: OutwardBatch)

    @Delete
    suspend fun deleteBatch(batch: OutwardBatch)
}

@Dao
interface BarcodeItemDao {
    @Query("SELECT * FROM barcode_items ORDER BY sentTimestamp DESC")
    fun getAllBarcodes(): Flow<List<BarcodeItem>>

    @Query("SELECT * FROM barcode_items WHERE batchId = :batchId ORDER BY setIndex ASC")
    fun getBarcodesForBatch(batchId: Int): Flow<List<BarcodeItem>>

    @Query("SELECT * FROM barcode_items WHERE batchId = :batchId ORDER BY setIndex ASC")
    suspend fun getBarcodesForBatchSync(batchId: Int): List<BarcodeItem>

    @Query("SELECT * FROM barcode_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getBarcodeByString(barcode: String): BarcodeItem?

    @Query("SELECT * FROM barcode_items WHERE status = 'Sent' ORDER BY sentTimestamp DESC")
    fun getPendingBarcodes(): Flow<List<BarcodeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcodes(barcodes: List<BarcodeItem>)

    @Update
    suspend fun updateBarcode(barcode: BarcodeItem)
}

@Dao
interface LedgerTransactionDao {
    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE workerId = :workerId ORDER BY timestamp DESC")
    fun getTransactionsForWorker(workerId: Int): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE workerId = :workerId")
    suspend fun getTransactionsForWorkerSync(workerId: Int): List<LedgerTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: LedgerTransaction)
}
