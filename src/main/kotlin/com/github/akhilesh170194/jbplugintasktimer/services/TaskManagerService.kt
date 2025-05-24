package com.github.akhilesh170194.jbplugintasktimer.services

import com.github.akhilesh170194.jbplugintasktimer.model.Task
import com.github.akhilesh170194.jbplugintasktimer.model.TaskSession
import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Path
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

    /**
     * Export the current list of tasks to a CSV file.
     */
    fun exportToCsv(path: Path) {
        path.toFile().bufferedWriter().use { writer ->
            writer.appendLine("id,name,tag,status,runningTime,startTime,stopTime,pauseResumeCount")
            tasks.forEach { task ->
                writer.appendLine(
                    listOf(
                        task.id,
                        task.name.replace(',', ' '),
                        task.tag ?: "",
                        task.status.name,
                        task.runningTime.toMillis().toString(),
                        task.startTime?.toString() ?: "",
                        task.stopTime?.toString() ?: "",
                        (task.sessions.size - 1).toString()
                    ).joinToString(",")
                )
            }
        }
    }

    /**
     * Export the current list of tasks to a JSON file.
     */
    fun exportToJson(path: Path) {
        path.toFile().bufferedWriter().use { writer ->
            val escaped = tasks.joinToString(",", prefix = "[", postfix = "]") { task ->
                buildString {
                    append("{")
                    append("\"id\":\"").append(task.id).append("\",")
                    append("\"name\":\"").append(task.name.replace("\"", "'" )).append("\",")
                    append("\"tag\":\"").append(task.tag ?: "").append("\",")
                    append("\"status\":\"").append(task.status.name).append("\",")
                    append("\"runningTime\":").append(task.runningTime.toMillis()).append(",")
                    append("\"startTime\":\"").append(task.startTime?.toString() ?: "").append("\",")
                    append("\"stopTime\":\"").append(task.stopTime?.toString() ?: "").append("\",")
                    append("\"pauseResumeCount\":").append(task.sessions.size - 1)
                    append("}")
                }
            }
            writer.append(escaped)
        }
    }
}
