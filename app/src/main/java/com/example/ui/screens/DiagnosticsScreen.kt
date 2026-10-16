package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val diagnostics by viewModel.diagnostics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshDiagnostics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System & Scheduler Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDiagnostics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("diagnostics_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Live Execution Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // WorkManager and Timing Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DiagRow(label = "Automation State", value = if (diagnostics?.isAutomationEnabled == true) "ENABLED" else "PAUSED", isHighlight = true)
                        DiagRow(label = "Configured Interval", value = diagnostics?.scheduleIntervalName ?: "N/A")
                        DiagRow(label = "Target Screen", value = diagnostics?.targetScreen ?: "Home Screen")
                        DiagRow(label = "Next Eligible Execution", value = diagnostics?.nextEligibleExecution ?: "N/A")
                        DiagRow(label = "Last Successful Change", value = diagnostics?.lastSuccessfulChange ?: "None")
                        DiagRow(label = "Last Failed Attempt", value = diagnostics?.lastFailedChange ?: "None", isError = diagnostics?.lastFailedChange != "None")
                    }
                }
            }

            item {
                Text(
                    text = "Device & Storage Telemetry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Storage and Device Health Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DiagRow(label = "Cached Wallpapers", value = "${diagnostics?.cachedWallpapersCount ?: 0} ready offline")
                        DiagRow(label = "Favorite Wallpapers", value = "${diagnostics?.favoritesCount ?: 0}")
                        DiagRow(label = "App Storage Used", value = "${String.format("%.2f", diagnostics?.storageUsedMb ?: 0.0)} MB")
                        DiagRow(label = "Internet Available", value = if (diagnostics?.isNetworkAvailable == true) "YES (Connected)" else "NO (Offline)")
                        DiagRow(label = "Wi-Fi Connected", value = if (diagnostics?.isWifiConnected == true) "YES" else "NO")
                        DiagRow(label = "Battery Level", value = "${diagnostics?.batteryPercent ?: 0}% (${if (diagnostics?.isCharging == true) "Charging" else "Unplugged"})")
                        DiagRow(label = "System Battery Saver", value = if (diagnostics?.isBatterySaverActive == true) "ACTIVE" else "Off")
                        DiagRow(label = "Notifications Permitted", value = if (diagnostics?.isNotificationPermissionGranted == true) "GRANTED" else "NOT GRANTED")
                    }
                }
            }

            item {
                Text(
                    text = "AI Quota & Cost Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // AI Status Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DiagRow(label = "Remaining Daily AI Quota", value = "${diagnostics?.aiQuotaRemaining ?: 0} generations")
                        DiagRow(label = "API Key Storage", value = "Secure Client Environment / Secrets")
                        DiagRow(label = "Offline Fallback", value = "Active (Rotates local cached artworks)")
                    }
                }
            }

            // Recent Error Logs if any
            val errors = diagnostics?.recentErrors ?: emptyList()
            if (errors.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Operation Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            errors.take(5).forEach { err ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String, isHighlight: Boolean = false, isError: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = when {
                isError -> MaterialTheme.colorScheme.error
                isHighlight -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
