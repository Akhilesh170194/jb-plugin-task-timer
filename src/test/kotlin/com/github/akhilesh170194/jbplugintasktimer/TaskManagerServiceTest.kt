package com.github.akhilesh170194.jbplugintasktimer

import com.github.akhilesh170194.jbplugintasktimer.export.TaskExporter
import com.github.akhilesh170194.jbplugintasktimer.model.AuditLogEntry
import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.github.akhilesh170194.jbplugintasktimer.settings.TaskTimerSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TaskManagerServiceTest {

    private val service: TaskManagerService = TaskManagerService()
    private val exporter: TaskExporter = TaskExporter(TaskTimerSettings().apply {
        state.timeZoneId = "UTC"
    })

    @Test
    fun testTaskLifecycle() {
        val task = service.createTask("sample", "tag", null, null)
        assertEquals("sample", task.name)
        assertEquals("tag", task.tag)
        assertEquals(TaskStatus.NOT_STARTED, task.status)

        service.startTask(task)
        assertEquals(TaskStatus.RUNNING, task.status)
        assertEquals(1, task.sessions.size)

        service.pauseTask(task)
        assertEquals(TaskStatus.PAUSED, task.status)
        assertEquals(1, task.sessions.size)

        service.resumeTask(task)
        assertEquals(TaskStatus.RUNNING, task.status)
        assertEquals(2, task.sessions.size)

        service.stopTask(task)
        assertEquals(TaskStatus.STOPPED, task.status)
        assertNotNull(task.stopTime)
        assertEquals(2, task.sessions.size)
        assertTrue(task.sessions.all { it.end != null })
    }

    @Test
    fun testExportFunctions() {
        val task = service.createTask("export", null, null, null)
        service.startTask(task)
        service.stopTask(task)

        val csv = java.nio.file.Files.createTempFile("tasks", ".csv").toFile()
        val json = java.nio.file.Files.createTempFile("tasks", ".json").toFile()

        exporter.exportToCsv(service.tasks, csv)
        exporter.exportToJson(service.tasks, json)

        assertTrue(csv.readLines().size > 1)
        val jsonContent = json.readText()
        assertTrue(jsonContent.startsWith("["))
    }

    @Test
    fun testTaskWithOverrides() {
        val idleTimeout = 10L
        val longTask = 60L

        val task = service.createTask("task with overrides", "test", idleTimeout, longTask)

        assertEquals("task with overrides", task.name)
        assertEquals("test", task.tag)
        assertEquals(idleTimeout, task.idleTimeoutMinutes)
        assertEquals(longTask, task.longTaskMinutes)
    }

    @Test
    fun testSerializationAndDeserializationOfTaskManagerServiceState() {

        val now: LocalDateTime = LocalDateTime.now();
        val auditLogEntry = AuditLogEntry(
            time = now,
            taskId = "123",
            action = "Created",
            details = "Task created successfully"
        )

        val state = TaskManagerService.State(
            auditLogs = mutableListOf(auditLogEntry)
        )

        // Serialize the state to JSON
        val json = Json.encodeToString(state)

        // Deserialize the JSON back to State
        val deserializedState = Json.decodeFromString<TaskManagerService.State>(json)

        // Verify the deserialized state
        assertEquals(1, deserializedState.auditLogs.size)
        assertEquals(now, deserializedState.auditLogs[0].time)
        assertEquals(auditLogEntry.taskId, deserializedState.auditLogs[0].taskId)
        assertEquals(auditLogEntry.action, deserializedState.auditLogs[0].action)
        assertEquals(auditLogEntry.details, deserializedState.auditLogs[0].details)
    }

    @Test
    fun testDeleteTask() {
        val task = service.createTask("toDelete", null, null, null)
        assertTrue(service.tasks.contains(task))

        service.deleteTask(task)

        assertFalse(service.tasks.contains(task))
        val log = service.auditLogs.last()
        assertEquals("Deleted", log.action)
        assertEquals(task.id, log.taskId)
    }
    
    fun testStatePersistsTasksAndAuditLogs() {
        val service = TaskManagerService()
        val task = service.createTask("persist", "tag", null, null)
        service.startTask(task)
        service.pauseTask(task)
        service.resumeTask(task)
        service.stopTask(task)

        val json = Json.encodeToString(service.state)
        val restored = Json.decodeFromString<TaskManagerService.State>(json)

        assertEquals(1, restored.tasks.size)
        assertEquals("persist", restored.tasks[0].name)
        assertTrue(restored.auditLogs.size >= 5)
    }

}
