package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AutomationSettings
import com.example.data.MessageEntity
import com.example.data.MessageRepository
import com.example.data.SettingsPreferences
import com.example.data.TimeUnit
import com.example.service.AutomationCoordinator
import com.example.service.AutomationLogManager
import com.example.service.DiscordAutomationService
import com.example.service.LogEntry
import com.example.service.SimulatedMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val messageRepository = MessageRepository(database.messageDao())
    val settingsPreferences = SettingsPreferences.getInstance(application)
    val automationCoordinator = AutomationCoordinator.getInstance(
        application,
        messageRepository,
        settingsPreferences
    )

    val allMessages: StateFlow<List<MessageEntity>> = messageRepository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messageCount: StateFlow<Int> = messageRepository.messageCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val settings: StateFlow<AutomationSettings> = settingsPreferences.settingsFlow

    val isAutomationRunning: StateFlow<Boolean> = automationCoordinator.isAutomationRunning
    val countdownRemainingSeconds: StateFlow<Long> = automationCoordinator.countdownRemainingSeconds
    val totalNextDelaySeconds: StateFlow<Long> = automationCoordinator.totalNextDelaySeconds
    val lastSentMessage: StateFlow<String?> = automationCoordinator.lastSentMessage
    val lastSentTimestamp: StateFlow<Long?> = automationCoordinator.lastSentTimestamp
    val totalSentSessionCount: StateFlow<Int> = automationCoordinator.totalSentSessionCount
    val simulatedMessages: StateFlow<List<SimulatedMessage>> = automationCoordinator.simulatedMessages

    val logs: StateFlow<List<LogEntry>> = AutomationLogManager.logs
    val isServiceConnected: StateFlow<Boolean> = DiscordAutomationService.isServiceActive

    private val _isAccessibilitySettingEnabled = MutableStateFlow(false)
    val isAccessibilitySettingEnabled: StateFlow<Boolean> = _isAccessibilitySettingEnabled.asStateFlow()

    init {
        refreshServiceStatus()
        // If first launch and DB is empty, let's load a few helpful initial samples
        viewModelScope.launch {
            if (database.messageDao().getRandomMessage() == null) {
                loadSampleMessages()
            }
        }
    }

    fun refreshServiceStatus() {
        val enabled = DiscordAutomationService.isAccessibilitySettingsEnabled(getApplication())
        _isAccessibilitySettingEnabled.value = enabled
    }

    fun addMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            messageRepository.insertMessage(text)
            AutomationLogManager.info("Added new message to Room database: \"${text.take(35)}\"")
        }
    }

    fun updateMessage(id: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            messageRepository.updateMessage(id, text)
            AutomationLogManager.info("Updated message #$id: \"${text.take(35)}\"")
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            messageRepository.deleteMessage(id)
            AutomationLogManager.info("Deleted message #$id from Room database")
        }
    }

    fun deleteAllMessages() {
        viewModelScope.launch {
            messageRepository.deleteAllMessages()
            AutomationLogManager.warning("Cleared all messages from Room database")
        }
    }

    fun loadSampleMessages() {
        viewModelScope.launch {
            val samples = listOf(
                "Hey everyone! Hope you're having an awesome day!",
                "Anyone up for a raid or game session tonight?",
                "Quick check-in! Don't forget to stay hydrated 💧",
                "GG well played on the last match!",
                "What's everyone working on today?",
                "Sharing good vibes in the server ✨",
                "Automated check-in from AutoSender 🚀"
            )
            messageRepository.insertMessages(samples)
            AutomationLogManager.success("Loaded ${samples.size} sample messages into Room database")
        }
    }

    fun saveSettings(
        minTime: Int,
        maxTime: Int,
        timeUnit: TimeUnit,
        targetPackage: String,
        autoLaunchApp: Boolean,
        sendEnterKey: Boolean,
        simulationMode: Boolean
    ) {
        settingsPreferences.saveSettings(
            minTime = minTime,
            maxTime = maxTime,
            timeUnit = timeUnit,
            targetPackage = targetPackage,
            autoLaunchApp = autoLaunchApp,
            sendEnterKey = sendEnterKey,
            simulationMode = simulationMode
        )
        AutomationLogManager.info(
            "Settings saved: $minTime - $maxTime ${timeUnit.label.lowercase()} (Target: $targetPackage)"
        )
    }

    fun setSimulationMode(enabled: Boolean) {
        settingsPreferences.setSimulationMode(enabled)
        AutomationLogManager.info("Simulation Sandbox Mode set to $enabled")
    }

    fun toggleAutomation(enable: Boolean) {
        refreshServiceStatus()
        automationCoordinator.toggleAutomation(enable)
    }

    fun triggerSingleSendNow() {
        viewModelScope.launch {
            AutomationLogManager.info("Manual test dispatch triggered")
            automationCoordinator.triggerSingleDispatchNow()
        }
    }

    fun clearLogs() {
        AutomationLogManager.clear()
    }

    fun clearSimulatedChat() {
        automationCoordinator.clearSimulatedMessages()
    }
}
