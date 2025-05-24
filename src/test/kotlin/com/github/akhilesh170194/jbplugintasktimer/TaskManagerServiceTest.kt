package com.github.akhilesh170194.jbplugintasktimer

import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TaskManagerServiceTest : BasePlatformTestCase() {
    fun testTaskLifecycle() {
        val service = project.service<TaskManagerService>()
        val task = service.createTask("sample", "tag", null, null)
        assertEquals("sample", task.name)
        assertEquals("tag", task.tag)
        assertEquals(TaskStatus.STOPPED, task.status)

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

    fun testExportFunctions() {
        val service = project.service<TaskManagerService>()
        val task = service.createTask("export", null, null, null)
        service.startTask(task)
        service.stopTask(task)

        val csv = java.nio.file.Files.createTempFile("tasks", ".csv").toFile()
        val json = java.nio.file.Files.createTempFile("tasks", ".json").toFile()

        val exporter = com.github.akhilesh170194.jbplugintasktimer.export.TaskExporter()
        exporter.exportToCsv(service.tasks, csv)
        exporter.exportToJson(service.tasks, json)

        assertTrue(csv.readLines().size > 1)
        val jsonContent = json.readText()
        assertTrue(jsonContent.startsWith("["))
    }

    fun testTaskWithOverrides() {
        val service = project.service<TaskManagerService>()
        val idleTimeout = 10L
        val longTask = 60L

        val task = service.createTask("task with overrides", "test", idleTimeout, longTask)

        assertEquals("task with overrides", task.name)
        assertEquals("test", task.tag)
        assertEquals(idleTimeout, task.idleTimeoutMinutes)
        assertEquals(longTask, task.longTaskMinutes)
    }
}
