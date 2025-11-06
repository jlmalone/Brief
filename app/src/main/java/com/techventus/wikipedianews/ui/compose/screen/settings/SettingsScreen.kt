package com.techventus.wikipedianews.ui.compose.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.techventus.wikipedianews.work.NewsWorkScheduler

/**
 * Settings screen with user preferences.
 *
 * Features:
 * - Dark theme toggle
 * - Background sync settings
 * - Notification preferences
 * - Cache management
 * - Reset settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsSwitchItem(
                    title = "Dark Theme",
                    subtitle = "Use dark theme throughout the app",
                    checked = preferences.isDarkTheme,
                    onCheckedChange = viewModel::toggleDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Background Sync Section
            SettingsSection(title = "Background Sync") {
                SettingsSwitchItem(
                    title = "Enable Background Sync",
                    subtitle = "Automatically fetch news in the background",
                    checked = preferences.isBackgroundSyncEnabled,
                    onCheckedChange = viewModel::toggleBackgroundSync
                )

                if (preferences.isBackgroundSyncEnabled) {
                    HorizontalDivider()
                    SyncIntervalSelector(
                        selectedInterval = preferences.syncIntervalHours,
                        onIntervalSelected = viewModel::updateSyncInterval
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    title = "Enable Notifications",
                    subtitle = "Get notified when new news is available",
                    checked = preferences.areNotificationsEnabled,
                    onCheckedChange = viewModel::toggleNotifications
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Management Section
            SettingsSection(title = "Data Management") {
                SettingsActionItem(
                    title = "Clear Cache",
                    subtitle = "Remove all cached news articles",
                    icon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear cache",
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { showClearCacheDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Section
            SettingsSection(title = "Advanced") {
                SettingsActionItem(
                    title = "Reset Settings",
                    subtitle = "Restore all settings to defaults",
                    onClick = { showResetDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Info
            Text(
                text = "Brief v2.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Dialogs
    if (showClearCacheDialog) {
        ConfirmationDialog(
            title = "Clear Cache?",
            message = "This will remove all cached news articles. You'll need an internet connection to view news after clearing.",
            onConfirm = {
                viewModel.clearCache()
                showClearCacheDialog = false
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    if (showResetDialog) {
        ConfirmationDialog(
            title = "Reset Settings?",
            message = "This will restore all settings to their default values.",
            onConfirm = {
                viewModel.resetSettings()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsActionItem(
    title: String,
    subtitle: String,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        icon?.invoke()
    }
}

@Composable
private fun SyncIntervalSelector(
    selectedInterval: Long,
    onIntervalSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Sync Interval",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NewsWorkScheduler.SYNC_INTERVAL_OPTIONS.forEach { interval ->
            val intervalText = when (interval) {
                1L -> "1 hour"
                24L -> "24 hours (daily)"
                else -> "$interval hours"
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntervalSelected(interval) }
                    .padding(vertical = 4.dp),
                color = if (selectedInterval == interval) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = intervalText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
