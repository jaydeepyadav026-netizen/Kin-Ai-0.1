package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.automation.NetworkStatus
import com.example.ui.components.GlassCard
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.SunsetOrange

@Composable
fun SearchScreen(
  networkStatus: NetworkStatus,
  onPerformSearch: (String, String) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedProvider by remember { mutableStateOf("Google") }

  val providers = listOf("Google", "DuckDuckGo", "Wikipedia")

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
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
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
          text = "INTERNET SEARCH CORE",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Direct web query dispatch & live browser navigation",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    if (!networkStatus.isConnected) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Filled.WifiOff, null, tint = MaterialTheme.colorScheme.onErrorContainer)
          Text(
            text = "Internet connection unavailable. Web queries will require active network interface.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
          )
        }
      }
    }

    // Provider Selector Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      providers.forEach { provider ->
        FilterChip(
          selected = selectedProvider == provider,
          onClick = { selectedProvider = provider },
          label = { Text(provider, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = PeachCoral,
            selectedLabelColor = Color.White
          )
        )
      }
    }

    // Search Input Field & Action
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Search terms") },
      placeholder = { Text("Enter query to search on $selectedProvider...") },
      trailingIcon = {
        IconButton(
          onClick = {
            if (searchQuery.isNotBlank()) {
              onPerformSearch(searchQuery.trim(), selectedProvider)
            }
          },
          enabled = searchQuery.isNotBlank()
        ) {
          Icon(Icons.Filled.Search, "Execute search", tint = PeachCoral)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search_query_input"),
      shape = RoundedCornerShape(14.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Button(
      onClick = {
        if (searchQuery.isNotBlank()) {
          onPerformSearch(searchQuery.trim(), selectedProvider)
        }
      },
      enabled = searchQuery.isNotBlank(),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("search_submit_btn"),
      shape = RoundedCornerShape(14.dp),
      colors = ButtonDefaults.buttonColors(containerColor = PeachCoral)
    ) {
      Icon(Icons.Filled.OpenInBrowser, null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text("Search on $selectedProvider", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Quick Popular Topics
    Text(
      text = "EXPLORE QUICK TOPICS",
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = PeachCoral,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    listOf(
      "Jetpack Compose Best Practices 2026",
      "Artificial General Intelligence Updates",
      "Quantum Machine Learning Foundations",
      "Android Architecture Components"
    ).forEach { topic ->
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clickable {
            searchQuery = topic
            onPerformSearch(topic, selectedProvider)
          }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Filled.Public, null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
            Text(
              text = topic,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
          Icon(
            Icons.Filled.OpenInBrowser,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
