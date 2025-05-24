package com.github.akhilesh170194.jbplugintasktimer.ui

import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import java.awt.event.MouseEvent
import java.time.Duration
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import javax.swing.Icon

/**
 * Status bar widget showing total running time of active tasks.
 */
class TaskTimerStatusBarWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = "TaskTimerWidget"

    override fun getDisplayName(): String = "Task Timer"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = TaskTimerStatusBarWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) {
        (widget as? TaskTimerStatusBarWidget)?.dispose()
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class TaskTimerStatusBarWidget(private val project: Project) : StatusBarWidget {
    private var statusBar: StatusBar? = null
    private var timer: Timer? = null

    override fun ID(): String = "TaskTimerWidget"

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        startTimer()
    }

    override fun dispose() {
        timer?.cancel()
        timer = null
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return object : StatusBarWidget.TextPresentation {
            override fun getText(): String {
                val service = project.service<TaskManagerService>()
                val totalTime = calculateTotalRunningTime(service)
                return formatDuration(totalTime)
            }

            override fun getTooltipText(): String = "Total time of running tasks"

            override fun getClickConsumer() = null

            override fun getAlignment(): Float = 0.0f
        }
    }

    private fun startTimer() {
        timer = Timer(true)
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                statusBar?.updateWidget(ID())
            }
        }, 0, 1000) // Update every second
    }

    private fun calculateTotalRunningTime(service: TaskManagerService): Duration {
        var totalTime = Duration.ZERO

        for (task in service.tasks) {
            if (task.status == TaskStatus.RUNNING) {
                // Add the stored running time
                totalTime = totalTime.plus(task.runningTime)

                // Add the time since the task was started
                val now = LocalDateTime.now()
                if (task.startTime != null) {
                    totalTime = totalTime.plus(Duration.between(task.startTime, now))
                }
            } else {
                // For non-running tasks, just add the stored running time
                totalTime = totalTime.plus(task.runningTime)
            }
        }

        return totalTime
    }

    private fun formatDuration(d: Duration): String {
        val h = d.toHours()
        val m = (d.toMinutes() % 60)
        val s = (d.seconds % 60)
        return String.format("Tasks: %02d:%02d:%02d", h, m, s)
    }
}
