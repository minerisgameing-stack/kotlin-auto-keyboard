package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

enum class TimeUnit(val label: String, val multiplierSeconds: Long) {
    SECONDS("Seconds", 1L),
    MINUTES("Minutes", 60L)
}

data class AutomationSettings(
    val minTime: Int = 10,
    val maxTime: Int = 30,
    val timeUnit: TimeUnit = TimeUnit.SECONDS,
    val targetPackage: String = "com.discord",
    val autoLaunchApp: Boolean = true,
    val sendEnterKey: Boolean = true,
    val simulationMode: Boolean = false
) {
    val minSeconds: Long
        get() = (minTime.coerceAtLeast(1) * timeUnit.multiplierSeconds)

    val maxSeconds: Long
        get() = (maxTime.coerceAtLeast(minTime.coerceAtLeast(1)) * timeUnit.multiplierSeconds)

    fun calculateRandomDelaySeconds(): Long {
        val minSec = minSeconds
        val maxSec = maxSeconds
        return if (minSec >= maxSec) {
            minSec
        } else {
            Random.nextLong(minSec, maxSec + 1)
        }
    }
}

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("autosender_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AutomationSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings(): AutomationSettings {
        val minTime = prefs.getInt(KEY_MIN_TIME, 10)
        val maxTime = prefs.getInt(KEY_MAX_TIME, 30)
        val unitName = prefs.getString(KEY_TIME_UNIT, TimeUnit.SECONDS.name) ?: TimeUnit.SECONDS.name
        val unit = try {
            TimeUnit.valueOf(unitName)
        } catch (_: Exception) {
            TimeUnit.SECONDS
        }
        val targetPackage = prefs.getString(KEY_TARGET_PACKAGE, "com.discord") ?: "com.discord"
        val autoLaunch = prefs.getBoolean(KEY_AUTO_LAUNCH, true)
        val sendEnter = prefs.getBoolean(KEY_SEND_ENTER, true)
        val simulation = prefs.getBoolean(KEY_SIMULATION_MODE, false)

        return AutomationSettings(
            minTime = minTime,
            maxTime = maxTime,
            timeUnit = unit,
            targetPackage = targetPackage,
            autoLaunchApp = autoLaunch,
            sendEnterKey = sendEnter,
            simulationMode = simulation
        )
    }

    fun saveSettings(
        minTime: Int,
        maxTime: Int,
        timeUnit: TimeUnit,
        targetPackage: String = "com.discord",
        autoLaunchApp: Boolean = true,
        sendEnterKey: Boolean = true,
        simulationMode: Boolean = false
    ) {
        val safeMin = minTime.coerceAtLeast(1)
        val safeMax = maxTime.coerceAtLeast(safeMin)

        prefs.edit()
            .putInt(KEY_MIN_TIME, safeMin)
            .putInt(KEY_MAX_TIME, safeMax)
            .putString(KEY_TIME_UNIT, timeUnit.name)
            .putString(KEY_TARGET_PACKAGE, targetPackage)
            .putBoolean(KEY_AUTO_LAUNCH, autoLaunchApp)
            .putBoolean(KEY_SEND_ENTER, sendEnterKey)
            .putBoolean(KEY_SIMULATION_MODE, simulationMode)
            .apply()

        _settingsFlow.value = loadSettings()
    }

    fun setSimulationMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIMULATION_MODE, enabled).apply()
        _settingsFlow.value = loadSettings()
    }

    companion object {
        private const val KEY_MIN_TIME = "min_time"
        private const val KEY_MAX_TIME = "max_time"
        private const val KEY_TIME_UNIT = "time_unit"
        private const val KEY_TARGET_PACKAGE = "target_package"
        private const val KEY_AUTO_LAUNCH = "auto_launch_app"
        private const val KEY_SEND_ENTER = "send_enter_key"
        private const val KEY_SIMULATION_MODE = "simulation_mode"

        @Volatile
        private var INSTANCE: SettingsPreferences? = null

        fun getInstance(context: Context): SettingsPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
