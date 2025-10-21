package com.eomrobotics.scouting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.eomrobotics.scouting.data.ScoutingDatabase
import com.eomrobotics.scouting.ui.screens.*
import com.eomrobotics.scouting.ui.theme.FRCScoutingTheme
import com.eomrobotics.scouting.viewmodel.ScoutingViewModel

class MainActivity : ComponentActivity() {
    private lateinit var database: ScoutingDatabase
    private lateinit var viewModel: ScoutingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = Room.databaseBuilder(
            applicationContext,
            ScoutingDatabase::class.java,
            "scouting_database"
        ).build()

        // Initialize ViewModel
        viewModel = ScoutingViewModel(database)

        setContent {
            FRCScoutingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FRCScoutingApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FRCScoutingApp(viewModel: ScoutingViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("scouter_setup") {
            ScouterSetupScreen(navController, viewModel)
        }
        composable("scouting_auto") {
            ScoutingAutoScreen(navController, viewModel)
        }
        composable("scouting_teleop") {
            ScoutingTeleopScreen(navController, viewModel)
        }
        composable("scouting_final") {
            ScoutingFinalScreen(navController, viewModel)
        }
        composable("manager") {
            ManagerScreen(navController)
        }
        composable("qr_scanner") {
            QRScannerScreen(navController, viewModel)
        }
        composable("spreadsheet") {
            SpreadsheetScreen(navController, viewModel)
        }
    }
}