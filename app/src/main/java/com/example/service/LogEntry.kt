package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

object AutomationLogManager {
    private const val MAX_LOGS = 150
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun log(level: LogLevel, message: String) {
        val entry = LogEntry(level = level, message = message)
        val updated = (_logs.value + entry).takeLast(MAX_LOGS)
        _logs.value = updated
    }

    fun info(message: String) = log(LogLevel.INFO, message)
    fun success(message: String) = log(LogLevel.SUCCESS, message)
    fun warning(message: String) = log(LogLevel.WARNING, message)
    fun error(message: String) = log(LogLevel.ERROR, message)

    fun clear() {
        _logs.value = emptyList()
    }
}
