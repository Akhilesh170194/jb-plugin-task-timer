package com.github.akhilesh170194.jbplugintasktimer.ui

import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

/**
 * Tool window showing the list of tasks.
 */
class TaskToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<TaskManagerService>()
        val panel = TaskToolWindow(service)
        val content = ContentFactory.getInstance().createContent(panel.mainPanel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    class TaskToolWindow(private val service: TaskManagerService) {
        val model = DefaultTableModel(arrayOf("Name", "Tag", "Status", "Time"), 0)
        val table = JTable(model)
        val nameField = JBTextField()
        val tagField = JBTextField()
        val addButton = JButton("Add")
        val startButton = JButton("Start")
        val pauseButton = JButton("Pause")
        val resumeButton = JButton("Resume")
        val stopButton = JButton("Stop")

        val mainPanel = JPanel().apply {
            layout = java.awt.BorderLayout()
            add(JBScrollPane(table), java.awt.BorderLayout.CENTER)
            val bottom = JPanel()
            bottom.add(nameField)
            bottom.add(tagField)
            bottom.add(addButton)
            bottom.add(startButton)
            bottom.add(pauseButton)
            bottom.add(resumeButton)
            bottom.add(stopButton)
            add(bottom, java.awt.BorderLayout.SOUTH)
        }

        init {
            refresh()
            addButton.addActionListener {
                val name = nameField.text.trim()
                val tag = tagField.text.trim()
                if (name.isNotEmpty()) {
                    service.createTask(name, tag.takeIf { it.isNotEmpty() }, null, null)
                    nameField.text = ""
                    tagField.text = ""
                    refresh()
                }
            }
            startButton.addActionListener {
                selectedTask()?.let {
                    service.startTask(it)
                    refresh()
                }
            }
            pauseButton.addActionListener {
                selectedTask()?.let {
                    service.pauseTask(it)
                    refresh()
                }
            }
            resumeButton.addActionListener {
                selectedTask()?.let {
                    service.resumeTask(it)
                    refresh()
                }
            }
            stopButton.addActionListener {
                selectedTask()?.let {
                    service.stopTask(it)
                    refresh()
                }
            }
        }

        private fun selectedTask() =
            service.tasks.getOrNull(table.selectedRow)

        private fun refresh() {
            model.setRowCount(0)
            service.tasks.forEach {
                model.addRow(arrayOf(it.name, it.tag ?: "", it.status.name, formatDuration(it.runningTime)))
            }
        }

        private fun formatDuration(d: java.time.Duration): String {
            val h = d.toHours()
            val m = (d.toMinutes() % 60)
            val s = (d.seconds % 60)
            return String.format("%02d:%02d:%02d", h, m, s)
        }
    }
}
