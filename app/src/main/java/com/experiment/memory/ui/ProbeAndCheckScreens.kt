package com.experiment.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val probeData = remember { generateProbe(words, targetPosition, probeType) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Which word appeared immediately before ${probeData.wordAsked}?", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Button(onClick = {
                val acc = if (probeData.leftOption == probeData.correctAnswer) 1 else 0
                onAnswered(probeData.wordAsked, probeData.leftOption, probeData.rightOption, probeData.leftOption, acc)
            }) {
                Text(probeData.leftOption, fontSize = 20.sp)
            }
            Button(onClick = {
                val acc = if (probeData.rightOption == probeData.correctAnswer) 1 else 0
                onAnswered(probeData.wordAsked, probeData.leftOption, probeData.rightOption, probeData.rightOption, acc)
            }) {
                Text(probeData.rightOption, fontSize = 20.sp)
            }
        }
    }
}

data class ProbeData(
    val wordAsked: String,
    val correctAnswer: String,
    val leftOption: String,
    val rightOption: String
)

fun generateProbe(words: List<String>, targetPosition: Int, probeType: String): ProbeData {
    val targetIndex = targetPosition - 1
    
    val testedIndex = if (probeType == "Critical") {
        targetIndex
    } else {
        // Control: any index from 1 to 9 EXCEPT targetIndex
        val possibleIndices = (1..9).filter { it != targetIndex }
        possibleIndices.random()
    }
    
    val wordAsked = words[testedIndex]
    val correctAnswer = words[testedIndex - 1]
    
    val possibleWrong = words.filter { it != correctAnswer && it != wordAsked }
    val wrongOption = possibleWrong.random()
    
    val options = listOf(correctAnswer, wrongOption).shuffled()
    return ProbeData(wordAsked, correctAnswer, options[0], options[1])
}

@Composable
fun ManipulationCheckScreen(onAnswered: (Int, Int) -> Unit) {
    var thinking by remember { mutableStateOf(0) }
    var anxiety by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("During this list, how much were you thinking about the word you would have to say aloud?", fontSize = 18.sp)
        RatingScale(selected = thinking, onSelect = { thinking = it })
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("During this list, how anxious or on edge did you feel about your upcoming speaking turn?", fontSize = 18.sp)
        RatingScale(selected = anxiety, onSelect = { anxiety = it })
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onAnswered(thinking, anxiety) },
            enabled = thinking > 0 && anxiety > 0
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun RatingScale(selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 1..7) {
            val isSelected = selected == i
            Button(
                onClick = { onSelect(i) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(i.toString())
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("1\nNot at all", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Text("7\nVery much", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
