package com.eomrobotics.scouting.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eomrobotics.scouting.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScoutingViewModel(private val database: ScoutingDatabase) : ViewModel() {
    private val gson = Gson()

    private val _currentEntry = MutableStateFlow<ScoutingEntry?>(null)
    val currentEntry: StateFlow<ScoutingEntry?> = _currentEntry.asStateFlow()

    fun initializeEntry(teamNumber: Int, matchNumber: Int, driveStation: String, scouterName: String) {
        _currentEntry.value = ScoutingEntry(
            teamNumber = teamNumber,
            matchNumber = matchNumber,
            driveStation = driveStation,
            scouterName = scouterName,
            autoData = AutoData(emptyMap(), emptyMap(), emptyMap(), emptyMap(), false),
            teleopData = TeleopData(emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), false),
            endGame = "None",
            fouls = 0,
            drivingRating = 3,
            notes = ""
        )
    }

    fun setAutoData(autoData: AutoData) {
        _currentEntry.value = _currentEntry.value?.copy(autoData = autoData)
    }

    fun setTeleopData(teleopData: TeleopData) {
        _currentEntry.value = _currentEntry.value?.copy(teleopData = teleopData)
    }

    fun completeEntry(endGame: String, fouls: Int, drivingRating: Int, notes: String) {
        _currentEntry.value = _currentEntry.value?.copy(
            endGame = endGame,
            fouls = fouls,
            drivingRating = drivingRating,
            notes = notes
        )

        _currentEntry.value?.let { entry ->
            viewModelScope.launch {
                saveEntry(entry)
            }
        }
    }

    private suspend fun saveEntry(entry: ScoutingEntry) {
        val entity = ScoutingEntryEntity(
            uuid = entry.uuid,
            teamNumber = entry.teamNumber,
            matchNumber = entry.matchNumber,
            driveStation = entry.driveStation,
            scouterName = entry.scouterName,
            autoDataJson = gson.toJson(entry.autoData),
            teleopDataJson = gson.toJson(entry.teleopData),
            endGame = entry.endGame,
            fouls = entry.fouls,
            drivingRating = entry.drivingRating,
            notes = entry.notes,
            timestamp = entry.timestamp
        )
        database.scoutingDao().insertEntry(entity)
    }

    fun getAllEntries(): Flow<List<ScoutingEntry>> {
        return database.scoutingDao().getAllEntries().map { entities ->
            entities.map { entity ->
                ScoutingEntry(
                    uuid = entity.uuid,
                    teamNumber = entity.teamNumber,
                    matchNumber = entity.matchNumber,
                    driveStation = entity.driveStation,
                    scouterName = entity.scouterName,
                    autoData = gson.fromJson(entity.autoDataJson, AutoData::class.java),
                    teleopData = gson.fromJson(entity.teleopDataJson, TeleopData::class.java),
                    endGame = entity.endGame,
                    fouls = entity.fouls,
                    drivingRating = entity.drivingRating,
                    notes = entity.notes,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    suspend fun importFromQR(jsonString: String): ImportResult {
        return try {
            val entries: List<ScoutingEntry> = gson.fromJson(jsonString,
                object : TypeToken<List<ScoutingEntry>>() {}.type)

            var imported = 0
            var duplicates = 0

            entries.forEach { entry ->
                val exists = database.scoutingDao().entryExists(entry.uuid) > 0
                if (!exists) {
                    saveEntry(entry)
                    imported++
                } else {
                    duplicates++
                }
            }

            ImportResult(imported, duplicates)
        } catch (e: Exception) {
            ImportResult(0, 0, e.message)
        }
    }

    fun generateQRCodeBitmap(entries: List<ScoutingEntry>): Bitmap {
        val json = gson.toJson(entries)
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(json, BarcodeFormat.QR_CODE, 512, 512)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }
}