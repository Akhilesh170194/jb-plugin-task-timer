package com.github.akhilesh170194.jbplugintasktimer.services

import com.github.akhilesh170194.jbplugintasktimer.model.Task
import com.github.akhilesh170194.jbplugintasktimer.model.TaskSession
import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import java.time.Duration
import java.time.LocalDateTime

/**
 * Service storing tasks for a project.
 */
@Service(Service.Level.PROJECT)
@State(name = "TaskManagerService", storages = [Storage("taskManager.xml")])
class TaskManagerService(private val project: Project) : PersistentStateComponent<TaskManagerService.State> {

    data class State(
        var tasks: MutableList<Task> = mutableListOf()
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    val tasks: MutableList<Task>
        get() = myState.tasks

    fun createTask(name: String, tag: String?, idle: Long?, longTask: Long?): Task {
        val task = Task(name = name, tag = tag, idleTimeoutMinutes = idle, longTaskMinutes = longTask)
        tasks.add(task)
        return task
    }

    fun startTask(task: Task) {
        if (task.status == TaskStatus.RUNNING) return
        task.startTime = LocalDateTime.now()
        task.status = TaskStatus.RUNNING
        task.sessions.add(TaskSession(start = task.startTime!!))
    }

    fun pauseTask(task: Task) {
        if (task.status != TaskStatus.RUNNING) return
        val now = LocalDateTime.now()
        val session = task.sessions.lastOrNull()
        session?.end = now
        task.runningTime = task.runningTime.plus(Duration.between(task.startTime, now))
        task.status = TaskStatus.PAUSED
    }

    fun resumeTask(task: Task) {
        if (task.status != TaskStatus.PAUSED) return
        task.startTime = LocalDateTime.now()
        task.status = TaskStatus.RUNNING
        task.sessions.add(TaskSession(start = task.startTime!!))
    }

    fun stopTask(task: Task) {
        if (task.status == TaskStatus.STOPPED) return
        val now = LocalDateTime.now()
        if (task.status == TaskStatus.RUNNING) {
            task.runningTime = task.runningTime.plus(Duration.between(task.startTime, now))
            task.sessions.lastOrNull()?.end = now
        }
        task.stopTime = now
        task.status = TaskStatus.STOPPED
    }
}
