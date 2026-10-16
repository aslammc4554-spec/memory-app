package com.experiment.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Upcoming List Cue",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your speaking position is",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$targetPosition",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Read Word #$targetPosition aloud when it appears on the screen, then tap Continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onFinished,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
                // Wait until participant clicks Continue
                snapshotFlow { continuePressed }.first { it }
                continueVisible = false
            } else {
                delay(wordIntervalMs)
            }
        }
        onFinished(rtMs, speakingResponseGiven)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Condition indicator at top
        if (groupCondition == "Predictable") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Speak at Word #$targetPosition",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Fixed Center Presentation Area
        // This ensures the word stays perfectly in the middle of the display without jumping
        if (currentIndex in words.indices) {
            val isTargetWord = currentIndex + 1 == targetPosition

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fixed height container for prompt so word doesn't jump
                Box(
                    modifier = Modifier.height(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (groupCondition == "Unpredictable" && isTargetWord) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Read this word aloud",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Prominent Word Card / Display
                Text(
                    text = words[currentIndex],
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Spacer matching prompt area to balance center alignment
                Spacer(modifier = Modifier.height(36.dp))
            }
        }

        // Bottom Continue Button Area
        // Pinned to bottom so word position remains completely invariant
        if (continueVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (!speakingResponseGiven) {
                            rtMs = System.currentTimeMillis() - targetOnset
                            speakingResponseGiven = true
                            continuePressed = true
                        }
                    },
                    enabled = !speakingResponseGiven,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (speakingResponseGiven) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (speakingResponseGiven) "Recorded ✓" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DistractorScreen(durationMs: Long, startNumber: Int = 100, onFinished: () -> Unit) {
    var timeLeft by remember(durationMs, startNumber) { mutableStateOf(durationMs / 1000) }
    val totalSeconds = remember(durationMs) { (durationMs / 1000).coerceAtLeast(1) }

    LaunchedEffect(durationMs, startNumber) {
        timeLeft = durationMs / 1000
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Count backwards aloud in steps of 3",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Start from",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "$startNumber",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "e.g., $startNumber, ${startNumber - 3}, ${startNumber - 6}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Timer display
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$timeLeft",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft <= 5) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "sec",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { timeLeft.toFloat() / totalSeconds.toFloat() },
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
