package com.eomrobotics.scouting.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scouting_entries")
data class ScoutingEntryEntity(
    @PrimaryKey val uuid: String,
    val teamNumber: Int,
    val matchNumber: Int,
    val driveStation: String,
    val scouterName: String,
    val autoDataJson: String,
    val teleopDataJson: String,
    val endGame: String,
    val fouls: Int,
    val drivingRating: Int,
    val notes: String,
    val timestamp: Long
)

@Dao
interface ScoutingDao {
    @Query("SELECT * FROM scouting_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<ScoutingEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEntry(entry: ScoutingEntryEntity): Long

    @Query("SELECT * FROM scouting_entries WHERE uuid = :uuid")
    suspend fun getEntryByUuid(uuid: String): ScoutingEntryEntity?

    @Query("SELECT COUNT(*) FROM scouting_entries WHERE uuid = :uuid")
    suspend fun entryExists(uuid: String): Int

    @Query("DELETE FROM scouting_entries")
    suspend fun deleteAll()
}

@Database(entities = [ScoutingEntryEntity::class], version = 1, exportSchema = false)
abstract class ScoutingDatabase : RoomDatabase() {
    abstract fun scoutingDao(): ScoutingDao
}