package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MemoryCategory
import com.example.data.model.NeuralMemory
import com.example.ui.components.GlassCard
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.SunsetOrange

@Composable
fun MemoryScreen(
  memories: List<NeuralMemory>,
  onAddMemory: (String, String, MemoryCategory) -> Unit,
  onDeleteMemory: (NeuralMemory) -> Unit,
  onClearAll: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAddDialog by remember { mutableStateOf(false) }
  var showClearConfirm by remember { mutableStateOf(false) }

  var newKey by remember { mutableStateOf("") }
  var newValue by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(MemoryCategory.PREFERENCE) }

  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Store New Memory") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Add a fact or preference for KIN AI to remember during conversations.")
          OutlinedTextField(
            value = newKey,
            onValueChange = { newKey = it },
            label = { Text("Topic / Subject") },
            placeholder = { Text("e.g., Favorite Framework, User City") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = newValue,
            onValueChange = { newValue = it },
            label = { Text("Memory Details") },
            placeholder = { Text("e.g., Prefers Kotlin Compose with M3") },
            modifier = Modifier.fillMaxWidth()
          )

          Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MemoryCategory.values().take(3).forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PeachCoral,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newKey.isNotBlank() && newValue.isNotBlank()) {
              onAddMemory(newKey, newValue, selectedCategory)
              newKey = ""
              newValue = ""
              showAddDialog = false
            } else {
              Toast.makeText(context, "Please fill in topic and details", Toast.LENGTH_SHORT).show()
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = PeachCoral)
        ) {
          Text("Save Memory")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All Neural Memories?") },
      text = { Text("This will permanently remove all stored facts from local SQLite storage.") },
      confirmButton = {
        Button(
          onClick = {
            showClearConfirm = false
            onClearAll()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Clear All")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(20.dp))
        }

        Column {
          Text(
            text = "NEURAL MEMORY BANK",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${memories.size} stored facts active",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        IconButton(
          onClick = { showAddDialog = true },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PeachCoral)
            .testTag("memory_add_btn")
        ) {
          Icon(Icons.Filled.Add, "Add memory", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        if (memories.isNotEmpty()) {
          IconButton(
            onClick = { showClearConfirm = true },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Filled.DeleteSweep, "Clear all", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    if (memories.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(PeachCoral.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Filled.Psychology, null, tint = PeachCoral, modifier = Modifier.size(30.dp))
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "No Memories Stored Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Teach KIN AI your preferences, project stacks, and habits. These are seamlessly injected into prompt context.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
          Spacer(modifier = Modifier.height(14.dp))
          Button(
            onClick = { showAddDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PeachCoral)
          ) {
            Text("Add First Memory")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(memories, key = { it.id }) { mem ->
          GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(SunsetOrange.copy(alpha = 0.2f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = mem.category.name,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = SunsetOrange
                    )
                  }
                  Text(
                    text = mem.key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = mem.value,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              IconButton(
                onClick = { onDeleteMemory(mem) },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = "Delete memory",
                  tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
