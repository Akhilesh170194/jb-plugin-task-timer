package com.github.akhilesh170194.jbplugintasktimer.startup

import com.github.akhilesh170194.jbplugintasktimer.listeners.ActivityListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Initializes the plugin components on startup.
 */
class TaskTimerStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Initialize the activity listener if it's not already initialized
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }

        // Initialize the activity listener
        ActivityListener.getInstance()
    }
}
