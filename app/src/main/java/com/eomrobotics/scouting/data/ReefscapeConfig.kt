package com.eomrobotics.scouting.data

object ReefscapeConfig {
    val autoCoralActions = listOf("L4", "L3", "L2", "L1")
    val autoAlgaeActions = listOf("Net", "Processor")

    val teleopCoralActions = listOf("L4", "L3", "L2", "L1")
    val teleopAlgaeActionsRobot = listOf("Net 🤖", "Processor")
    val teleopAlgaeActionsHuman = listOf("Net 🚶")

    val endGameOptions = listOf("None", "Park", "Shallow Climb", "Deep Climb")
    val driveStations = listOf("R1", "R2", "R3", "B1", "B2", "B3")

    const val AUTO_DURATION = 15
    const val TELEOP_DURATION = 135
}