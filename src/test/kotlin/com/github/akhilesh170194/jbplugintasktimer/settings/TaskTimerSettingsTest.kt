package com.github.akhilesh170194.jbplugintasktimer.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TaskTimerSettingsTest {

    @Test
    fun testDefaultStateValues() {
        val settings = TaskTimerSettings()
        val state = settings.state

        assertEquals(5, state.idleTimeoutMinutes, "Default idleTimeoutMinutes should be 5")
        assertEquals(30, state.longTaskMinutes, "Default longTaskMinutes should be 30")
        assertEquals(
            java.time.ZoneId.systemDefault().id,
            state.timeZoneId,
            "Default timeZoneId should match system default"
        )
    }

    @Test
    fun testLoadStateUpdatesStateCorrectly() {
        val settings = TaskTimerSettings()
        val newState = TaskTimerSettings.State(
            idleTimeoutMinutes = 10,
            longTaskMinutes = 60,
            timeZoneId = "UTC"
        )

        settings.loadState(newState)
        val state = settings.state

        assertEquals(10, state.idleTimeoutMinutes, "idleTimeoutMinutes should be updated to 10")
        assertEquals(60, state.longTaskMinutes, "longTaskMinutes should be updated to 60")
        assertEquals("UTC", state.timeZoneId, "timeZoneId should be updated to UTC")
    }

}