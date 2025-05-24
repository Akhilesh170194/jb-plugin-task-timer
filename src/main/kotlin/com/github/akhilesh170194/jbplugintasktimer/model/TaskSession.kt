package com.github.akhilesh170194.jbplugintasktimer.model

import java.time.LocalDateTime

data class TaskSession(
    var start: LocalDateTime = LocalDateTime.now(),
    var end: LocalDateTime? = null
)
