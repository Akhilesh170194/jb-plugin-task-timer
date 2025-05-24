package com.github.akhilesh170194.jbplugintasktimer.serialization

import com.intellij.util.xmlb.Converter
import java.time.Duration

/**
 * XML serialization converter for [Duration].
 */
class DurationConverter : Converter<Duration>() {
    override fun fromString(value: String): Duration = Duration.parse(value)

    override fun toString(value: Duration): String = value.toString()
}
