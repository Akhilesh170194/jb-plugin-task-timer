package com.github.akhilesh170194.jbplugintasktimer.export

import com.github.akhilesh170194.jbplugintasktimer.model.Task
import com.github.akhilesh170194.jbplugintasktimer.settings.TaskTimerSettings
import com.intellij.openapi.components.service
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Exports task data to various formats.
 */
class TaskExporter {
    private val settings = service<TaskTimerSettings>()
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of(settings.state.timeZoneId))

    /**
     * Exports tasks to CSV format.
     */
    fun exportToCsv(tasks: List<Task>, file: File) {
        val csvContent = buildString {
            // Header
            appendLine("Name,Tag,Status,Running Time,Start Time,Stop Time,Sessions")

            // Data rows
            tasks.forEach { task ->
                append(escapeForCsv(task.name)).append(",")
                append(escapeForCsv(task.tag ?: "")).append(",")
                append(task.status.name).append(",")
                append(formatDuration(task.runningTime)).append(",")
                append(task.startTime?.let { dateTimeFormatter.format(it) } ?: "").append(",")
                append(task.stopTime?.let { dateTimeFormatter.format(it) } ?: "").append(",")
                append(task.sessions.size)
                appendLine()
            }
        }

        file.writeText(csvContent)
    }

    /**
     * Exports tasks to JSON format.
     */
    fun exportToJson(tasks: List<Task>, file: File) {
        val jsonContent = buildString {
            append("[\n")
            tasks.forEachIndexed { index, task ->
                append("  {\n")
                append("    \"name\": \"${escapeForJson(task.name)}\",\n")
                append("    \"tag\": ${task.tag?.let { "\"${escapeForJson(it)}\"" } ?: "null"},\n")
                append("    \"status\": \"${task.status.name}\",\n")
                append("    \"runningTime\": \"${formatDuration(task.runningTime)}\",\n")
                append("    \"startTime\": ${if (task.startTime != null) "\"${dateTimeFormatter.format(task.startTime)}\"" else "null"},\n")
                append("    \"stopTime\": ${if (task.stopTime != null) "\"${dateTimeFormatter.format(task.stopTime)}\"" else "null"},\n")
                append("    \"sessions\": ${task.sessions.size}\n")
                append("  }")
                if (index < tasks.size - 1) {
                    append(",")
                }
                append("\n")
            }
            append("]\n")
        }

        file.writeText(jsonContent)
    }

    private fun escapeForCsv(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun escapeForJson(value: String): String {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
    }

    private fun formatDuration(d: java.time.Duration): String {
        val h = d.toHours()
        val m = (d.toMinutes() % 60)
        val s = (d.seconds % 60)
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}
