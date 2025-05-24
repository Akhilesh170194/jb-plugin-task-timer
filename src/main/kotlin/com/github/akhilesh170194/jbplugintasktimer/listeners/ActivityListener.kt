package com.github.akhilesh170194.jbplugintasktimer.listeners

import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.github.akhilesh170194.jbplugintasktimer.settings.TaskTimerSettings
import com.intellij.ide.IdeEventQueue
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.util.*

/**
 * Listens for IDE activity and automatically pauses/resumes tasks.
 */
class ActivityListener {
    private val settings = service<TaskTimerSettings>()
    private var lastActivityTime = System.currentTimeMillis()
    private var isIdle = false
    private var timer: Timer? = null

    init {
        startActivityMonitoring()
    }

    private fun startActivityMonitoring() {
        // Listen for IDE events to detect activity
        IdeEventQueue.getInstance().addActivityListener({
            lastActivityTime = System.currentTimeMillis()
            if (isIdle) {
                isIdle = false
                resumeAllTasks()
            }
        }, ApplicationManager.getApplication())

        // Check for idle state periodically
        timer = Timer(true)
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkIdleState()
            }
        }, 10000, 10000) // Check every 10 seconds
    }

    private fun checkIdleState() {
        val idleThresholdMs = settings.state.idleTimeoutMinutes * 60 * 1000
        val currentTime = System.currentTimeMillis()
        val idleTime = currentTime - lastActivityTime

        if (idleTime >= idleThresholdMs && !isIdle) {
            isIdle = true
            pauseAllTasks()
        }
    }

    private fun pauseAllTasks() {
        for (project in ProjectManager.getInstance().openProjects) {
            val taskManager = project.service<TaskManagerService>()
            val runningTasks = taskManager.tasks.filter { it.status == TaskStatus.RUNNING }

            if (runningTasks.isNotEmpty()) {
                for (task in runningTasks) {
                    taskManager.pauseTask(task)
                }

                // Show notification
                showNotification(project, "Tasks auto-paused due to inactivity", NotificationType.INFORMATION)
            }
        }
    }

    private fun resumeAllTasks() {
        for (project in ProjectManager.getInstance().openProjects) {
            val taskManager = project.service<TaskManagerService>()
            val pausedTasks = taskManager.tasks.filter { it.status == TaskStatus.PAUSED }

            if (pausedTasks.isNotEmpty()) {
                for (task in pausedTasks) {
                    taskManager.resumeTask(task)
                }

                // Show notification
                showNotification(project, "Tasks auto-resumed due to activity", NotificationType.INFORMATION)
            }
        }
    }

    private fun showNotification(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Task Timer Notifications")
            .createNotification(content, type)
            .notify(project)
    }

    fun dispose() {
        timer?.cancel()
        timer = null
    }

    companion object {
        private var instance: ActivityListener? = null

        fun getInstance(): ActivityListener {
            if (instance == null) {
                instance = ActivityListener()
            }
            return instance!!
        }
    }
}