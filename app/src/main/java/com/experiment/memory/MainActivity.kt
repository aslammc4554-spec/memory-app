package com.experiment.memory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.experiment.memory.ui.*
import com.experiment.memory.viewmodel.ExperimentViewModel
import com.experiment.memory.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    private val viewModel: ExperimentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExperimentApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun ExperimentApp(viewModel: ExperimentViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    val listIndex = viewModel.currentListIndex
    val safeListNumber = viewModel.listOrder.getOrElse(listIndex) { 0 }
    val safeWords = viewModel.wordLists.getList(safeListNumber)
    val safeTargetPos = viewModel.targetPositions.getOrElse(listIndex) { 4 }
    val safeProbeType = viewModel.probeTypes.getOrElse(listIndex) { "Control" }

    when (currentScreen) {
        ScreenState.WELCOME -> WelcomeScreen(
            onNext = { viewModel.startExperiment() },
            onAdminMode = { viewModel.openAdmin() }
        )
        ScreenState.CONSENT -> ConsentScreen(
            contactName = viewModel.config.contact_name,
            contactEmail = viewModel.config.contact_email,
            onConsent = { viewModel.acceptConsent() }
        )
        ScreenState.INSTRUCTIONS -> InstructionsScreen(
            onNext = { viewModel.startTrials() }
        )
        ScreenState.TRIAL_PRECUE -> PreCueScreen(
            targetPosition = safeTargetPos,
            onFinished = { viewModel.preCueFinished() }
        )
        ScreenState.TRIAL_WORDLIST -> WordListScreen(
            words = safeWords,
            targetPosition = safeTargetPos,
            groupCondition = viewModel.groupCondition,
            wordIntervalMs = viewModel.config.word_interval_ms,
            onFinished = { rt, responded -> viewModel.wordListFinished(rt, responded) }
        )
        ScreenState.TRIAL_DISTRACTOR -> DistractorScreen(
            durationMs = viewModel.config.distractor_duration_ms,
            onFinished = { viewModel.distractorFinished() }
        )
        ScreenState.TRIAL_PROBE -> ProbeScreen(
            words = safeWords,
            targetPosition = safeTargetPos,
            probeType = safeProbeType,
            onAnswered = { asked, optL, optR, resp, acc -> viewModel.probeAnswered(asked, optL, optR, resp, acc) }
        )
        ScreenState.TRIAL_MANIPULATION_CHECK -> ManipulationCheckScreen(
            onAnswered = { t, a -> viewModel.manipulationCheckAnswered(t, a) }
        )
        ScreenState.PRACTICE_COMPLETE -> PracticeCompleteScreen(
            onContinue = { viewModel.continueAfterPractice() }
        )
        ScreenState.DEBRIEF -> DebriefScreen(
            contactName = viewModel.config.contact_name,
            contactEmail = viewModel.config.contact_email,
            onSubmit = { decision -> viewModel.submitExperiment(decision) }
        )
        ScreenState.FINISHED -> FinishedScreen()
        ScreenState.ADMIN -> AdminScreen(
            onClose = { viewModel.closeAdmin() }
        )
    }
}
