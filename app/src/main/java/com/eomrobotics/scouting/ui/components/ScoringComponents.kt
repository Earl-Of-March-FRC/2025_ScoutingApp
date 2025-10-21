package com.eomrobotics.scouting.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScoringRowMadeMissed(
    label: String,
    made: Int,
    missed: Int,
    onMadeIncrement: () -> Unit,
    onMadeDecrement: () -> Unit,
    onMissedIncrement: () -> Unit,
    onMissedDecrement: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Made
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text("Made:", modifier = Modifier.width(60.dp))
                IconButton(onClick = onMadeDecrement, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(20.dp))
                }
                Text(
                    text = made.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.widthIn(min = 30.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onMadeIncrement, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(20.dp))
                }
            }

            // Missed
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text("Missed:", modifier = Modifier.width(60.dp))
                IconButton(onClick = onMissedDecrement, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(20.dp))
                }
                Text(
                    text = missed.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.widthIn(min = 30.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onMissedIncrement, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(20.dp))
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}