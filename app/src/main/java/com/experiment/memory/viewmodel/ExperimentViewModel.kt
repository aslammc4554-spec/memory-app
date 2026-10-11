package com.experiment.memory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.memory.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

enum class ScreenState {
    WELCOME, CONSENT, INSTRUCTIONS, 
    TRIAL_PRECUE, TRIAL_WORDLIST, TRIAL_DISTRACTOR, TRIAL_PROBE, TRIAL_MANIPULATION_CHECK,
    PRACTICE_COMPLETE,
    DEBRIEF, FINISHED, ADMIN
}

class ExperimentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ExperimentDatabase.getDatabase(application)
    
    private val _currentScreen = MutableStateFlow(ScreenState.WELCOME)
    val currentScreen: StateFlow<ScreenState> = _currentScreen

    fun openAdmin() { _currentScreen.value = ScreenState.ADMIN }
    fun closeAdmin() { _currentScreen.value = ScreenState.WELCOME }

    val participantId = "P-" + java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(Date()) + "-" + UUID.randomUUID().toString().substring(0, 6).uppercase()
    val groupCondition = if (Random.nextBoolean()) "Predictable" else "Unpredictable"
    var config: ExperimentConfig = ExperimentConfig()
    var wordLists: WordLists = loadDefaultWordLists()
    
    var currentListIndex = 0 // 0 is practice, 1-8 are experimental
    var listOrder = mutableListOf<Int>()
    var targetPositions = mutableListOf<Int>()
    var probeTypes = mutableListOf<String>()
    
    var currentTrialData: TrialDataBuilder? = null
    var sessionStartTime = 0L

    init {
        // Prepare list orders, target positions, probes
        val expLists = (1..8).toList().shuffled()
        listOrder.add(0) // practice
        listOrder.addAll(expLists)
        
        // 8 lists: balance target positions
        val positions = mutableListOf<Int>()
        val basePositions = config.target_positions
        while (positions.size < 8) {
            positions.addAll(basePositions)
        }
        positions.shuffle()
        
        targetPositions.add(Random.nextInt(4, 8)) // practice
        targetPositions.addAll(positions.take(8))

        // Probes: 4 Critical, 4 Control for the 8 lists
        val pTypes = mutableListOf("Critical", "Critical", "Critical", "Critical", "Control", "Control", "Control", "Control")
        pTypes.shuffle()
        probeTypes.add("Control") // Practice is always Control
        probeTypes.addAll(pTypes)
    }
    
    private fun loadDefaultWordLists(): WordLists {
        return WordLists(
            practice = listOf("Bicycle", "Library", "Pillow", "Toaster", "Sheep", "Wallet", "Ground", "Ladder", "Finger", "Camera"),
            list_1 = listOf("Basket", "Rabbit", "Shirt", "Ankle", "Wheel", "Banana", "Clock", "Doctor", "Grass", "Stapler"),
            list_2 = listOf("Bottle", "Button", "Jacket", "Paper", "Pencil", "Window", "Mirror", "Peach", "Towel", "Potato"),
            list_3 = listOf("Garlic", "Field", "Muscle", "Onion", "Paint", "Pastry", "Purse", "Student", "Radio", "School"),
            list_4 = listOf("Bread", "Chair", "Daisy", "Cream", "Tongue", "Glass", "Fridge", "River", "Candle", "Police"),
            list_5 = listOf("Apple", "Brush", "Child", "Crumb", "Earth", "Glove", "Grape", "Person", "Jeans", "Lemon"),
            list_6 = listOf("Gallery", "Lettuce", "Liquid", "Melon", "Pasta", "Painter", "Tomato", "Peanut", "House", "Phone"),
            list_7 = listOf("Cottage", "Butter", "Sister", "Folder", "Steak", "Stomach", "Skirt", "Snack", "Carrot", "Throat"),
            list_8 = listOf("Village", "Shelf", "Carpet", "Scarf", "Burger", "Wrist", "Table", "Coffee", "Plate", "Cheese")
        )
    }

    fun startExperiment() {
        sessionStartTime = System.currentTimeMillis()
        _currentScreen.value = ScreenState.CONSENT
    }

    fun acceptConsent() {
        _currentScreen.value = ScreenState.INSTRUCTIONS
    }

    fun startTrials() {
        currentListIndex = 0
        startNextTrial()
    }

    private fun startNextTrial() {
        if (currentListIndex > 8 || currentListIndex >= listOrder.size) {
            _currentScreen.value = ScreenState.DEBRIEF
            return
        }
        
        val actualListNum = listOrder.getOrElse(currentListIndex) { 0 }
        val targetPos = targetPositions.getOrElse(currentListIndex) { 4 }
        val probeTp = probeTypes.getOrElse(currentListIndex) { "Control" }

        currentTrialData = TrialDataBuilder(
            participantId = participantId,
            groupCondition = groupCondition,
            listNumber = actualListNum,
            presentationOrder = currentListIndex,
            targetPosition = targetPos,
            probeType = probeTp,
            practice = if (currentListIndex == 0) 1 else 0,
            mode = config.mode,
            timestampStart = System.currentTimeMillis()
        )
        
        if (groupCondition == "Predictable") {
            _currentScreen.value = ScreenState.TRIAL_PRECUE
        } else {
            _currentScreen.value = ScreenState.TRIAL_WORDLIST
        }
    }

    fun preCueFinished() {
        _currentScreen.value = ScreenState.TRIAL_WORDLIST
    }

    fun wordListFinished(continueRt: Long?, speakingResponseGiven: Boolean) {
        currentTrialData?.continueRtMs = continueRt
        currentTrialData?.speakingResponseGiven = speakingResponseGiven
        _currentScreen.value = ScreenState.TRIAL_DISTRACTOR
    }

    fun distractorFinished() {
        _currentScreen.value = ScreenState.TRIAL_PROBE
    }

    fun probeAnswered(probeWordAsked: String, optionsLeft: String, optionsRight: String, response: String, accuracy: Int) {
        currentTrialData?.probeWordAsked = probeWordAsked
        currentTrialData?.optionsShownLeft = optionsLeft
        currentTrialData?.optionsShownRight = optionsRight
        currentTrialData?.response = response
        currentTrialData?.accuracy = accuracy
        
        if (currentListIndex in listOf(1, 4, 8)) {
            _currentScreen.value = ScreenState.TRIAL_MANIPULATION_CHECK
        } else {
            saveTrialAndProceed()
        }
    }

    fun manipulationCheckAnswered(thinking: Int, anxiety: Int) {
        currentTrialData?.manipThinking = thinking
        currentTrialData?.manipAnxiety = anxiety
        saveTrialAndProceed()
    }

    private fun saveTrialAndProceed() {
        currentTrialData?.timestampEnd = System.currentTimeMillis()
        val isPractice = (currentListIndex == 0)
        val builder = currentTrialData
        if (builder != null) {
            val data = builder.build()
            viewModelScope.launch {
                db.trialDataDao().insertTrialData(data)
                currentListIndex++
                if (isPractice) {
                    _currentScreen.value = ScreenState.PRACTICE_COMPLETE
                } else {
                    startNextTrial()
                }
            }
        } else {
            currentListIndex++
            if (isPractice) {
                _currentScreen.value = ScreenState.PRACTICE_COMPLETE
            } else {
                startNextTrial()
            }
        }
    }

    fun continueAfterPractice() {
        startNextTrial()
    }

    fun submitExperiment(dataUseDecision: String) {
        viewModelScope.launch {
            db.trialDataDao().updateDataUseDecision(participantId, dataUseDecision)
            _currentScreen.value = ScreenState.FINISHED
        }
    }
}

