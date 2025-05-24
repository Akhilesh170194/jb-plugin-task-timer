package com.github.akhilesh170194.jbplugintasktimer.model

import java.time.Duration
import java.time.LocalDateTime

/**
 * Represents a single task with time tracking information.
 */
data class Task(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var tag: String? = null,
    var created: LocalDateTime = LocalDateTime.now(),
    var status: TaskStatus = TaskStatus.STOPPED,
    var runningTime: Duration = Duration.ZERO,
    var startTime: LocalDateTime? = null,
    var stopTime: LocalDateTime? = null,
    var idleTimeoutMinutes: Long? = null,
    var longTaskMinutes: Long? = null,
    val sessions: MutableList<TaskSession> = mutableListOf()
)
