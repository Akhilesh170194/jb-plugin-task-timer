package com.github.akhilesh170194.jbplugintasktimer.settings

import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.time.ZoneId

/**
 * Global settings for the Task Timer plugin.
 */
@Service
@State(name = "TaskTimerSettings", storages = [Storage("taskTimerSettings.xml")])
class TaskTimerSettings : SerializablePersistentStateComponent<TaskTimerSettings.State>(State()) {

    data class State(
        var idleTimeoutMinutes: Long = 5,
        var longTaskMinutes: Long = 30,
        var timeZoneId: String = ZoneId.systemDefault().id
    )
}
