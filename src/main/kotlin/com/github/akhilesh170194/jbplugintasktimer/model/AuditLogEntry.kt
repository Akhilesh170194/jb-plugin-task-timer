package com.github.akhilesh170194.jbplugintasktimer.model

import java.time.LocalDateTime

/**
 * Represents an audit log entry recording a task action.
 */
data class AuditLogEntry(
    var taskId: String = "",
    var action: String = "",
    var time: LocalDateTime = LocalDateTime.now(),
    var details: String = ""
)
