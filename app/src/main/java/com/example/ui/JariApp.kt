package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JariApp(viewModel: JariViewModel, modifier: Modifier = Modifier) {
    var currentTab by rememberSaveable { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageOfJariRoll(),
                                contentDescription = "Jari Icon",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "JariFlow Pro",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                "TEXTILE JOBWORK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_navigation_bar"),
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Jobworkers") },
                    label = { Text("Workers") },
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    modifier = Modifier.testTag("tab_workers")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.LocalShipping, contentDescription = "Issue Material") },
                    label = { Text("Outward") },
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    modifier = Modifier.testTag("tab_issue")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan Inward") },
                    label = { Text("Scanner") },
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    modifier = Modifier.testTag("tab_scanner")
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Accounts Ledger") },
                    label = { Text("Ledger") },
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    modifier = Modifier.testTag("tab_ledger")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                0 -> WorkersScreen(viewModel)
                1 -> OutwardScreen(viewModel)
                2 -> ScannerScreen(viewModel)
                3 -> LedgerScreen(viewModel)
            }
        }
    }
}

@Composable
fun WorkersScreen(viewModel: JariViewModel) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    
    // Form Inputs
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var laborRate by rememberSaveable { mutableStateOf("") }
    var rateType by rememberSaveable { mutableStateOf("KG") } // "KG" or "SET"

    var expandedForm by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Form Button
        item {
            ElevatedCard(
                onClick = { expandedForm = !expandedForm },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (expandedForm) MaterialTheme.colorScheme.primaryContainer 
                                     else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (expandedForm) Icons.Filled.Close else Icons.Filled.PersonAdd,
                            contentDescription = "Add Worker Toggle"
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (expandedForm) "Close Registration Form" else "Register New Jobworker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Icon(
                        if (expandedForm) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Arrow"
                    )
                }
            }
        }

        // Registration form panel
        if (expandedForm) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_registration_form"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "New Profile Details",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Worker Full Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("worker_name_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Person, "Name icon") }
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("worker_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Phone, "Phone icon") }
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Unit Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("worker_location_input"),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.LocationOn, "Location icon") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = laborRate,
                                onValueChange = { laborRate = it },
                                label = { Text("Labor Rate (₹) *") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("worker_rate_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, "Rupee") }
                            )

                            // Segmented Basis selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Rate Unit Basis",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(2.dp)
                                ) {
                                    val isKg = rateType == "KG"
                                    Button(
                                        onClick = { rateType = "KG" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isKg) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (isKg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("per KG", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { rateType = "SET" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isKg) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (!isKg) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("per Set", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val dRate = laborRate.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && dRate > 0) {
                                    viewModel.addWorker(name, phone, location, dRate, rateType)
                                    // Reset fields
                                    name = ""
                                    phone = ""
                                    location = ""
                                    laborRate = ""
                                    expandedForm = false
                                }
                            },
                            enabled = name.isNotBlank() && laborRate.toDoubleOrNull() != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_worker_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Worker Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section Title: Workers Directory
        item {
            Text(
                "Workers Directory",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (workers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = "No workers",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "No Workers Registered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "To issue material outward, you must first register at least one Jari jobworker above.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(workers) { worker ->
                WorkerCardItem(worker = worker, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OutwardScreen(viewModel: JariViewModel) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    
    val selectedWorkerId by viewModel.selectedIssueWorkerId.collectAsStateWithLifecycle()
    val recentlyIssuedBatch by viewModel.recentlyIssuedBatch.collectAsStateWithLifecycle()
    val recentlyIssuedBarcodes by viewModel.recentlyIssuedBarcodes.collectAsStateWithLifecycle()

    // Form inputs
    var totalWeightInput by rememberSaveable { mutableStateOf("") }
    var wastageInput by rememberSaveable { mutableStateOf("2.0") }
    var numSetsInput by rememberSaveable { mutableStateOf("30") } // default 30 sets
    var notesInput by rememberSaveable { mutableStateOf("") }

    var expandedSelectDropdown by remember { mutableStateOf(false) }

    // Reactive calculations
    val totalWeight = totalWeightInput.toDoubleOrNull() ?: 0.0
    val wastagePct = wastageInput.toDoubleOrNull() ?: 0.0
    val numSets = numSetsInput.toIntOrNull() ?: 30

    val allowedWastage = (totalWeight * wastagePct) / 100.0
    val expectedNetWeight = totalWeight - allowedWastage
    val grossWeightPerSet = if (numSets > 0) totalWeight / numSets else 0.0
    val netWeightPerSet = if (numSets > 0) expectedNetWeight / numSets else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Issue Material (Outward)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (workers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, "Warning", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Please add a Worker first in the Workers tab before issuing materials.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "New Outward Allocation",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.3).sp
                            )
                        )

                        // Jobworker Selection Dropdown
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val activeWorker = workers.find { it.id == selectedWorkerId }
                            Text(
                                "Jobworker *",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedCard(
                                    onClick = { expandedSelectDropdown = !expandedSelectDropdown },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("select_worker_outward"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.AccountCircle,
                                                contentDescription = "worker icon",
                                                tint = if (activeWorker != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = activeWorker?.name ?: "Choose jobworker...",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (activeWorker != null) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (activeWorker != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            )
                                        }
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            contentDescription = "dropdown",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = expandedSelectDropdown,
                                    onDismissRequest = { expandedSelectDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    workers.forEach { w ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(w.name, fontWeight = FontWeight.Bold)
                                                    Text("₹${w.laborRate}/${w.rateType}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                                }
                                            },
                                            onClick = {
                                                viewModel.selectIssueWorker(w.id)
                                                expandedSelectDropdown = false
                                            },
                                            modifier = Modifier.testTag("select_worker_item_${w.id}")
                                        )
                                    }
                                }
                            }
                        }

                        // Weight Input
                        OutlinedTextField(
                            value = totalWeightInput,
                            onValueChange = { totalWeightInput = it },
                            label = { Text("Total Gross Weight Sent (KG) *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("outward_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Filled.Scale, "Scale", modifier = Modifier.size(20.dp)) }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Wastage % Input
                            OutlinedTextField(
                                value = wastageInput,
                                onValueChange = { wastageInput = it },
                                label = { Text("Wastage %") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("outward_wastage_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Filled.Percent, "Percent", modifier = Modifier.size(16.dp)) }
                            )

                            // Number of Sets Input
                            OutlinedTextField(
                                value = numSetsInput,
                                onValueChange = { numSetsInput = it },
                                label = { Text("Rolls Count") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("outward_sets_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Filled.Numbers, "Sets count", modifier = Modifier.size(16.dp)) }
                            )
                        }

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Transaction Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Filled.EditNote, "Notes", modifier = Modifier.size(20.dp)) }
                        )

                        // Clean Yield Calculator & Report Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "PRO-RATA ROLL CALCULATOR",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                RowReportLine("Gross Material Sent", "${String.format("%.2f", totalWeight)} kg")
                                RowReportLine("Allowed Wastage (${String.format("%.1f", wastagePct)}%)", "${String.format("%.2f", allowedWastage)} kg", tint = MaterialTheme.colorScheme.error)
                                RowReportLine("Expected Net Yield", "${String.format("%.2f", expectedNetWeight)} kg", isBold = true)
                                RowReportLine("Layout Division", "$numSets rolls to be printed", isItalic = true)
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                RowReportLine("Weight Split Target / Roll", "Gross: ${String.format("%.2f", grossWeightPerSet)} kg  |  Expected Net: ${String.format("%.2f", netWeightPerSet)} kg", isBold = true)
                            }
                        }

                        // Beautiful Preview of Individual Sets Configuration
                        if (totalWeight > 0.0 && numSets > 0) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "PREVIEW: INDIVIDUAL SETS WEIGHTS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        "Total Expect: ${String.format("%.1f", expectedNetWeight)} kg",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val previewLimit = minOf(numSets, 12)
                                    for (i in 1..previewLimit) {
                                        Card(
                                            modifier = Modifier.width(115.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    "ROLL SET #$i",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 8.sp,
                                                        letterSpacing = 0.3.sp
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "Gross: ${String.format("%.2f", grossWeightPerSet)} kg",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                                Text(
                                                    "Net: ${String.format("%.2f", netWeightPerSet)} kg",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    if (numSets > previewLimit) {
                                        Card(
                                            modifier = Modifier.width(100.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "+ ${numSets - previewLimit} more",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Black,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    ),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (selectedWorkerId != null && totalWeight > 0.0 && numSets > 0) {
                                    viewModel.issueMaterial(
                                        workerId = selectedWorkerId!!,
                                        totalWeight = totalWeight,
                                        wastagePercentage = wastagePct,
                                        totalSets = numSets,
                                        notes = notesInput
                                    )
                                    // Reset fields except defaults
                                    totalWeightInput = ""
                                    notesInput = ""
                                }
                            },
                            enabled = selectedWorkerId != null && totalWeight > 0.0 && numSets > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_barcodes_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        ) {
                            Icon(Icons.Filled.QrCode, "Generate", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Barcodes & Issue", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Display Simulation Receipt if a batch was recently generated
        if (recentlyIssuedBatch != null) {
            item {
                BluetoothPrintSimulatorView(
                    batch = recentlyIssuedBatch!!,
                    barcodes = recentlyIssuedBarcodes,
                    onDismiss = { viewModel.clearRecentlyIssued() }
                )
            }
        }

        // Section Title: Issue Ledger
        item {
            Text(
                "Recent Outward Batches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (batches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Text(
                        "No outward batches issued yet.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(batches) { batch ->
                OutwardBatchItem(batch = batch, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RowReportLine(title: String, value: String, isBold: Boolean = false, isItalic: Boolean = false, tint: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title, 
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (tint != Color.Unspecified) tint else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// Thermal label printer layout simulator
@Composable
fun BluetoothPrintSimulatorView(
    batch: OutwardBatch,
    barcodes: List<BarcodeItem>,
    onDismiss: () -> Unit
) {
    var hasPrintedSimulation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bluetooth_print_simulator"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Print, "Printer", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Label Print Simulator", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, "Dismiss")
                }
            }

            Text(
                "Automatic barcode stickers for Bluetooth thermal sticker label printer:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Simulated Paper Slip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Receipt Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("★ STICKER LABELS PRINT SQUAD ★", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("JARI METALLIC TRACKING", fontSize = 9.sp, color = Color.Gray)
                    Text("---------------------------------", fontSize = 10.sp, color = Color.Gray)
                }

                // Batch general receipt details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Batch: ${batch.batchNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Worker: ${batch.workerName}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Gross: ${batch.totalWeight} kg", fontSize = 9.sp, color = Color.DarkGray)
                    Text("Sets Issued: ${batch.totalSets}", fontSize = 9.sp, color = Color.DarkGray)
                }

                Text("- - SAMPLE STICKER PREVIEWS - -", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))

                // Scrollable set of stickers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    barcodes.forEach { bar ->
                        // Mini Sticker Box
                        Column(
                            modifier = Modifier
                                .width(150.dp)
                                .background(Color(0xFFFAFAFA))
                                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("JARI JOBWORK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            
                            // Visual barcode simulation lines
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .padding(vertical = 2.dp)
                                    .background(Color.LightGray.copy(alpha = 0.2f))
                                    .drawBehind {
                                        // Draw a simple mock barcode pattern
                                        val barWidths = listOf(2.dp, 4.dp, 1.dp, 3.dp, 1.dp, 5.dp, 2.dp, 1.dp, 3.dp, 4.dp, 1.dp)
                                        var xOffset = 4.dp.toPx()
                                        var iIdx = 0
                                        while (xOffset < size.width - 4.dp.toPx()) {
                                            val w = barWidths[iIdx % barWidths.size].toPx()
                                            val isDark = iIdx % 2 == 0
                                            if (isDark) {
                                                drawRect(
                                                    color = Color.Black,
                                                    topLeft = androidx.compose.ui.geometry.Offset(xOffset, 2.dp.toPx()),
                                                    size = androidx.compose.ui.geometry.Size(w, size.height - 4.dp.toPx())
                                                )
                                            }
                                            xOffset += w + 2.dp.toPx()
                                            iIdx++
                                        }
                                    }
                            ) {}

                            Text(bar.barcode, fontSize = 8.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Set: ${String.format("%02d", bar.setIndex)} / ${batch.totalSets}", fontSize = 7.sp, color = Color.DarkGray)
                            Text("Gross: ${String.format("%.2f", bar.grossWeight)} kg", fontSize = 7.sp, color = Color.Black)
                            Text("Exp Net: ${String.format("%.2f", bar.netWeight)} kg (Wastage ${batch.wastagePercentage}%)", fontSize = 6.sp, color = Color.Red)
                        }
                    }
                }

                Text("---------------------------------", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Bluetooth ESC/POS Device: Connected", fontSize = 8.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            AnimatedContent(
                targetState = hasPrintedSimulation, label = "ButtonTransition"
            ) { printed ->
                if (printed) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, "success", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("30 stickers sent to thermal printer!", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { hasPrintedSimulation = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_print_finish_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Print, "print")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print 30 Labels to Bluetooth Printer")
                    }
                }
            }
        }
    }
}

@Composable
fun ScannerScreen(viewModel: JariViewModel) {
    val pendingBarcodes by viewModel.pendingBarcodes.collectAsStateWithLifecycle()
    val lastScanResult by viewModel.lastScanResult.collectAsStateWithLifecycle()
    val searchQuery by viewModel.scannerSearchQuery.collectAsStateWithLifecycle()

    var manualBarcodeText by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Scanner beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "Beam")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    // Check if duplicate red overlay error is active
    val isDuplicateError = lastScanResult is ScanResult.AlreadyReceived

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Inward Scanner Terminal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // --- CAMERA SCANNER VIEWFINDER OVERLAY OR SIMULATOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Simulated camera viewfinder texture
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw camera guidelines, corner brackets
                val cornerSize = 25.dp.toPx()
                val thickness = 4.dp.toPx()
                val paintColor = Color(0xFFFFB300) // Gold corner brackets
                
                // Top-Left Corner
                drawRect(paintColor, size = androidx.compose.ui.geometry.Size(cornerSize, thickness))
                drawRect(paintColor, size = androidx.compose.ui.geometry.Size(thickness, cornerSize))
                
                // Top-Right Corner
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width - cornerSize, 0f),
                    size = androidx.compose.ui.geometry.Size(cornerSize, thickness)
                )
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width - thickness, 0f),
                    size = androidx.compose.ui.geometry.Size(thickness, cornerSize)
                )

                // Bottom-Left Corner
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - thickness),
                    size = androidx.compose.ui.geometry.Size(cornerSize, thickness)
                )
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - cornerSize),
                    size = androidx.compose.ui.geometry.Size(thickness, cornerSize)
                )

                // Bottom-Right Corner
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width - cornerSize, size.height - thickness),
                    size = androidx.compose.ui.geometry.Size(cornerSize, thickness)
                )
                drawRect(
                    paintColor,
                    topLeft = androidx.compose.ui.geometry.Offset(size.width - thickness, size.height - cornerSize),
                    size = androidx.compose.ui.geometry.Size(thickness, cornerSize)
                )
            }

            // Central laser scanning line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (240.dp * laserOffset))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFF3D00), // Scanning Laser Red
                                Color(0xFFFF3D00),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner visual simulator grid or text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Outlined.QrCodeScanner,
                    contentDescription = "scanning",
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "CAMERA VIEWFINDER ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Align Jari Sticker Barcode within scanner frame",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            // --- RED DUPLICATE WARNING DIALOG PANEL OVERVIEW ---
            if (isDuplicateError) {
                val detail = lastScanResult as ScanResult.AlreadyReceived
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFD50000)) // Pure Red error overlay
                        .testTag("duplicate_error_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.NewReleases,
                            contentDescription = "Duplicate scanned",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            "DUPLICATE DETECTED!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Error: Barcode already received on\n${viewModel.formatDateTime(detail.receivedTime)}!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Barcode Code: ${detail.barcodeName}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        
                        Button(
                            onClick = { viewModel.clearScanResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("dismiss_scan_error")
                        ) {
                            Text("Acknowledge & Unlock Scanner", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Search bar or text-entry helper for barcode scan
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Manual Input / Barcode Gun Reader",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualBarcodeText,
                        onValueChange = { manualBarcodeText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_barcode_input"),
                        placeholder = { Text("e.g. JOB-2026-001-SET04") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        trailingIcon = {
                            if (manualBarcodeText.isNotEmpty()) {
                                IconButton(onClick = { manualBarcodeText = "" }) {
                                    Icon(Icons.Filled.Close, "clear")
                                }
                            }
                        }
                    )

                    Button(
                        onClick = {
                            if (manualBarcodeText.isNotBlank()) {
                                viewModel.scanBarcode(manualBarcodeText)
                                manualBarcodeText = ""
                            }
                        },
                        modifier = Modifier.testTag("scan_submit_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Search/Scan")
                    }
                }
            }
        }

        // --- LIVE SCAN SUCCESS / NOT FOUND METRICS LABELS ---
        AnimatedVisibility(
            visible = lastScanResult != null && lastScanResult !is ScanResult.AlreadyReceived,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            if (lastScanResult is ScanResult.Success) {
                val detail = lastScanResult as ScanResult.Success
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CheckCircle, "success", tint = Color(0xFF2E7D32))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SUCCESSFULLY RECEIVED INWARD", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Barcode: ${detail.barcode.barcode}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Jobworker Assigned: ${detail.barcode.workerName}", style = MaterialTheme.typography.bodySmall)
                        Text("Gross Recd: ${String.format("%.2f", detail.barcode.grossWeight)} kg  | Expected Net: ${String.format("%.2f", detail.barcode.netWeight)} kg", style = MaterialTheme.typography.bodySmall)
                        Text("Worker Compensation Credited: ₹${String.format("%.2f", detail.creditEarned)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearScanResult() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("OK")
                        }
                    }
                }
            } else if (lastScanResult is ScanResult.NotFound) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFE65100))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, "not found", tint = Color(0xFFE65100))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BARCODE CODE NOT FOUND", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                        Text("Please make sure you have typed/scanned a valid generated serial code.", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearScanResult() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }

        // --- SCAN SIMULATION DEV HELPER FOR BROWSER TESTING ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Science, "Demo", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🧪 Demo Simulator Assistant (For Emulator/Browser Testing)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                }

                Text(
                    "Since the browser emulator lacks physical laser barcode scanners or cameras, click any of the pending outward rolls below to trigger immediate inward scanning registration simulation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (pendingBarcodes.isEmpty()) {
                    Text(
                        "No pending rolls in tracking. Go to 'Outward' tab to register an issue of jari rolls.",
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    Text("Active Outward Roll Labels (${pendingBarcodes.size} sets remaining):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(pendingBarcodes) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                        .clickable { viewModel.scanBarcode(item.barcode) }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(item.barcode, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Worker: ${item.workerName} | Net: ${String.format("%.2f", item.netWeight)} kg", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tap to Scan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.KeyboardArrowRight, "Right Arrow", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerScreen(viewModel: JariViewModel) {
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val transactions by viewModel.selectedWorkerTransactions.collectAsStateWithLifecycle()
    val selectedWorkerId by viewModel.selectedWorkerId.collectAsStateWithLifecycle()
    val selectedWorker by viewModel.selectedWorker.collectAsStateWithLifecycle()

    val summary = if (selectedWorker != null) {
        viewModel.getWorkerSummary(selectedWorker!!.id).collectAsStateWithLifecycle(initialValue = JariViewModel.WorkerAccountSummary()).value
    } else {
        JariViewModel.WorkerAccountSummary()
    }

    var paymentAmountInput by rememberSaveable { mutableStateOf("") }
    var paymentMethodInput by rememberSaveable { mutableStateOf("UPI") } // "UPI" or "Cash"
    var descriptionInput by rememberSaveable { mutableStateOf("") }

    var expandedSelectDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Worker Ledger & Accounting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Selector for jobworker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose Jobworker to view Ledger", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expandedSelectDropdown = !expandedSelectDropdown },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_worker_ledger")
                        ) {
                            Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    selectedWorker?.name ?: "--- Select Jobworker ---",
                                    fontWeight = if (selectedWorker != null) FontWeight.Bold else FontWeight.Normal
                                )
                                Icon(Icons.Filled.ArrowDropDown, "dropdown")
                            }
                        }

                        DropdownMenu(
                            expanded = expandedSelectDropdown,
                            onDismissRequest = { expandedSelectDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            workers.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text(w.name) },
                                    onClick = {
                                        viewModel.selectWorker(w.id)
                                        expandedSelectDropdown = false
                                    },
                                    modifier = Modifier.testTag("select_ledger_worker_item_${w.id}")
                                )
                            }
                        }
                    }
                }
            }
        }

        if (selectedWorker == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ManageAccounts, "Ledger Account", modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Text(
                            "Select a worker from the dropdown above to view pending statement, log disbursements/UPI cash payments, and query their full historic credit logs.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            val worker = selectedWorker!!

            // Financial Balance Cards Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ledger_financial_board"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "${worker.name}'s Financial Dashboard",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GridStatBox("TOTAL EARNED", "₹${String.format("%.2f", summary.totalEarned)}", Color(0xFF2E7D32), Modifier.weight(1f))
                            GridStatBox("TOTAL PAID", "₹${String.format("%.2f", summary.totalPaid)}", Color(0xFFD50000), Modifier.weight(1f))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("CLEAR OUTSTANDING BALANCE DUE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                    Text(
                                        "₹${String.format("%.2f", summary.balanceDue)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Icon(Icons.Filled.AccountBalance, "Due balance", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
                            }
                        }

                        // Work stats metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Yield Processed: ${String.format("%.2f", summary.totalWeightProcessed)} kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text("Completed Returns: ${summary.setsCompleted} rolls", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Pay Worker Form Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Disburse Cash / Record Payout",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = paymentAmountInput,
                                onValueChange = { paymentAmountInput = it },
                                label = { Text("Payout Amount (₹) *") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("payout_amount_input"),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.CurrencyRupee, "Disb") }
                            )

                            // Unified Selector
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Payment Method", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(2.dp)
                                ) {
                                    val isUpi = paymentMethodInput == "UPI"
                                    Button(
                                        onClick = { paymentMethodInput = "UPI" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isUpi) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (isUpi) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("UPI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { paymentMethodInput = "Cash" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isUpi) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            contentColor = if (!isUpi) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Cash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = descriptionInput,
                            onValueChange = { descriptionInput = it },
                            label = { Text("Reference / Notes (e.g. UPI ref receipt code)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Description, "Description icon") }
                        )

                        Button(
                            onClick = {
                                val amtStr = paymentAmountInput.toDoubleOrNull()
                                if (amtStr != null && amtStr > 0) {
                                    viewModel.recordPayment(
                                        workerId = worker.id,
                                        amount = amtStr,
                                        method = paymentMethodInput,
                                        description = descriptionInput
                                    )
                                    paymentAmountInput = ""
                                    descriptionInput = ""
                                }
                            },
                            enabled = paymentAmountInput.toDoubleOrNull() != null && paymentAmountInput.toDoubleOrNull()!! > 0.0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("record_payment_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                        ) {
                            Icon(Icons.Filled.Payments, "Pay")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Post Transaction", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Ledger Transaction logs
            item {
                Text(
                    "Statement Ledger Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Text(
                            "No ledger entries recorded yet. Receive rolls in Inward tab to record custom yield credits.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    val isCredit = tx.type == "Credit"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_item_${tx.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (isCredit) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCredit) Icons.Filled.AddCard else Icons.Filled.Send,
                                        contentDescription = if (isCredit) "Earned credit" else "Paid debit",
                                        tint = if (isCredit) Color(0xFF2E7D32) else Color(0xFFD50000),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        tx.description,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        viewModel.formatDateTime(tx.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${if (isCredit) "+ ₹" else "- ₹"}${String.format("%.2f", tx.amount)}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isCredit) Color(0xFF2E7D32) else Color(0xFFD50000),
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridStatBox(title: String, score: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.Bold, 
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                score, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Draw a beautiful circular roll representation vector asset
@Composable
fun imageOfJariRoll() = Icons.Outlined.Layers

@Composable
fun WorkerCardItem(worker: Worker, viewModel: JariViewModel) {
    val summary by viewModel.getWorkerSummary(worker.id).collectAsStateWithLifecycle(initialValue = JariViewModel.WorkerAccountSummary())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        worker.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.0.dp, vertical = 2.0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Sell,
                            contentDescription = "Rate",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Rate: ₹${worker.laborRate} / ${worker.rateType}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Delete Action
                var confirmDelete by remember { mutableStateOf(false) }
                if (confirmDelete) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.deleteWorker(worker) },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Filled.Check, "Confirm Delete")
                        }
                        IconButton(
                            onClick = { confirmDelete = false },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Icon(Icons.Filled.Close, "Cancel Delete")
                        }
                    }
                } else {
                    IconButton(
                        onClick = { confirmDelete = true },
                        modifier = Modifier.testTag("delete_worker_${worker.id}")
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete worker profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (worker.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Filled.Phone,
                            "Phone",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            worker.phone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (worker.location.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Filled.LocationOn,
                            "Location",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            worker.location,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Compact metrics stats in worker card - Updated styled background container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "SETS DONE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${summary.setsCompleted} done",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PENDING SETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${summary.setsPending} sets",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFFE65100)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "NET BALANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "₹${String.format("%.2f", summary.balanceDue)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun OutwardBatchItem(batch: OutwardBatch, viewModel: JariViewModel) {
    val barcodes by viewModel.getBarcodesForBatch(batch.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val completedCount = barcodes.count { it.status == "Received" }
    val totalCount = batch.totalSets
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f
    val isCompleted = batch.status == "Completed" || completedCount == totalCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outward_batch_${batch.batchNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        batch.batchNumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        viewModel.formatDateTime(batch.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Modern Status Chip with customized padding & colors
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCompleted) "Completed" else "In Progress",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "WORKER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        batch.workerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "TOTAL WEIGHT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        "${batch.totalWeight} kg",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "EST. WASTAGE (-${batch.wastagePercentage}%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        "${String.format("%.2f", batch.allowedWastage)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "EXPECTED YIELD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        "${String.format("%.2f", batch.expectedNetWeight)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Progress bar for scanned sets
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Returns Clearance Tracking",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        "$completedCount / $totalCount Rolls",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

