package com.github.akhilesh170194.jbplugintasktimer.serialization

import com.intellij.util.xmlb.Converter
import java.time.LocalDateTime

/**
 * XML serialization converter for [LocalDateTime].
 */
class LocalDateTimeConverter : Converter<LocalDateTime>() {
    override fun fromString(value: String): LocalDateTime = LocalDateTime.parse(value)

    override fun toString(value: LocalDateTime): String = value.toString()
}
