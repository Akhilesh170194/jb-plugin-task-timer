package com.github.akhilesh170194.jbplugintasktimer.model

import com.github.akhilesh170194.jbplugintasktimer.serialization.DurationConverter
import com.github.akhilesh170194.jbplugintasktimer.serialization.LocalDateTimeConverter
import com.intellij.util.xmlb.annotations.OptionTag
import java.time.Duration
import java.time.LocalDateTime

/**
 * Represents a single task with time tracking information.
 */
data class Task(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var tag: String? = null,
    @OptionTag(converter = LocalDateTimeConverter::class)
    var created: LocalDateTime = LocalDateTime.now(),
    var status: TaskStatus = TaskStatus.STOPPED,
    @OptionTag(converter = DurationConverter::class)
    var runningTime: Duration = Duration.ZERO,
    @OptionTag(converter = LocalDateTimeConverter::class)
    var startTime: LocalDateTime? = null,
    @OptionTag(converter = LocalDateTimeConverter::class)
    var stopTime: LocalDateTime? = null,
    var idleTimeoutMinutes: Long? = null,
    var longTaskMinutes: Long? = null,
    var pauseCount: Int = 0,
    var resumeCount: Int = 0,
    val sessions: MutableList<TaskSession> = mutableListOf()
)
