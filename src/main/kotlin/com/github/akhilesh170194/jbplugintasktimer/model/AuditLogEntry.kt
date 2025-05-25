package com.github.akhilesh170194.jbplugintasktimer.model

import com.github.akhilesh170194.jbplugintasktimer.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Represents a single audit log entry for task changes.
 */
@Serializable
data class AuditLogEntry(
    @Serializable(with = LocalDateTimeSerializer::class)
    var time: LocalDateTime = LocalDateTime.now(),
    var taskId: String = "",
    var action: String = "",
    var details: String = ""
)
