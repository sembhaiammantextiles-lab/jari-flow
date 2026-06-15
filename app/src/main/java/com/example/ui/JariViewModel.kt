package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JariViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: JariRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = JariRepository(database)
    }

    // Live Data Sources
    val workers: StateFlow<List<Worker>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<OutwardBatch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingBarcodes: StateFlow<List<BarcodeItem>> = repository.pendingBarcodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<LedgerTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI UI-States ---

    // Selected Worker for Ledger Screen
    private val _selectedWorkerId = MutableStateFlow<Int?>(null)
    val selectedWorkerId: StateFlow<Int?> = _selectedWorkerId.asStateFlow()

    val selectedWorker: StateFlow<Worker?> = _selectedWorkerId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else workers.map { list -> list.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedWorkerTransactions: StateFlow<List<LedgerTransaction>> = _selectedWorkerId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getTransactionsForWorker(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Scanner Screen States
    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)
    val lastScanResult: StateFlow<ScanResult?> = _lastScanResult.asStateFlow()

    private val _scannerSearchQuery = MutableStateFlow("")
    val scannerSearchQuery: StateFlow<String> = _scannerSearchQuery.asStateFlow()

    // Outward screen active selection
    private val _selectedIssueWorkerId = MutableStateFlow<Int?>(null)
    val selectedIssueWorkerId: StateFlow<Int?> = _selectedIssueWorkerId.asStateFlow()

    // Helper: Calculate worker accounts summary (Balance, Total Earned, Total Paid, Processed bar count)
    data class WorkerAccountSummary(
        val totalEarned: Double = 0.0,
        val totalPaid: Double = 0.0,
        val balanceDue: Double = 0.0,
        val setsPending: Int = 0,
        val setsCompleted: Int = 0,
        val totalWeightProcessed: Double = 0.0
    )

    fun getWorkerSummary(workerId: Int): Flow<WorkerAccountSummary> {
        return combine(
            repository.getTransactionsForWorker(workerId),
            repository.allBarcodes
        ) { transactions, barcodes ->
            var earned = 0.0
            var paid = 0.0
            var weight = 0.0
            
            transactions.forEach { tx ->
                if (tx.type == "Credit") {
                    earned += tx.amount
                } else if (tx.type == "Debit") {
                    paid += tx.amount
                }
            }

            val workerBarcodes = barcodes.filter { it.workerId == workerId }
            val completed = workerBarcodes.count { it.status == "Received" }
            val pending = workerBarcodes.count { it.status == "Sent" }
            
            workerBarcodes.filter { it.status == "Received" }.forEach { barcode ->
                weight += barcode.netWeight
            }

            WorkerAccountSummary(
                totalEarned = earned,
                totalPaid = paid,
                balanceDue = earned - paid,
                setsPending = pending,
                setsCompleted = completed,
                totalWeightProcessed = weight
            )
        }
    }

    // --- Worker Operations ---

    fun selectWorker(id: Int?) {
        _selectedWorkerId.value = id
    }

    fun selectIssueWorker(id: Int?) {
        _selectedIssueWorkerId.value = id
    }

    fun addWorker(name: String, phone: String, location: String, laborRate: Double, rateType: String) {
        viewModelScope.launch {
            repository.insertWorker(
                Worker(
                    name = name.trim(),
                    phone = phone.trim(),
                    location = location.trim(),
                    laborRate = laborRate,
                    rateType = rateType
                )
            )
        }
    }

    fun deleteWorker(worker: Worker) {
        viewModelScope.launch {
            repository.deleteWorker(worker)
            if (_selectedWorkerId.value == worker.id) {
                _selectedWorkerId.value = null
            }
            if (_selectedIssueWorkerId.value == worker.id) {
                _selectedIssueWorkerId.value = null
            }
        }
    }

    // --- Batch / Barcode Material Outward Operations ---

    // Cache the most recently issued batch for displaying labels/barcodes instantly
    private val _recentlyIssuedBatch = MutableStateFlow<OutwardBatch?>(null)
    val recentlyIssuedBatch: StateFlow<OutwardBatch?> = _recentlyIssuedBatch.asStateFlow()

    private val _recentlyIssuedBarcodes = MutableStateFlow<List<BarcodeItem>>(emptyList())
    val recentlyIssuedBarcodes: StateFlow<List<BarcodeItem>> = _recentlyIssuedBarcodes.asStateFlow()

    fun clearRecentlyIssued() {
        _recentlyIssuedBatch.value = null
        _recentlyIssuedBarcodes.value = emptyList()
    }

    fun issueMaterial(workerId: Int, totalWeight: Double, wastagePercentage: Double, totalSets: Int = 30, notes: String = "") {
        viewModelScope.launch {
            val worker = workers.value.find { it.id == workerId } ?: return@launch
            val batch = repository.issueMaterial(
                worker = worker,
                totalWeight = totalWeight,
                wastagePercentage = wastagePercentage,
                totalSets = totalSets,
                notes = notes
            )
            _recentlyIssuedBatch.value = batch
            
            // Query generated barcodes to show "print preview sticker" immediately
            repository.getBarcodesForBatch(batch.id).take(1).collect { barcodesList ->
                _recentlyIssuedBarcodes.value = barcodesList
            }
        }
    }

    fun getBarcodesForBatch(batchId: Int): Flow<List<BarcodeItem>> {
        return repository.getBarcodesForBatch(batchId)
    }

    // --- Scanner Operations ---

    fun updateScannerQuery(query: String) {
        _scannerSearchQuery.value = query
    }

    fun scanBarcode(barcodeString: String) {
        viewModelScope.launch {
            val result = repository.receiveMaterialBarcode(barcodeString.uppercase().trim())
            _lastScanResult.value = result
            if (result is ScanResult.Success) {
                // Clear search input on successful scan
                _scannerSearchQuery.value = ""
            }
        }
    }

    fun clearScanResult() {
        _lastScanResult.value = null
    }

    // --- Ledger Payments Operations ---

    fun recordPayment(workerId: Int, amount: Double, method: String, description: String) {
        viewModelScope.launch {
            val worker = workers.value.find { it.id == workerId } ?: return@launch
            repository.recordWorkerPayment(
                workerId = worker.id,
                workerName = worker.name,
                amount = amount,
                method = method,
                description = description
            )
        }
    }

    // Utility: Format timestamp to readable date/time
    fun formatDateTime(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
