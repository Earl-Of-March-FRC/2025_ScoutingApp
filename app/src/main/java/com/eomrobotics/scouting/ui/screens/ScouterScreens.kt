package com.eomrobotics.scouting.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eomrobotics.scouting.data.*
import com.eomrobotics.scouting.ui.components.ScoringRowMadeMissed
import com.eomrobotics.scouting.viewmodel.ScoutingViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.padding
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScouterSetupScreen(navController: NavController, viewModel: ScoutingViewModel) {
    var teamNumber by remember { mutableStateOf("") }
    var matchNumber by remember { mutableStateOf("") }
    var selectedStation by remember { mutableStateOf("") }
    var scouterName by remember { mutableStateOf("") }
    var showStationMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Match Setup",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = teamNumber,
            onValueChange = { teamNumber = it },
            label = { Text("Team Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = matchNumber,
            onValueChange = { matchNumber = it },
            label = { Text("Match Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = showStationMenu,
            onExpandedChange = { showStationMenu = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedStation,
                onValueChange = {},
                readOnly = true,
                label = { Text("Drive Station") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStationMenu) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showStationMenu,
                onDismissRequest = { showStationMenu = false }
            ) {
                ReefscapeConfig.driveStations.forEach { station ->
                    DropdownMenuItem(
                        text = { Text(station) },
                        onClick = {
                            selectedStation = station
                            showStationMenu = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = scouterName,
            onValueChange = { scouterName = it },
            label = { Text("Scout Full Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                viewModel.initializeEntry(
                    teamNumber.toInt(),
                    matchNumber.toInt(),
                    selectedStation,
                    scouterName
                )
                navController.navigate("scouting_auto")
            },
            enabled = teamNumber.isNotBlank() && matchNumber.isNotBlank() &&
                    selectedStation.isNotBlank() && scouterName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Auto Period")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingAutoScreen(navController: NavController, viewModel: ScoutingViewModel) {
    var timeLeft by remember { mutableStateOf(ReefscapeConfig.AUTO_DURATION) }
    var isRunning by remember { mutableStateOf(false) }

    val coralMade = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.autoCoralActions.forEach { this[it] = 0 }
    }}
    val coralMissed = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.autoCoralActions.forEach { this[it] = 0 }
    }}
    val algaeMade = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.autoAlgaeActions.forEach { this[it] = 0 }
    }}
    val algaeMissed = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.autoAlgaeActions.forEach { this[it] = 0 }
    }}
    var leave by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0) {
                viewModel.setAutoData(AutoData(
                    coralMade = coralMade.toMap(),
                    coralMissed = coralMissed.toMap(),
                    algaeMade = algaeMade.toMap(),
                    algaeMissed = algaeMissed.toMap(),
                    leave = leave
                ))
                navController.navigate("scouting_teleop")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "${timeLeft}s",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "AUTONOMOUS",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        if (!isRunning) {
            Button(
                onClick = { isRunning = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("START AUTO")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = "🪸 CORAL",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(ReefscapeConfig.autoCoralActions) { action ->
                ScoringRowMadeMissed(
                    label = action,
                    made = coralMade[action] ?: 0,
                    missed = coralMissed[action] ?: 0,
                    onMadeIncrement = { coralMade[action] = (coralMade[action] ?: 0) + 1 },
                    onMadeDecrement = { if ((coralMade[action] ?: 0) > 0) coralMade[action] = (coralMade[action] ?: 0) - 1 },
                    onMissedIncrement = { coralMissed[action] = (coralMissed[action] ?: 0) + 1 },
                    onMissedDecrement = { if ((coralMissed[action] ?: 0) > 0) coralMissed[action] = (coralMissed[action] ?: 0) - 1 }
                )
            }

            item {
                Text(
                    text = "🟢 ALGAE",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 8.dp)
                )
            }

            items(ReefscapeConfig.autoAlgaeActions) { action ->
                ScoringRowMadeMissed(
                    label = action,
                    made = algaeMade[action] ?: 0,
                    missed = algaeMissed[action] ?: 0,
                    onMadeIncrement = { algaeMade[action] = (algaeMade[action] ?: 0) + 1 },
                    onMadeDecrement = { if ((algaeMade[action] ?: 0) > 0) algaeMade[action] = (algaeMade[action] ?: 0) - 1 },
                    onMissedIncrement = { algaeMissed[action] = (algaeMissed[action] ?: 0) + 1 },
                    onMissedDecrement = { if ((algaeMissed[action] ?: 0) > 0) algaeMissed[action] = (algaeMissed[action] ?: 0) - 1 }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Leave?", style = MaterialTheme.typography.titleMedium)
                    Row {
                        FilterChip(
                            selected = leave,
                            onClick = { leave = true },
                            label = { Text("Y") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !leave,
                            onClick = { leave = false },
                            label = { Text("N") }
                        )
                    }
                }
            }
        }

        if (timeLeft == 0) {
            Button(
                onClick = {
                    viewModel.setAutoData(AutoData(
                        coralMade = coralMade.toMap(),
                        coralMissed = coralMissed.toMap(),
                        algaeMade = algaeMade.toMap(),
                        algaeMissed = algaeMissed.toMap(),
                        leave = leave
                    ))
                    navController.navigate("scouting_teleop")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to Teleop")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoutingTeleopScreen(navController: NavController, viewModel: ScoutingViewModel) {
    var timeLeft by remember { mutableStateOf(ReefscapeConfig.TELEOP_DURATION) }
    var isRunning by remember { mutableStateOf(false) }

    val coralMade = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopCoralActions.forEach { this[it] = 0 }
    }}
    val coralMissed = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopCoralActions.forEach { this[it] = 0 }
    }}
    val algaeRobotMade = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopAlgaeActionsRobot.forEach { this[it] = 0 }
    }}
    val algaeRobotMissed = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopAlgaeActionsRobot.forEach { this[it] = 0 }
    }}
    val algaeHumanMade = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopAlgaeActionsHuman.forEach { this[it] = 0 }
    }}
    val algaeHumanMissed = remember { mutableStateMapOf<String, Int>().apply {
        ReefscapeConfig.teleopAlgaeActionsHuman.forEach { this[it] = 0 }
    }}
    var disconnects by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft == 0) {
                viewModel.setTeleopData(TeleopData(
                    coralMade = coralMade.toMap(),
                    coralMissed = coralMissed.toMap(),
                    algaeRobotMade = algaeRobotMade.toMap(),
                    algaeRobotMissed = algaeRobotMissed.toMap(),
                    algaeHumanMade = algaeHumanMade.toMap(),
                    algaeHumanMissed = algaeHumanMissed.toMap(),
                    disconnects = disconnects
                ))
                navController.navigate("scouting_final")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "${timeLeft}s",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center,
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "TELEOPERATED",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        if (!isRunning) {
            Button(
                onClick = { isRunning = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text("START TELEOP")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = "🪸 CORAL",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(ReefscapeConfig.teleopCoralActions) { action ->
                ScoringRowMadeMissed(
                    label = action,
                    made = coralMade[action] ?: 0,
                    missed = coralMissed[action] ?: 0,
                    onMadeIncrement = { coralMade[action] = (coralMade[action] ?: 0) + 1 },
                    onMadeDecrement = { if ((coralMade[action] ?: 0) > 0) coralMade[action] = (coralMade[action] ?: 0) - 1 },
                    onMissedIncrement = { coralMissed[action] = (coralMissed[action] ?: 0) + 1 },
                    onMissedDecrement = { if ((coralMissed[action] ?: 0) > 0) coralMissed[action] = (coralMissed[action] ?: 0) - 1 }
                )
            }

            item {
                Text(
                    text = "🟢 ALGAE (Robot)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }

            items(ReefscapeConfig.teleopAlgaeActionsRobot) { action ->
                ScoringRowMadeMissed(
                    label = action,
                    made = algaeRobotMade[action] ?: 0,
                    missed = algaeRobotMissed[action] ?: 0,
                    onMadeIncrement = { algaeRobotMade[action] = (algaeRobotMade[action] ?: 0) + 1 },
                    onMadeDecrement = { if ((algaeRobotMade[action] ?: 0) > 0) algaeRobotMade[action] = (algaeRobotMade[action] ?: 0) - 1 },
                    onMissedIncrement = { algaeRobotMissed[action] = (algaeRobotMissed[action] ?: 0) + 1 },
                    onMissedDecrement = { if ((algaeRobotMissed[action] ?: 0) > 0) algaeRobotMissed[action] = (algaeRobotMissed[action] ?: 0) - 1 }
                )
            }

            item {
                Text(
                    text = "🟢 ALGAE (Human Player)",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }

            items(ReefscapeConfig.teleopAlgaeActionsHuman) { action ->
                ScoringRowMadeMissed(
                    label = action,
                    made = algaeHumanMade[action] ?: 0,
                    missed = algaeHumanMissed[action] ?: 0,
                    onMadeIncrement = { algaeHumanMade[action] = (algaeHumanMade[action] ?: 0) + 1 },
                    onMadeDecrement = { if ((algaeHumanMade[action] ?: 0) > 0) algaeHumanMade[action] = (algaeHumanMade[action] ?: 0) - 1 },
                    onMissedIncrement = { algaeHumanMissed[action] = (algaeHumanMissed[action] ?: 0) + 1 },
                    onMissedDecrement = { if ((algaeHumanMissed[action] ?: 0) > 0) algaeHumanMissed[action] = (algaeHumanMissed[action] ?: 0) - 1 }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Disconnects?", style = MaterialTheme.typography.titleMedium)
                    Row {
                        FilterChip(
                            selected = disconnects,
                            onClick = { disconnects = true },
                            label = { Text("Y") }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !disconnects,
                            onClick = { disconnects = false },
                            label = { Text("N") }
                        )
                    }
                }
            }
        }

        if (timeLeft == 0) {
            Button(
                onClick = {
                    viewModel.setTeleopData(TeleopData(
                        coralMade = coralMade.toMap(),
                        coralMissed = coralMissed.toMap(),
                        algaeRobotMade = algaeRobotMade.toMap(),
                        algaeRobotMissed = algaeRobotMissed.toMap(),
                        algaeHumanMade = algaeHumanMade.toMap(),
                        algaeHumanMissed = algaeHumanMissed.toMap(),
                        disconnects = disconnects
                    ))
                    navController.navigate("scouting_final")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue to End Game")
            }
        }
    }
}