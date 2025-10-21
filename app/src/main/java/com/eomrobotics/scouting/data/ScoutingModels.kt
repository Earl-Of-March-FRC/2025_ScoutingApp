package com.eomrobotics.scouting.data

import java.util.UUID

data class ScoutingEntry(
    val uuid: String = UUID.randomUUID().toString(),
    val teamNumber: Int,
    val matchNumber: Int,
    val driveStation: String,
    val scouterName: String,
    val autoData: AutoData,
    val teleopData: TeleopData,
    val endGame: String,
    val fouls: Int,
    val drivingRating: Int,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AutoData(
    val coralMade: Map<String, Int>,
    val coralMissed: Map<String, Int>,
    val algaeMade: Map<String, Int>,
    val algaeMissed: Map<String, Int>,
    val leave: Boolean
)

data class TeleopData(
    val coralMade: Map<String, Int>,
    val coralMissed: Map<String, Int>,
    val algaeRobotMade: Map<String, Int>,
    val algaeRobotMissed: Map<String, Int>,
    val algaeHumanMade: Map<String, Int>,
    val algaeHumanMissed: Map<String, Int>,
    val disconnects: Boolean
)

data class ImportResult(
    val imported: Int,
    val duplicates: Int,
    val error: String? = null
)