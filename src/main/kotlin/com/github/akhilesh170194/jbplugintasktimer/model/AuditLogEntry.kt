package com.github.akhilesh170194.jbplugintasktimer.model

import com.github.akhilesh170194.jbplugintasktimer.serialization.LocalDateTimeConverter
import com.intellij.util.xmlb.annotations.OptionTag
import java.time.LocalDateTime

/**
 * Represents a single audit log entry for task changes.
 */
data class AuditLogEntry(
    @OptionTag(converter = LocalDateTimeConverter::class)
    var time: LocalDateTime = LocalDateTime.now(),
    var taskId: String = "",
    var action: String = "",
    var details: String = ""
)
