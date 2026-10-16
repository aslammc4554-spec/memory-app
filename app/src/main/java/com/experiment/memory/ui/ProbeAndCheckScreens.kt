package com.experiment.memory.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun ProbeScreen(
    words: List<String>,
    targetPosition: Int, // 1-indexed
    probeType: String,
    onAnswered: (String, String, String, String, Int) -> Unit
) {
    val probeData = remember(words, targetPosition, probeType) {
        generateProbe(words, targetPosition, probeType)
    }
    var isSubmitted by remember(words, targetPosition, probeType) { mutableStateOf(false) }
    var selectedWord by remember(words, targetPosition, probeType) { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Which word appeared immediately before",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = probeData.wordAsked,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tap the correct word below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Responsive 2 × 2 grid of 4 answer options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Row 1: Top-Left (option 0) & Top-Right (option 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OptionButton(
                        word = probeData.options.getOrElse(0) { "" },
                        isSelected = selectedWord == probeData.options.getOrElse(0) { "" },
                        isEnabled = !isSubmitted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSubmitted) {
                                isSubmitted = true
                                val chosen = probeData.options.getOrElse(0) { "" }
                                selectedWord = chosen
                                val acc = if (chosen == probeData.correctAnswer) 1 else 0
                                onAnswered(probeData.wordAsked, probeData.optionsShownLeft, probeData.optionsShownRight, chosen, acc)
                            }
                        }
                    )
                    OptionButton(
                        word = probeData.options.getOrElse(1) { "" },
                        isSelected = selectedWord == probeData.options.getOrElse(1) { "" },
                        isEnabled = !isSubmitted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSubmitted) {
                                isSubmitted = true
                                val chosen = probeData.options.getOrElse(1) { "" }
                                selectedWord = chosen
                                val acc = if (chosen == probeData.correctAnswer) 1 else 0
                                onAnswered(probeData.wordAsked, probeData.optionsShownLeft, probeData.optionsShownRight, chosen, acc)
                            }
                        }
                    )
                }

                // Row 2: Bottom-Left (option 2) & Bottom-Right (option 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OptionButton(
                        word = probeData.options.getOrElse(2) { "" },
                        isSelected = selectedWord == probeData.options.getOrElse(2) { "" },
                        isEnabled = !isSubmitted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSubmitted) {
                                isSubmitted = true
                                val chosen = probeData.options.getOrElse(2) { "" }
                                selectedWord = chosen
                                val acc = if (chosen == probeData.correctAnswer) 1 else 0
                                onAnswered(probeData.wordAsked, probeData.optionsShownLeft, probeData.optionsShownRight, chosen, acc)
                            }
                        }
                    )
                    OptionButton(
                        word = probeData.options.getOrElse(3) { "" },
                        isSelected = selectedWord == probeData.options.getOrElse(3) { "" },
                        isEnabled = !isSubmitted,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isSubmitted) {
                                isSubmitted = true
                                val chosen = probeData.options.getOrElse(3) { "" }
                                selectedWord = chosen
                                val acc = if (chosen == probeData.correctAnswer) 1 else 0
                                onAnswered(probeData.wordAsked, probeData.optionsShownLeft, probeData.optionsShownRight, chosen, acc)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionButton(
    word: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.heightIn(min = 60.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        border = BorderStroke(1.5.dp, borderColor),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = word,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

data class ProbeData(
    val wordAsked: String,
    val correctAnswer: String,
    val options: List<String>
) {
    constructor(wordAsked: String, correctAnswer: String, leftOption: String, rightOption: String) :
        this(wordAsked, correctAnswer, listOf(leftOption, rightOption))

    val optionsShownLeft: String get() = if (options.size >= 4) "${options[0]} | ${options[2]}" else leftOption
    val optionsShownRight: String get() = if (options.size >= 4) "${options[1]} | ${options[3]}" else rightOption

    val leftOption: String get() = options.getOrElse(0) { "" }
    val rightOption: String get() = options.getOrElse(1) { "" }
}

private val FALLBACK_DISTRACTORS = listOf(
    "Apple", "Bicycle", "Clock", "Daisy", "Earth",
    "Folder", "Garlic", "House", "Jacket", "Lemon",
    "Mirror", "Paper", "Rabbit", "Silver", "Window",
    "Pencil", "Orange", "River", "Garden", "Market"
)

fun generateProbe(
    words: List<String>,
    targetPosition: Int,
    probeType: String,
    random: Random = Random.Default
): ProbeData {
    if (words.size < 2) {
        val wordAsked = words.firstOrNull() ?: "Target"
        val correctAnswer = "Answer"
        val distractors = FALLBACK_DISTRACTORS
            .filter { it != correctAnswer && it != wordAsked }
            .shuffled(random)
            .take(3)
        val opts = (listOf(correctAnswer) + distractors).shuffled(random)
        return ProbeData(wordAsked, correctAnswer, opts)
    }

    val targetIndex = (targetPosition - 1).coerceIn(1, words.size - 1)

    val testedIndex = if (probeType == "Critical") {
        targetIndex
    } else {
        val possibleIndices = (1 until words.size).filter { it != targetIndex }
        if (possibleIndices.isNotEmpty()) possibleIndices.random(random) else targetIndex
    }

    val wordAsked = words.getOrElse(testedIndex) { words.last() }
    val correctAnswer = words.getOrElse(testedIndex - 1) { words.first() }

    // Collect candidate distractors from the same list, excluding correct answer and target word
    val candidateDistractors = words
        .filter { it != correctAnswer && it != wordAsked }
        .distinct()
        .shuffled(random)
        .toMutableList()

    val chosenDistractors = mutableListOf<String>()
    for (cand in candidateDistractors) {
        if (chosenDistractors.size == 3) break
        if (!chosenDistractors.contains(cand)) {
            chosenDistractors.add(cand)
        }
    }

    // In edge cases where words in list are fewer than needed, backfill with fallback words
    if (chosenDistractors.size < 3) {
        for (fallback in FALLBACK_DISTRACTORS.shuffled(random)) {
            if (chosenDistractors.size == 3) break
            if (fallback != correctAnswer && fallback != wordAsked && !chosenDistractors.contains(fallback)) {
                chosenDistractors.add(fallback)
            }
        }
    }

    val options = (listOf(correctAnswer) + chosenDistractors).shuffled(random)
    return ProbeData(wordAsked, correctAnswer, options)
}

@Composable
fun ManipulationCheckScreen(onAnswered: (Int, Int) -> Unit) {
    var thinking by remember(onAnswered) { mutableStateOf(0) }
    var anxiety by remember(onAnswered) { mutableStateOf(0) }
    var submitted by remember(onAnswered) { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Subjective Ratings",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "Post-List Check",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Rate your experience during the word list just completed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Question 1 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "While viewing the list, how much did you think about the word you had to say aloud?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RatingScale(selected = thinking, onSelect = { thinking = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question 2 Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "During the list, how anxious or on edge did you feel about your upcoming speaking turn?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RatingScale(selected = anxiety, onSelect = { anxiety = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!submitted && thinking > 0 && anxiety > 0) {
                        submitted = true
                        onAnswered(thinking, anxiety)
                    }
                },
                enabled = thinking > 0 && anxiety > 0 && !submitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
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
fun RatingScale(selected: Int, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..7) {
                val isSelected = selected == i
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(i) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = i.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "1: Not at all",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "7: Very much",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
