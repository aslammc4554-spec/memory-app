package com.experiment.memory.ui

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.experiment.memory.data.ExperimentDatabase
import com.experiment.memory.data.TrialData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AdminScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var trialData by remember { mutableStateOf<List<TrialData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = ExperimentDatabase.getDatabase(context)
        trialData = db.trialDataDao().getAllData()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Researcher / Admin Mode", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onClose) {
                Text("Close")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = {
            coroutineScope.launch {
                exportDataToCsv(context, trialData)
            }
        }) {
            Text("Export Data to CSV")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text("Total Records: ${trialData.size}")
            LazyColumn {
                items(trialData) { data ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Participant: ${data.participant_id} | Group: ${data.group_condition}")
                            Text("List: ${data.list_number} | Mode: ${data.mode} | Use: ${data.data_use_decision}")
                        }
                    }
                }
            }
        }
    }
}

suspend fun exportDataToCsv(context: Context, data: List<TrialData>) {
    withContext(Dispatchers.IO) {
        try {
            val csvContent = StringBuilder().apply {
                append("id,participant_id,group_condition,list_number,presentation_order,target_position,probe_type,probe_word_asked,options_shown_left,options_shown_right,response,accuracy,continue_rt_ms,speaking_response_given,manip_thinking,manip_anxiety,practice,mode,data_use_decision,timestamp_start,timestamp_end,excluded\n")
                for (row in data) {
                    append("${row.id},${row.participant_id},${row.group_condition},${row.list_number},${row.presentation_order},${row.target_position},${row.probe_type},${row.probe_word_asked},${row.options_shown_left},${row.options_shown_right},${row.response},${row.accuracy},${row.continue_rt_ms ?: ""},${row.speaking_response_given},${row.manip_thinking ?: ""},${row.manip_anxiety ?: ""},${row.practice},${row.mode},${row.data_use_decision ?: ""},${row.timestamp_start},${row.timestamp_end},${row.excluded ?: ""}\n")
                }
            }.toString()

            val fileName = "experiment_data_${System.currentTimeMillis()}.csv"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csvContent.toByteArray())
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                file.writeText(csvContent)
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Exported $fileName to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