class TrialDataBuilder(
    val participantId: String,
    val groupCondition: String,
    val listNumber: Int,
    val presentationOrder: Int,
    val targetPosition: Int,
    val probeType: String,
    val practice: Int,
    val mode: String,
    val timestampStart: Long
) {
    var probeWordAsked: String = ""
    var optionsShownLeft: String = ""
    var optionsShownRight: String = ""
    var response: String = ""
    var accuracy: Int = 0
    var continueRtMs: Long? = null
    var speakingResponseGiven: Boolean = false
    var manipThinking: Int? = null
    var manipAnxiety: Int? = null
    var timestampEnd: Long = 0

    fun build(): TrialData {
        return TrialData(
            participant_id = participantId,
            group_condition = groupCondition,
            list_number = listNumber,
            presentation_order = presentationOrder,
            target_position = targetPosition,
            probe_type = probeType,
            probe_word_asked = probeWordAsked,
            options_shown_left = optionsShownLeft,
            options_shown_right = optionsShownRight,
            response = response,
            accuracy = accuracy,
            continue_rt_ms = continueRtMs,
            speaking_response_given = speakingResponseGiven,
            manip_thinking = manipThinking,
            manip_anxiety = manipAnxiety,
            practice = practice,
            mode = mode,
            timestamp_start = timestampStart,
            timestamp_end = timestampEnd
        )
    }
}
