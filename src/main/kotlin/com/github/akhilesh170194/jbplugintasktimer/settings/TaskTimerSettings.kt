package com.github.akhilesh170194.jbplugintasktimer.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.time.ZoneId

/**
 * Global settings for the Task Timer plugin.
 */
@Service
@State(name = "TaskTimerSettings", storages = [Storage("taskTimerSettings.xml")])
class TaskTimerSettings : PersistentStateComponent<TaskTimerSettings.State> {

    data class State(
        var idleTimeoutMinutes: Long = 5,
        var longTaskMinutes: Long = 30,
        var timeZoneId: String = ZoneId.systemDefault().id
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }
}
