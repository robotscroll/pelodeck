package com.pelodeck.diagnostics

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
}

enum class LogLevel {
    DEBUG, INFO, WARN, ERROR
}

object DiagnosticLogger {
    private const val MAX_LOGS = 250
    private val logsList = mutableListOf<DiagnosticLogEntry>()
    private val _logsFlow = MutableStateFlow<List<DiagnosticLogEntry>>(emptyList())
    val logsFlow: Flow<List<DiagnosticLogEntry>> = _logsFlow.asStateFlow()

    @Synchronized
    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = DiagnosticLogEntry(tag = tag, message = message, level = level)
        if (logsList.size >= MAX_LOGS) {
            logsList.removeAt(0)
        }
        logsList.add(entry)
        _logsFlow.value = logsList.toList()
    }

    fun d(tag: String, message: String) = log(tag, message, LogLevel.DEBUG)
    fun i(tag: String, message: String) = log(tag, message, LogLevel.INFO)
    fun w(tag: String, message: String) = log(tag, message, LogLevel.WARN)
    fun e(tag: String, message: String) = log(tag, message, LogLevel.ERROR)

    @Synchronized
    fun clear() {
        logsList.clear()
        _logsFlow.value = emptyList()
    }

    @Synchronized
    fun dumpText(): String {
        return logsList.joinToString("\n") { "[${it.formattedTime}] [${it.level}] [${it.tag}] ${it.message}" }
    }
}
