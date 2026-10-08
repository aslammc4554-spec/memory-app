package com.experiment.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun PreCueScreen(targetPosition: Int, onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Your speaking position is Word #$targetPosition", fontSize = 32.sp, fontWeight = FontWeight.Bold)
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
    var currentIndex by remember { mutableStateOf(-1) }
    var continueVisible by remember { mutableStateOf(false) }
    var targetOnset by remember { mutableStateOf(0L) }
    var rtMs by remember { mutableStateOf<Long?>(null) }
    var speakingResponseGiven by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        for (i in words.indices) {
            currentIndex = i
            if (i + 1 == targetPosition) {
                continueVisible = true
                targetOnset = System.currentTimeMillis()
            }
            delay(wordIntervalMs)
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
    var timeLeft by remember { mutableStateOf(durationMs / 1000) }
    
    LaunchedEffect(Unit) {
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
