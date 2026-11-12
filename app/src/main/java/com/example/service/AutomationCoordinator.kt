package com.example.service

import android.content.Context
import com.example.data.AutomationSettings
import com.example.data.MessageEntity
import com.example.data.MessageRepository
import com.example.data.SettingsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SimulatedMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAutoSent: Boolean = true
)

class AutomationCoordinator(
    private val context: Context,
    private val messageRepository: MessageRepository,
    private val settingsPreferences: SettingsPreferences
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var automationJob: Job? = null

    private val _isAutomationRunning = MutableStateFlow(false)
    val isAutomationRunning: StateFlow<Boolean> = _isAutomationRunning.asStateFlow()

    private val _countdownRemainingSeconds = MutableStateFlow(0L)
    val countdownRemainingSeconds: StateFlow<Long> = _countdownRemainingSeconds.asStateFlow()

    private val _totalNextDelaySeconds = MutableStateFlow(0L)
    val totalNextDelaySeconds: StateFlow<Long> = _totalNextDelaySeconds.asStateFlow()

    private val _lastSentMessage = MutableStateFlow<String?>(null)
    val lastSentMessage: StateFlow<String?> = _lastSentMessage.asStateFlow()

    private val _lastSentTimestamp = MutableStateFlow<Long?>(null)
    val lastSentTimestamp: StateFlow<Long?> = _lastSentTimestamp.asStateFlow()

    private val _totalSentSessionCount = MutableStateFlow(0)
    val totalSentSessionCount: StateFlow<Int> = _totalSentSessionCount.asStateFlow()

    private val _simulatedMessages = MutableStateFlow<List<SimulatedMessage>>(emptyList())
    val simulatedMessages: StateFlow<List<SimulatedMessage>> = _simulatedMessages.asStateFlow()

    fun toggleAutomation(enable: Boolean) {
        if (enable) {
            startAutomation()
        } else {
            stopAutomation()
        }
    }

    fun startAutomation() {
        if (_isAutomationRunning.value) return

        _isAutomationRunning.value = true
        AutomationLogManager.info("Automation loop initiated")

        automationJob?.cancel()
        automationJob = scope.launch {
            runLoop()
        }
    }

    fun stopAutomation() {
        _isAutomationRunning.value = false
        automationJob?.cancel()
        automationJob = null
        _countdownRemainingSeconds.value = 0L
        _totalNextDelaySeconds.value = 0L
        AutomationLogManager.info("Automation loop stopped by user")
    }

    private suspend fun runLoop() {
        while (_isAutomationRunning.value) {
            val settings = settingsPreferences.settingsFlow.value
            val delaySeconds = settings.calculateRandomDelaySeconds()

            _totalNextDelaySeconds.value = delaySeconds
            _countdownRemainingSeconds.value = delaySeconds

            AutomationLogManager.info(
                "Random timer scheduled: next dispatch in ${delaySeconds}s " +
                        "(Range: ${settings.minTime}-${settings.maxTime} ${settings.timeUnit.label.lowercase()})"
            )

            // Countdown tick loop
            for (remaining in delaySeconds downTo 1) {
                if (!_isAutomationRunning.value) break
                _countdownRemainingSeconds.value = remaining
                delay(1000)
            }

            if (!_isAutomationRunning.value) break
            _countdownRemainingSeconds.value = 0

            // Trigger dispatch
            dispatchNextRandomMessage(settings)
        }
    }

    suspend fun triggerSingleDispatchNow() {
        val settings = settingsPreferences.settingsFlow.value
        dispatchNextRandomMessage(settings)
    }

    private suspend fun dispatchNextRandomMessage(settings: AutomationSettings) {
        val randomMessage = messageRepository.getRandomMessage()
        if (randomMessage == null) {
            AutomationLogManager.warning(
                "Cannot dispatch: Room database has 0 messages. Please add messages in the Messages tab."
            )
            return
        }

        AutomationLogManager.info("Selected random message: \"${randomMessage.text}\" (ID: ${randomMessage.id})")

        if (settings.simulationMode) {
            // Simulation Sandbox Mode
            handleSimulationSend(randomMessage)
        } else {
            // Real Discord Accessibility Dispatch
            handleRealDiscordSend(randomMessage, settings)
        }
    }

    private suspend fun handleSimulationSend(message: MessageEntity) {
        delay(300) // Simulate typing delay
        val simulated = SimulatedMessage(text = message.text)
        _simulatedMessages.value = (_simulatedMessages.value + simulated).takeLast(50)

        _lastSentMessage.value = message.text
        _lastSentTimestamp.value = System.currentTimeMillis()
        _totalSentSessionCount.value += 1

        messageRepository.recordMessageSent(message.id)
        AutomationLogManager.success("[Sandbox] Simulated send successful: \"${message.text}\"")
    }

    private suspend fun handleRealDiscordSend(message: MessageEntity, settings: AutomationSettings) {
        val service = DiscordAutomationService.instance

        if (service == null) {
            val isEnabledInSettings = DiscordAutomationService.isAccessibilitySettingsEnabled(context)
            if (!isEnabledInSettings) {
                AutomationLogManager.error(
                    "Accessibility Service is not enabled! Enable AutoSender in Android Accessibility Settings."
                )
            } else {
                AutomationLogManager.warning(
                    "Accessibility Service is enabled in system settings but currently inactive. Launch Discord or restart the service."
                )
            }

            // In case user is testing on emulator without real Discord, also record to sandbox
            val simulated = SimulatedMessage(text = message.text)
            _simulatedMessages.value = (_simulatedMessages.value + simulated).takeLast(50)
            return
        }

        // Auto launch Discord if configured
        if (settings.autoLaunchApp) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(settings.targetPackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    AutomationLogManager.info("Bringing Discord to foreground...")
                    delay(1200) // Allow window rendering
                }
            } catch (e: Exception) {
                AutomationLogManager.warning("Could not auto-launch ${settings.targetPackage}: ${e.message}")
            }
        }

        // Inject and send
        val result = service.injectMessageAndSend(
            text = message.text,
            targetPackage = settings.targetPackage,
            sendEnter = settings.sendEnterKey
        )

        if (result.success) {
            _lastSentMessage.value = message.text
            _lastSentTimestamp.value = System.currentTimeMillis()
            _totalSentSessionCount.value += 1
            messageRepository.recordMessageSent(message.id)

            val simulated = SimulatedMessage(text = message.text)
            _simulatedMessages.value = (_simulatedMessages.value + simulated).takeLast(50)
        } else {
            AutomationLogManager.error("Dispatch failed: ${result.message}")
        }
    }

    fun clearSimulatedMessages() {
        _simulatedMessages.value = emptyList()
    }

    companion object {
        @Volatile
        private var INSTANCE: AutomationCoordinator? = null

        fun getInstance(
            context: Context,
            messageRepository: MessageRepository,
            settingsPreferences: SettingsPreferences
        ): AutomationCoordinator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AutomationCoordinator(
                    context.applicationContext,
                    messageRepository,
                    settingsPreferences
                ).also { INSTANCE = it }
            }
        }
    }
}
