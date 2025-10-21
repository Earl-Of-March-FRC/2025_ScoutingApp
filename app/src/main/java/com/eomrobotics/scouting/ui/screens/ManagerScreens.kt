package com.eomrobotics.scouting.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eomrobotics.scouting.data.ScoutingEntry
import com.eomrobotics.scouting.viewmodel.ScoutingViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun ManagerScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Manager Mode",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { navController.navigate("qr_scanner") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan QR Code")
        }

        Button(
            onClick = { navController.navigate("spreadsheet") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Data")
        }

        OutlinedButton(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun QRScannerScreen(navController: NavController, viewModel: ScoutingViewModel) {
    var scanResult by remember { mutableStateOf<com.eomrobotics.scouting.data.ImportResult?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let { contents ->
            kotlinx.coroutines.MainScope().launch {
                val importResult = viewModel.importFromQR(contents)
                scanResult = importResult
                showResult = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showResult && scanResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Import Results",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (scanResult?.error != null) {
                            Text(
                                text = "Error: ${scanResult?.error}",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("✓ Imported: ${scanResult?.imported}")
                            Text("⚠ Duplicates skipped: ${scanResult?.duplicates}")
                        }

                        Button(
                            onClick = {
                                showResult = false
                                scanResult = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Scan Another")
                        }
                    }
                }
            } else {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp).padding(bottom = 24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Scan Scouter QR Code",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Position the QR code within the frame",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = {
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan QR Code")
                            setCameraId(0)
                            setBeepEnabled(true)
                            setOrientationLocked(false)
                        }
                        scanLauncher.launch(options)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Scanning")
                }
            }

            OutlinedButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetScreen(navController: NavController, viewModel: ScoutingViewModel) {
    val entries by viewModel.getAllEntries().collectAsState(initial = emptyList())
    var sortBy by remember { mutableStateOf("matchNumber") }
    var sortAscending by remember { mutableStateOf(true) }
    var showStats by remember { mutableStateOf(false) }

    val sortedEntries = remember(entries, sortBy, sortAscending) {
        val sorted = when (sortBy) {
            "matchNumber" -> entries.sortedBy { it.matchNumber }
            "teamNumber" -> entries.sortedBy { it.teamNumber }
            "drivingRating" -> entries.sortedBy { it.drivingRating }
            "timestamp" -> entries.sortedBy { it.timestamp }
            else -> entries
        }
        if (sortAscending) sorted else sorted.reversed()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scouting Data",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(onClick = { showStats = !showStats }) {
                Icon(
                    if (showStats) Icons.Default.TableChart else Icons.Default.Analytics,
                    contentDescription = "Toggle Stats"
                )
            }
        }

        // Stats Panel
        if (showStats) {
            StatisticsPanel(entries)
        }

        // Sort Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = sortBy == "matchNumber",
                onClick = {
                    if (sortBy == "matchNumber") sortAscending = !sortAscending
                    else sortBy = "matchNumber"
                },
                label = { Text("Match #") }
            )
            FilterChip(
                selected = sortBy == "teamNumber",
                onClick = {
                    if (sortBy == "teamNumber") sortAscending = !sortAscending
                    else sortBy = "teamNumber"
                },
                label = { Text("Team #") }
            )
            FilterChip(
                selected = sortBy == "drivingRating",
                onClick = {
                    if (sortBy == "drivingRating") sortAscending = !sortAscending
                    else sortBy = "drivingRating"
                },
                label = { Text("Rating") }
            )
        }

        // Data Table
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Table Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Match", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                    Text("Team", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                    Text("Station", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                    Text("Auto", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                    Text("Teleop", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.Bold)
                    Text("Rating", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider()
            }

            // Data Rows
            items(sortedEntries) { entry ->
                EntryRow(entry)
            }

            if (sortedEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No data yet. Start scouting or scan QR codes.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        OutlinedButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun EntryRow(entry: ScoutingEntry) {
    val autoScore = entry.autoData.coralMade.values.sum() + entry.autoData.algaeMade.values.sum()
    val teleopScore = entry.teleopData.coralMade.values.sum() +
            entry.teleopData.algaeRobotMade.values.sum() +
            entry.teleopData.algaeHumanMade.values.sum()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {})
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(entry.matchNumber.toString(), modifier = Modifier.weight(0.8f))
            Text(entry.teamNumber.toString(), modifier = Modifier.weight(0.8f))
            Text(entry.driveStation, modifier = Modifier.weight(0.8f))
            Text(autoScore.toString(), modifier = Modifier.weight(0.8f))
            Text(teleopScore.toString(), modifier = Modifier.weight(0.8f))
            Text(entry.drivingRating.toString(), modifier = Modifier.weight(0.6f))
        }
        HorizontalDivider()
    }
}

@Composable
fun StatisticsPanel(entries: List<ScoutingEntry>) {
    if (entries.isEmpty()) return

    val avgDrivingRating = entries.map { it.drivingRating }.average()
    val avgAutoScore = entries.map {
        it.autoData.coralMade.values.sum() + it.autoData.algaeMade.values.sum()
    }.average()
    val avgTeleopScore = entries.map {
        it.teleopData.coralMade.values.sum() +
                it.teleopData.algaeRobotMade.values.sum() +
                it.teleopData.algaeHumanMade.values.sum()
    }.average()

    val drivingStdDev = calculateStdDev(entries.map { it.drivingRating.toDouble() })

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics (${entries.size} matches)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Avg Auto", String.format("%.1f", avgAutoScore))
                StatItem("Avg Teleop", String.format("%.1f", avgTeleopScore))
                StatItem("Avg Rating", String.format("%.2f", avgDrivingRating))
                StatItem("Rating σ", String.format("%.2f", drivingStdDev))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun calculateStdDev(values: List<Double>): Double {
    if (values.isEmpty()) return 0.0
    val mean = values.average()
    val variance = values.map { (it - mean).pow(2) }.average()
    return sqrt(variance)
}