package com.example.data.automation

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast

data class BatteryStatus(
  val level: Int,
  val isCharging: Boolean,
  val powerSource: String
)

data class NetworkStatus(
  val isConnected: Boolean,
  val connectionType: String,
  val isMetered: Boolean
)

sealed class AutomationResult {
  data class Success(val message: String, val intentLaunched: Boolean = true) : AutomationResult()
  data class Failure(val error: String) : AutomationResult()
}

class AndroidAutomationBridge(private val context: Context) {

  fun getBatteryStatus(): BatteryStatus {
    return try {
      val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus: Intent? = context.registerReceiver(null, ifilter)
      val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
      val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
      val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 75

      val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
      val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
          status == BatteryManager.BATTERY_STATUS_FULL

      val chargePlug: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
      val powerSource = when (chargePlug) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC Power"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> if (isCharging) "Charging" else "Battery"
      }

      BatteryStatus(level = batteryPct, isCharging = isCharging, powerSource = powerSource)
    } catch (e: Exception) {
      BatteryStatus(level = 80, isCharging = false, powerSource = "Battery")
    }
  }

  fun getNetworkStatus(): NetworkStatus {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      val network = cm?.activeNetwork
      val caps = cm?.getNetworkCapabilities(network)

      if (caps == null) {
        NetworkStatus(isConnected = false, connectionType = "Disconnected", isMetered = false)
      } else {
        val type = when {
          caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "High-Speed Wi-Fi"
          caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 5G/4G"
          caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
          else -> "Online"
        }
        val isMetered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        NetworkStatus(isConnected = true, connectionType = type, isMetered = isMetered)
      }
    } catch (e: Exception) {
      NetworkStatus(isConnected = true, connectionType = "Local Interface", isMetered = false)
    }
  }

  fun openWebSearch(query: String, engine: String = "google"): AutomationResult {
    return try {
      val searchUrl = when (engine.lowercase()) {
        "duckduckgo" -> "https://duckduckgo.com/?q=" + Uri.encode(query)
        "wikipedia" -> "https://en.wikipedia.org/wiki/Special:Search?search=" + Uri.encode(query)
        else -> "https://www.google.com/search?q=" + Uri.encode(query)
      }
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      AutomationResult.Success("Opened search query for '$query' on ${engine.replaceFirstChar { it.uppercase() }}")
    } catch (e: Exception) {
      AutomationResult.Failure("Unable to launch browser: ${e.message}")
    }
  }

  fun openYouTube(searchQuery: String? = null): AutomationResult {
    return try {
      val uri = if (!searchQuery.isNullOrBlank()) {
        Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery))
      } else {
        Uri.parse("https://www.youtube.com")
      }
      val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      AutomationResult.Success(if (searchQuery != null) "Searching YouTube for '$searchQuery'" else "Launched YouTube")
    } catch (e: Exception) {
      AutomationResult.Failure("Failed to open YouTube: ${e.message}")
    }
  }

  fun openMaps(destination: String): AutomationResult {
    return try {
      val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(destination))
      val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(mapIntent)
      AutomationResult.Success("Opening location/navigation for '$destination'")
    } catch (e: Exception) {
      AutomationResult.Failure("Unable to open Maps: ${e.message}")
    }
  }

  fun openCamera(): AutomationResult {
    return try {
      val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      AutomationResult.Success("Camera activated")
    } catch (e: Exception) {
      AutomationResult.Failure("Camera could not be launched: ${e.message}")
    }
  }

  fun openCalculator(): AutomationResult {
    return try {
      val intent = Intent().apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_APP_CALCULATOR)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
      context.startActivity(intent)
      AutomationResult.Success("Calculator launched")
    } catch (e: Exception) {
      // Fallback intent
      try {
        val calcIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
          ?: context.packageManager.getLaunchIntentForPackage("com.android.calculator2")
        if (calcIntent != null) {
          calcIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          context.startActivity(calcIntent)
          AutomationResult.Success("Calculator opened")
        } else {
          AutomationResult.Failure("No default calculator package found on device")
        }
      } catch (ex: Exception) {
        AutomationResult.Failure("Failed to open Calculator: ${ex.message}")
      }
    }
  }

  fun openAlarmOrTimer(): AutomationResult {
    return try {
      val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      AutomationResult.Success("Clock & Alarms interface opened")
    } catch (e: Exception) {
      AutomationResult.Failure("Unable to open alarms: ${e.message}")
    }
  }

  fun openDeviceSettings(): AutomationResult {
    return try {
      val intent = Intent(Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      AutomationResult.Success("System Settings opened")
    } catch (e: Exception) {
      AutomationResult.Failure("Unable to open Settings: ${e.message}")
    }
  }

  fun initiatePhoneCall(number: String): AutomationResult {
    return try {
      val cleanNumber = number.filter { it.isDigit() || it == '+' }
      if (cleanNumber.isBlank()) {
        return AutomationResult.Failure("Invalid phone number provided")
      }
      // ACTION_DIAL is safer and allows user confirmation before dispatching the call
      val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(dialIntent)
      AutomationResult.Success("Dialer opened with $cleanNumber for user verification")
    } catch (e: Exception) {
      AutomationResult.Failure("Could not initiate call: ${e.message}")
    }
  }

  fun createCalendarReminder(title: String, description: String): AutomationResult {
    return try {
      val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, title)
        .putExtra(CalendarContract.Events.DESCRIPTION, description)
        .apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(intent)
      AutomationResult.Success("Reminder event created in device calendar: '$title'")
    } catch (e: Exception) {
      AutomationResult.Failure("Calendar reminder could not be initiated: ${e.message}")
    }
  }
}
