package com.eomrobotics.scouting.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eomrobotics.scouting.data.ReefscapeConfig
import com.eomrobotics.scouting.viewmodel.ScoutingViewModel
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun ScoutingFinalScreen(navController: NavController, viewModel: ScoutingViewModel) {
    var selectedEndGame by remember { mutableStateOf("None") }
    var fouls by remember { mutableStateOf(0) }
    var drivingRating by remember { mutableStateOf(3) }
    var notes by remember { mutableStateOf("") }
    var showQR by remember { mutableStateOf(false) }

    if (showQR) {
        QRDisplayScreen(
            viewModel = viewModel,
            onBack = {
                showQR = false
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "END GAME & NOTES",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text("End Game", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReefscapeConfig.endGameOptions.forEach { option ->
                    FilterChip(
                        selected = selectedEndGame == option,
                        onClick = { selectedEndGame = option },
                        label = { Text(option) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Fouls", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (fouls > 0) fouls-- }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(fouls.toString(), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { fouls++ }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Driving Rating (4 = 👌)", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (1..4).forEach { rating ->
                    FilterChip(
                        selected = drivingRating == rating,
                        onClick = { drivingRating = rating },
                        label = { Text(rating.toString()) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.completeEntry(selectedEndGame, fouls, drivingRating, notes)
                    showQR = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & Generate QR Code")
            }
        }
    }
}

@Composable
fun QRDisplayScreen(viewModel: ScoutingViewModel, onBack: () -> Unit) {
    val entries by viewModel.getAllEntries().collectAsState(initial = emptyList())
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(entries) {
        if (entries.isNotEmpty()) {
            qrBitmap = viewModel.generateQRCodeBitmap(entries)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Scan to Import Data",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "${entries.size} match${if (entries.size != 1) "es" else ""} ready",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .size(350.dp)
                    .padding(16.dp)
                    .background(Color.White)
            )
        } ?: CircularProgressIndicator()

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Home")
        }
    }
}