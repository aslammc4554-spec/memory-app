package com.experiment.memory.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DebriefScreen(contactName: String, contactEmail: String, onSubmit: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Thank you for participating!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("This study examined how knowing when your speaking turn will come affects memory for information presented immediately before your turn, a phenomenon known as the next-in-line effect.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("During the task, you were asked to read words aloud when it was your turn. Participants were randomly assigned to one of two conditions: a Predictable group, in which their speaking position was announced in advance, or an Unpredictable group, in which their speaking position was not announced beforehand. We also examined memory for the word presented immediately before the word you read aloud.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("This information was not fully explained before the task because knowing the experimental condition and the specific memory measure could have influenced participants' attention, expectations, and performance. This partial withholding of information was therefore necessary to maintain the naturalness and validity of responses.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your responses will be kept confidential and will be used only for the purposes of this research.")
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Data-Use Decision", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Now that you have been informed about the full purpose of the study, please indicate whether you agree to your responses being used for this research.")
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "Agree",
                onClick = { selectedOption = "Agree" }
            )
            Text("I agree to the use of my data")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedOption == "Withdraw",
                onClick = { selectedOption = "Withdraw" }
            )
            Text("I do not agree and wish to withdraw my data")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { selectedOption?.let { onSubmit(it) } },
            enabled = selectedOption != null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Submit")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("Questions or concerns? Contact $contactName at $contactEmail.", fontSize = 14.sp)
    }
}
