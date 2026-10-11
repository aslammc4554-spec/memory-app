package com.experiment.memory.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SessionState(
    val participantId: String,
    val groupCondition: String,
    val currentListIndex: Int,
    val listOrder: List<Int>,
    val targetPositions: List<Int>,
    val probeTypes: List<String>,
    val isProbesAssigned: Boolean,
    val isPracticeCompleted: Boolean,
    val isFinished: Boolean
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "memory_experiment_session"
        private const val KEY_PARTICIPANT_ID = "participant_id"
        private const val KEY_GROUP_CONDITION = "group_condition"
        private const val KEY_CURRENT_LIST_INDEX = "current_list_index"
        private const val KEY_LIST_ORDER = "list_order"
        private const val KEY_TARGET_POSITIONS = "target_positions"
        private const val KEY_PROBE_TYPES = "probe_types"
        private const val KEY_PROBES_ASSIGNED = "is_probes_assigned"
        private const val KEY_PRACTICE_COMPLETED = "is_practice_completed"
        private const val KEY_IS_FINISHED = "is_finished"
    }

    fun hasActiveSession(): Boolean {
        val participantId = prefs.getString(KEY_PARTICIPANT_ID, null)
        val isFinished = prefs.getBoolean(KEY_IS_FINISHED, false)
        return !participantId.isNullOrEmpty() && !isFinished
    }

    fun loadSession(): SessionState? {
        val participantId = prefs.getString(KEY_PARTICIPANT_ID, null) ?: return null
        val isFinished = prefs.getBoolean(KEY_IS_FINISHED, false)
        if (isFinished) return null

        val groupCondition = prefs.getString(KEY_GROUP_CONDITION, "Predictable") ?: "Predictable"
        val currentListIndex = prefs.getInt(KEY_CURRENT_LIST_INDEX, 0)
        val isProbesAssigned = prefs.getBoolean(KEY_PROBES_ASSIGNED, false)
        val isPracticeCompleted = prefs.getBoolean(KEY_PRACTICE_COMPLETED, false)

        val listOrderJson = prefs.getString(KEY_LIST_ORDER, null)
        val targetPositionsJson = prefs.getString(KEY_TARGET_POSITIONS, null)
        val probeTypesJson = prefs.getString(KEY_PROBE_TYPES, null)

        val intListType = object : TypeToken<List<Int>>() {}.type
        val stringListType = object : TypeToken<List<String>>() {}.type

        val listOrder: List<Int> = if (listOrderJson != null) gson.fromJson(listOrderJson, intListType) else emptyList()
        val targetPositions: List<Int> = if (targetPositionsJson != null) gson.fromJson(targetPositionsJson, intListType) else emptyList()
        val probeTypes: List<String> = if (probeTypesJson != null) gson.fromJson(probeTypesJson, stringListType) else emptyList()

        return SessionState(
            participantId = participantId,
            groupCondition = groupCondition,
            currentListIndex = currentListIndex,
            listOrder = listOrder,
            targetPositions = targetPositions,
            probeTypes = probeTypes,
            isProbesAssigned = isProbesAssigned,
            isPracticeCompleted = isPracticeCompleted,
            isFinished = isFinished
        )
    }

    fun saveSession(state: SessionState) {
        prefs.edit().apply {
            putString(KEY_PARTICIPANT_ID, state.participantId)
            putString(KEY_GROUP_CONDITION, state.groupCondition)
            putInt(KEY_CURRENT_LIST_INDEX, state.currentListIndex)
            putString(KEY_LIST_ORDER, gson.toJson(state.listOrder))
            putString(KEY_TARGET_POSITIONS, gson.toJson(state.targetPositions))
            putString(KEY_PROBE_TYPES, gson.toJson(state.probeTypes))
            putBoolean(KEY_PROBES_ASSIGNED, state.isProbesAssigned)
            putBoolean(KEY_PRACTICE_COMPLETED, state.isPracticeCompleted)
            putBoolean(KEY_IS_FINISHED, state.isFinished)
            apply()
        }
    }

    fun updateListIndex(index: Int) {
        prefs.edit().putInt(KEY_CURRENT_LIST_INDEX, index).apply()
    }

    fun saveProbeAssignments(probeTypes: List<String>, isProbesAssigned: Boolean = true, isPracticeCompleted: Boolean = true) {
        prefs.edit().apply {
            putString(KEY_PROBE_TYPES, gson.toJson(probeTypes))
            putBoolean(KEY_PROBES_ASSIGNED, isProbesAssigned)
            putBoolean(KEY_PRACTICE_COMPLETED, isPracticeCompleted)
            apply()
        }
    }

    fun markFinished() {
        prefs.edit().putBoolean(KEY_IS_FINISHED, true).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
