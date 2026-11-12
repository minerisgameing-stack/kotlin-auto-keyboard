package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.AccessibilityStatusBanner
import com.example.ui.components.ActivityLogCard
import com.example.ui.components.SimulatedDiscordChat
import com.example.ui.components.TimerCard

@Composable
fun ControlScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isRunning by viewModel.isAutomationRunning.collectAsStateWithLifecycle()
    val countdown by viewModel.countdownRemainingSeconds.collectAsStateWithLifecycle()
    val totalDelay by viewModel.totalNextDelaySeconds.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val lastSent by viewModel.lastSentMessage.collectAsStateWithLifecycle()
    val totalSent by viewModel.totalSentSessionCount.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isServiceConnected by viewModel.isServiceConnected.collectAsStateWithLifecycle()
    val isSettingsEnabled by viewModel.isAccessibilitySettingEnabled.collectAsStateWithLifecycle()
    val simulatedMessages by viewModel.simulatedMessages.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("control_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Accessibility Service Status
        AccessibilityStatusBanner(
            isServiceConnected = isServiceConnected,
            isSettingsEnabled = isSettingsEnabled,
            onRefreshStatus = { viewModel.refreshServiceStatus() }
        )

        // Master Control & Random Timer Card
        TimerCard(
            isAutomationRunning = isRunning,
            countdownRemainingSeconds = countdown,
            totalNextDelaySeconds = totalDelay,
            settings = settings,
            lastSentMessage = lastSent,
            totalSentSessionCount = totalSent,
            onToggleAutomation = { enable -> viewModel.toggleAutomation(enable) },
            onSendSingleNow = { viewModel.triggerSingleSendNow() }
        )

        // Simulated Discord Chat Feed
        SimulatedDiscordChat(
            messages = simulatedMessages,
            onClearChat = { viewModel.clearSimulatedChat() }
        )

        // Live Diagnostic and Engine Activity Log
        ActivityLogCard(
            logs = logs,
            onClearLogs = { viewModel.clearLogs() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
