package com.experiment.memory.data

import android.content.Context
import androidx.room.*

@Entity(tableName = "trial_data")
data class TrialData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val participant_id: String,
    val group_condition: String, // Predictable / Unpredictable
    val list_number: Int, // 1-8. Practice is 0
    val presentation_order: Int,
    val target_position: Int,
    val probe_type: String, // Critical / Control
    val probe_word_asked: String,
    val options_shown_left: String,
    val options_shown_right: String,
    val response: String,
    val accuracy: Int, // 1 or 0
    val continue_rt_ms: Long?,
    val speaking_response_given: Boolean,
    val manip_thinking: Int?, // 1-7
    val manip_anxiety: Int?, // 1-7
    val practice: Int, // 1 or 0
    val mode: String, // pilot / main
    var data_use_decision: String? = null, // Agree / Withdraw
    val timestamp_start: Long,
    val timestamp_end: Long,
    var excluded: String? = null // Reason if excluded
)

@Dao
interface TrialDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrialData(data: TrialData)

    @Query("UPDATE trial_data SET data_use_decision = :decision WHERE participant_id = :participantId")
    suspend fun updateDataUseDecision(participantId: String, decision: String)

    @Query("SELECT * FROM trial_data")
    suspend fun getAllData(): List<TrialData>
    
    @Query("SELECT * FROM trial_data WHERE participant_id = :participantId")
    suspend fun getDataForParticipant(participantId: String): List<TrialData>
}

@Database(entities = [TrialData::class], version = 1, exportSchema = false)
abstract class ExperimentDatabase : RoomDatabase() {
    abstract fun trialDataDao(): TrialDataDao

    companion object {
        @Volatile
        private var INSTANCE: ExperimentDatabase? = null

        fun getDatabase(context: Context): ExperimentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExperimentDatabase::class.java,
                    "experiment_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
