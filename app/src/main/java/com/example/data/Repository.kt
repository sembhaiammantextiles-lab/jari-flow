package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ScanResult {
    data class Success(val barcode: BarcodeItem, val creditEarned: Double) : ScanResult()
    data class AlreadyReceived(val barcodeName: String, val receivedTime: Long) : ScanResult()
    object NotFound : ScanResult()
}

class JariRepository(private val database: AppDatabase) {
    private val workerDao = database.workerDao()
    private val batchDao = database.outwardBatchDao()
    private val barcodeDao = database.barcodeItemDao()
    private val ledgerDao = database.ledgerTransactionDao()

    // Workers
    val allWorkers: Flow<List<Worker>> = workerDao.getAllWorkers()
    
    suspend fun getWorker(id: Int): Worker? = workerDao.getWorkerById(id)
    suspend fun insertWorker(worker: Worker) = workerDao.insertWorker(worker)
    suspend fun updateWorker(worker: Worker) = workerDao.updateWorker(worker)
    suspend fun deleteWorker(worker: Worker) = workerDao.deleteWorker(worker)

    // Batches
    val allBatches: Flow<List<OutwardBatch>> = batchDao.getAllBatches()
    
    suspend fun getBatch(id: Int): OutwardBatch? = batchDao.getBatchById(id)
    suspend fun deleteBatch(batch: OutwardBatch) = batchDao.deleteBatch(batch)

    // Barcodes
    val allBarcodes: Flow<List<BarcodeItem>> = barcodeDao.getAllBarcodes()
    val pendingBarcodes: Flow<List<BarcodeItem>> = barcodeDao.getPendingBarcodes()

    fun getBarcodesForBatch(batchId: Int): Flow<List<BarcodeItem>> = barcodeDao.getBarcodesForBatch(batchId)

    // Ledger
    val allTransactions: Flow<List<LedgerTransaction>> = ledgerDao.getAllTransactions()
    fun getTransactionsForWorker(workerId: Int): Flow<List<LedgerTransaction>> = ledgerDao.getTransactionsForWorker(workerId)

    /**
     * Issues material to a jobworker:
     * 1. Auto-calculates sequental batch number: JOB-[Year]-[BatchCount + 1]
     * 2. Auto-generates N barcodes: JOB-[Year]-[BatchCount + 1]-SET01 ... SET[N]
     * 3. Inserts Batch and all its Barcode Items inside database.
     */
    suspend fun issueMaterial(
        worker: Worker,
        totalWeight: Double,
        wastagePercentage: Double,
        totalSets: Int = 30,
        notes: String = ""
    ): OutwardBatch {
        val timestamp = System.currentTimeMillis()
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(timestamp))
        
        // Find out count of batches to generate sequential code
        val existingBatches = batchDao.getAllBatches().firstOrNull() ?: emptyList()
        val currentYearCount = existingBatches.filter { 
            it.batchNumber.startsWith("JOB-$year-")
        }.size
        
        val sequenceNoFormatted = String.format("%03d", currentYearCount + 1)
        val batchNumber = "JOB-$year-$sequenceNoFormatted"

        // Calculations
        val allowedWastage = (totalWeight * wastagePercentage) / 100.0
        val expectedNetWeight = totalWeight - allowedWastage
        
        val grossWeightPerSet = totalWeight / totalSets
        val netWeightPerSet = expectedNetWeight / totalSets

        val batch = OutwardBatch(
            batchNumber = batchNumber,
            workerId = worker.id,
            workerName = worker.name,
            totalWeight = totalWeight,
            wastagePercentage = wastagePercentage,
            allowedWastage = allowedWastage,
            expectedNetWeight = expectedNetWeight,
            totalSets = totalSets,
            grossWeightPerSet = grossWeightPerSet,
            netWeightPerSet = netWeightPerSet,
            status = "Pending",
            timestamp = timestamp,
            notes = notes
        )

        val batchId = batchDao.insertBatch(batch).toInt()
        val savedBatch = batch.copy(id = batchId)

