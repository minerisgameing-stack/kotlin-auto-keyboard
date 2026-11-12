package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TimeUnit
import com.example.ui.MainViewModel
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordGreen

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentSettings by viewModel.settings.collectAsStateWithLifecycle()

    var minTime by remember { mutableIntStateOf(currentSettings.minTime) }
    var maxTime by remember { mutableIntStateOf(currentSettings.maxTime) }
    var selectedUnit by remember { mutableStateOf(currentSettings.timeUnit) }
    var targetPackage by remember { mutableStateOf(currentSettings.targetPackage) }
    var autoLaunch by remember { mutableStateOf(currentSettings.autoLaunchApp) }
    var sendEnter by remember { mutableStateOf(currentSettings.sendEnterKey) }
    var simulationMode by remember { mutableStateOf(currentSettings.simulationMode) }

    var savedBannerVisible by remember { mutableStateOf(false) }

    // Synchronize if settings change externally
    LaunchedEffect(currentSettings) {
        minTime = currentSettings.minTime
        maxTime = currentSettings.maxTime
        selectedUnit = currentSettings.timeUnit
        targetPackage = currentSettings.targetPackage
        autoLaunch = currentSettings.autoLaunchApp
        sendEnter = currentSettings.sendEnterKey
        simulationMode = currentSettings.simulationMode
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saved Banner
        AnimatedVisibility(visible = savedBannerVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DiscordGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .border(1.dp, DiscordGreen, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = DiscordGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings saved to SharedPreferences successfully!",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = DiscordGreen
                )
            }
        }

        // Section 1: Random Timer Range
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = DiscordBlurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dynamic Random Timer Range",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A random delay between the minimum and maximum bounds will be picked after each message dispatch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Unit Selector (Seconds / Minutes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeUnit.values().forEach { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = unit },
                            label = { Text(unit.label) },
                            leadingIcon = if (selectedUnit == unit) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DiscordBlurple,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("unit_chip_${unit.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Minimum Time Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minimum Time: $minTime ${selectedUnit.label.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedTextField(
                        value = minTime.toString(),
                        onValueChange = { str ->
                            str.toIntOrNull()?.let {
                                val valid = it.coerceAtLeast(1)
                                minTime = valid
                                if (maxTime < valid) maxTime = valid
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .height(48.dp)
                            .testTag("min_time_input_field"),
                        singleLine = true
                    )
                }

                val maxSliderVal = if (selectedUnit == TimeUnit.SECONDS) 120f else 60f
                Slider(
                    value = minTime.toFloat().coerceIn(1f, maxSliderVal),
                    onValueChange = {
                        val intVal = it.toInt().coerceAtLeast(1)
                        minTime = intVal
                        if (maxTime < intVal) maxTime = intVal
                    },
                    valueRange = 1f..maxSliderVal,
                    modifier = Modifier.testTag("min_time_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Maximum Time Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Maximum Time: $maxTime ${selectedUnit.label.lowercase()}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    OutlinedTextField(
                        value = maxTime.toString(),
                        onValueChange = { str ->
                            str.toIntOrNull()?.let {
                                val valid = it.coerceAtLeast(minTime)
                                maxTime = valid
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(80.dp)
                            .height(48.dp)
                            .testTag("max_time_input_field"),
                        singleLine = true
                    )
                }

                Slider(
                    value = maxTime.toFloat().coerceIn(1f, maxSliderVal),
                    onValueChange = {
                        val intVal = it.toInt().coerceAtLeast(minTime)
                        maxTime = intVal
                    },
                    valueRange = 1f..maxSliderVal,
                    modifier = Modifier.testTag("max_time_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip("5-15s") {
                        minTime = 5
                        maxTime = 15
                        selectedUnit = TimeUnit.SECONDS
                    }
                    PresetChip("15-45s") {
                        minTime = 15
                        maxTime = 45
                        selectedUnit = TimeUnit.SECONDS
                    }
                    PresetChip("1-3m") {
                        minTime = 1
                        maxTime = 3
                        selectedUnit = TimeUnit.MINUTES
                    }
                    PresetChip("5-15m") {
                        minTime = 5
                        maxTime = 15
                        selectedUnit = TimeUnit.MINUTES
                    }
                }
            }
        }

        // Section 2: Automation Target & Behaviors
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = DiscordBlurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Target App & Automation Rules",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Package Name
                OutlinedTextField(
                    value = targetPackage,
                    onValueChange = { targetPackage = it },
                    label = { Text("Target App Package") },
                    placeholder = { Text("com.discord") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_package_input_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Auto Launch Discord Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-launch App on Trigger",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Brings Discord to foreground if not open when the timer elapses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoLaunch,
                        onCheckedChange = { autoLaunch = it },
                        modifier = Modifier.testTag("auto_launch_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Simulate Enter / Send Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulate Enter / Send",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Automatically clicks the Discord Send button immediately after typing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = sendEnter,
                        onCheckedChange = { sendEnter = it },
                        modifier = Modifier.testTag("send_enter_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // In-App Simulation Sandbox Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "In-App Simulation Sandbox",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Simulate dispatches in the app's chat feed (ideal for testing timer without switching apps)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = simulationMode,
                        onCheckedChange = { simulationMode = it },
                        modifier = Modifier.testTag("simulation_mode_switch")
                    )
                }
            }
        }

        // Save & Reset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveSettings(
                        minTime = minTime,
                        maxTime = maxTime,
                        timeUnit = selectedUnit,
                        targetPackage = targetPackage.trim(),
                        autoLaunchApp = autoLaunch,
                        sendEnterKey = sendEnter,
                        simulationMode = simulationMode
                    )
                    savedBannerVisible = true
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Settings", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    minTime = 10
                    maxTime = 30
                    selectedUnit = TimeUnit.SECONDS
                    targetPackage = "com.discord"
                    autoLaunch = true
                    sendEnter = true
                    simulationMode = false
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reset_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
