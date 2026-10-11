package com.experiment.memory.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onNext: () -> Unit, onAdminMode: () -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to the Memory Experiment", 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tapCount++
                if (tapCount >= 5) {
                    tapCount = 0
                    onAdminMode()
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Please read the following instructions carefully.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNext) {
            Text("Begin")
        }
    }
}

@Composable
fun ConsentScreen(contactName: String, contactEmail: String, onConsent: () -> Unit) {
    var ageChecked by remember { mutableStateOf(false) }
    var readChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Informed Consent: Memory for Spoken Words", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Conducted by $contactName.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("You are invited to take part in a study about how people remember words shown one at a time.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("What you will do: read short lists of words on screen, read one word aloud in each list, do a brief counting task, and answer simple memory questions. You will also rate your thoughts and feelings during the task. Your voice is not recorded. The session takes about 15 minutes.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Who can take part: adults aged between 18-29 who can read English words aloud.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Voluntary participation: Taking part is voluntary.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Confidentiality: No name or email is collected. Your responses are stored under a participant ID, seen only by the research team, used only for this research, and reported only as group results.")
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tick both boxes to continue:", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = ageChecked, onCheckedChange = { ageChecked = it })
            Text("I am 18 years of age or over.")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = readChecked, onCheckedChange = { readChecked = it })
            Text("I have read and understood the above information carefully and I consent to take part.")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onConsent,
            enabled = ageChecked && readChecked,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun InstructionsScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Instructions", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Words will appear one at a time on the screen.\n\nRead the designated word aloud when it is your turn.\n\nAfter reading it aloud, click 'Continue'.\n\nAfter each list, you will do a short counting task, then answer a memory question.")
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNext) {
            Text("Start Practice")
        }
    }
}

@Composable
fun PracticeCompleteScreen(onContinue: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "The practice is now complete. The real experiment begins next. There will be 8 lists. Click Continue when you are ready.",
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onContinue) {
            Text("Continue")
        }
    }
}

@Composable
fun FinishedScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your response has been recorded.", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("You may now close this app.")
    }
}
