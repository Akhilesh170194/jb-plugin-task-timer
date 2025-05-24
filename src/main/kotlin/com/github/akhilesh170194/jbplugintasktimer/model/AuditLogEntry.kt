package com.github.akhilesh170194.jbplugintasktimer.model

import java.time.LocalDateTime

/**
 * Represents a single audit log entry for task changes.
 */
data class AuditLogEntry(
    var time: LocalDateTime = LocalDateTime.now(),
    var taskId: String = "",
    var action: String = "",
    var details: String = ""
)
