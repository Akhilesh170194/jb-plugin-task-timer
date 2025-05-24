package com.github.akhilesh170194.jbplugintasktimer.model

import com.github.akhilesh170194.jbplugintasktimer.serialization.LocalDateTimeConverter
import com.intellij.util.xmlb.annotations.OptionTag
import java.time.LocalDateTime

data class TaskSession(
    @OptionTag(converter = LocalDateTimeConverter::class)
    var start: LocalDateTime = LocalDateTime.now(),
    @OptionTag(converter = LocalDateTimeConverter::class)
    var end: LocalDateTime? = null
)
