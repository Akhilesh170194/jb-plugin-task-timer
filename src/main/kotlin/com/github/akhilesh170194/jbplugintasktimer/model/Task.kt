package com.github.akhilesh170194.jbplugintasktimer.model

import com.github.akhilesh170194.jbplugintasktimer.serialization.DurationSerializer
import com.github.akhilesh170194.jbplugintasktimer.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime

/**
 * Represents a single task with time tracking information.
 */
@Serializable
data class Task(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var tag: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    var created: LocalDateTime = LocalDateTime.now(),
    var status: TaskStatus? = null,
    @Serializable(with = DurationSerializer::class)
    var runningTime: Duration = Duration.ZERO,
    @Serializable(with = LocalDateTimeSerializer::class)
    var startTime: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    var stopTime: LocalDateTime? = null,
    var idleTimeoutMinutes: Long? = null,
    var longTaskMinutes: Long? = null,
    var pauseCount: Int = 0,
    var resumeCount: Int = 0,
    val sessions: MutableList<TaskSession> = mutableListOf()
)
