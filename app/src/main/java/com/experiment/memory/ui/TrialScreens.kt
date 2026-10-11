package com.experiment.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun PreCueScreen(targetPosition: Int, onFinished: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your speaking position is Word #$targetPosition",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onFinished) {
            Text("Continue")
        }
    }
}

@Composable
fun WordListScreen(
    words: List<String>,
    targetPosition: Int,
    groupCondition: String,
    wordIntervalMs: Long,
    onFinished: (Long?, Boolean) -> Unit
) {
    var currentIndex by remember(words, targetPosition) { mutableStateOf(-1) }
    var continueVisible by remember(words, targetPosition) { mutableStateOf(false) }
    var continuePressed by remember(words, targetPosition) { mutableStateOf(false) }
    var targetOnset by remember(words, targetPosition) { mutableStateOf(0L) }
    var rtMs by remember(words, targetPosition) { mutableStateOf<Long?>(null) }
    var speakingResponseGiven by remember(words, targetPosition) { mutableStateOf(false) }

    LaunchedEffect(words, targetPosition) {
        currentIndex = -1
        continueVisible = false
        continuePressed = false
        targetOnset = 0L
        rtMs = null
        speakingResponseGiven = false

        for (i in words.indices) {
            currentIndex = i
            if (i + 1 == targetPosition) {
                continueVisible = true
                targetOnset = System.currentTimeMillis()
                // Wait until the participant clicks Continue
                snapshotFlow { continuePressed }.first { it }
                continueVisible = false
            } else {
                delay(wordIntervalMs)
            }
        }
        onFinished(rtMs, speakingResponseGiven)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (groupCondition == "Predictable") {
            Text(
                "Speak at Word #$targetPosition",
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                fontSize = 16.sp
            )
        }

        if (currentIndex >= 0 && currentIndex < words.size) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (groupCondition == "Unpredictable" && currentIndex + 1 == targetPosition) {
                    Text("Read this word aloud", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(words[currentIndex], fontSize = 48.sp, fontWeight = FontWeight.Bold)
                
                if (continueVisible) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (!speakingResponseGiven) {
                                rtMs = System.currentTimeMillis() - targetOnset
                                speakingResponseGiven = true
                                continuePressed = true
                            }
                        },
                        enabled = !speakingResponseGiven
                    ) {
                        Text(if (speakingResponseGiven) "Recorded" else "Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun DistractorScreen(durationMs: Long, onFinished: () -> Unit) {
    var timeLeft by remember(durationMs) { mutableStateOf(durationMs / 1000) }
    
    LaunchedEffect(durationMs) {
        timeLeft = durationMs / 1000
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onFinished()
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Count backwards from 100 in steps of 3", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Text(timeLeft.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold)
    }
}