        // Generate Barcodes
        val barcodes = ArrayList<BarcodeItem>()
        for (i in 1..totalSets) {
            val setNoFormatted = String.format("%02d", i)
            val barcodeCode = "$batchNumber-SET$setNoFormatted"
            barcodes.add(
                BarcodeItem(
                    batchId = batchId,
                    batchNumber = batchNumber,
                    workerId = worker.id,
                    workerName = worker.name,
                    barcode = barcodeCode,
                    setIndex = i,
                    grossWeight = grossWeightPerSet,
                    netWeight = netWeightPerSet,
                    status = "Sent",
                    sentTimestamp = timestamp
                )
            )
        }
        
        barcodeDao.insertBarcodes(barcodes)
        return savedBatch
    }

    /**
     * Receives material by single barcode scanning:
     * 1. Validates barcode existence and status.
     * 2. Overlap/Duplicate scanning prevention with red error UI trigger.
     * 3. Calculates worker credits (Net weight x Labor rate, or per rate-set).
     * 4. Appends a ledger transaction as client earning credit.
     * 5. Automatically updates batch completion status if all sets received.
     */
    suspend fun receiveMaterialBarcode(barcodeString: String): ScanResult {
        val uppercaseBarcode = barcodeString.trim().uppercase()
        val barcodeItem = barcodeDao.getBarcodeByString(uppercaseBarcode) ?: return ScanResult.NotFound

        if (barcodeItem.status == "Received") {
            return ScanResult.AlreadyReceived(
                barcodeName = barcodeItem.barcode,
                receivedTime = barcodeItem.receivedTimestamp ?: System.currentTimeMillis()
            )
        }

        // It is currently "Sent", we marks it as "Received"
        val timestamp = System.currentTimeMillis()
        val updatedBarcode = barcodeItem.copy(
            status = "Received",
            receivedTimestamp = timestamp
        )
        barcodeDao.updateBarcode(updatedBarcode)

        // Calculate Worker accounting credit
        val worker = workerDao.getWorkerById(barcodeItem.workerId)
        val laborRate = worker?.laborRate ?: 15.0
        val rateType = worker?.rateType ?: "KG"

        val amountDue = if (rateType == "KG") {
            updatedBarcode.netWeight * laborRate
        } else {
            // "SET" rate type
            laborRate
        }

        // Add Ledger credit
        val ledgerTx = LedgerTransaction(
            workerId = updatedBarcode.workerId,
            workerName = updatedBarcode.workerName,
            batchId = updatedBarcode.batchId,
            barcodeId = updatedBarcode.id,
            barcodeString = updatedBarcode.barcode,
            type = "Credit",
            amount = amountDue,
            description = "Received Set #${updatedBarcode.setIndex_formatted()} [${updatedBarcode.barcode}] (Wt: ${String.format("%.2f", updatedBarcode.netWeight)} kg)",
            timestamp = timestamp
        )
        ledgerDao.insertTransaction(ledgerTx)

        // Verify if entire batch is completed
        val batchBarcodes = barcodeDao.getBarcodesForBatchSync(updatedBarcode.batchId)
        val allReceived = batchBarcodes.all { it.id == updatedBarcode.id || it.status == "Received" }
        if (allReceived) {
            val batch = batchDao.getBatchById(updatedBarcode.batchId)
            if (batch != null) {
                batchDao.updateBatch(batch.copy(status = "Completed"))
            }
        }

        return ScanResult.Success(updatedBarcode, amountDue)
    }

    /**
     * Records a payment made (Cash or UPI) to the jobworker.
     */
    suspend fun recordWorkerPayment(
        workerId: Int,
        workerName: String,
        amount: Double,
        method: String,
        description: String = ""
    ) {
        val paymentTx = LedgerTransaction(
            workerId = workerId,
            workerName = workerName,
            type = "Debit",
            amount = amount,
            paymentMethod = method,
            description = description.ifEmpty { "Payment made via $method" },
            timestamp = System.currentTimeMillis()
        )
        ledgerDao.insertTransaction(paymentTx)
    }

    private fun BarcodeItem.setIndex_formatted(): String {
        return String.format("%02d", this.setIndex)
    }
}
