package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.automation.BatteryStatus
import com.example.data.automation.NetworkStatus
import com.example.ui.Screen
import com.example.ui.theme.PeachCoral
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.SunsetGold

@Composable
fun KinAiTopBar(
  assistantName: String,
  batteryStatus: BatteryStatus,
  networkStatus: NetworkStatus,
  hasApiKey: Boolean,
  onNavigateToMemory: () -> Unit,
  onNavigateToSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Left: Brand identity & status
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(PeachCoral)
          .border(1.5.dp, SunsetGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "K",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 18.sp
        )
      }

      Column {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = assistantName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          // Live status dot
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (networkStatus.isConnected) StatusGreen else Color.Gray)
          )
        }

        Text(
          text = if (hasApiKey) "Gemini Cloud Online" else "KIN Neural Core (Offline/Ready)",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 10.sp
        )
      }
    }

    // Right: Device telemetry & Quick actions
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Telemetry pill (Battery + Network)
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          imageVector = if (batteryStatus.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
          contentDescription = "Battery ${batteryStatus.level}%",
          tint = if (batteryStatus.level < 20) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(14.dp)
        )
        Text(
          text = "${batteryStatus.level}%",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Icon(
          imageVector = if (networkStatus.isConnected) Icons.Filled.Wifi else Icons.Filled.WifiOff,
          contentDescription = "Network",
          tint = if (networkStatus.isConnected) PeachCoral else Color.Gray,
          modifier = Modifier.size(14.dp)
        )
      }

      // Memory button
      IconButton(
        onClick = onNavigateToMemory,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .testTag("top_bar_memory")
      ) {
        Icon(
          imageVector = Icons.Filled.Psychology,
          contentDescription = "Neural Memory Bank",
          tint = PeachCoral,
          modifier = Modifier.size(20.dp)
        )
      }

      // Search button
      IconButton(
        onClick = onNavigateToSearch,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .testTag("top_bar_search")
      ) {
        Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = "Internet Search",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(19.dp)
        )
      }
    }
  }
}
