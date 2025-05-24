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
        val model = DefaultTableModel(arrayOf("Name", "Tag", "Status", "Time", "Start Time", "Stop Time", "Actions"), 0)
        val table = JTable(model)
        val nameField = JBTextField(15)
        val tagField = JBTextField(10)
        val idleTimeoutField = JBTextField("5", 3)
        val longTaskField = JBTextField("30", 3)
        val addButton = JButton("Add")
        val startButton = JButton("Start")
        val pauseButton = JButton("Pause")
        val resumeButton = JButton("Resume")
        val stopButton = JButton("Stop")
        val exportCsvButton = JButton("Export CSV")
        val exportJsonButton = JButton("Export JSON")
        val editButton = JButton("Edit")
        val deleteButton = JButton("Delete")

        val mainPanel = JPanel().apply {
            layout = java.awt.BorderLayout()
            add(JBScrollPane(table), java.awt.BorderLayout.CENTER)

            // Task creation panel
            val createPanel = JPanel(java.awt.GridBagLayout())
            val gbc = java.awt.GridBagConstraints()
            gbc.insets = java.awt.Insets(2, 2, 2, 2)

            gbc.gridx = 0; gbc.gridy = 0
            createPanel.add(javax.swing.JLabel("Name:"), gbc)
            gbc.gridx = 1
            createPanel.add(nameField, gbc)

            gbc.gridx = 0; gbc.gridy = 1
            createPanel.add(javax.swing.JLabel("Tag:"), gbc)
            gbc.gridx = 1
            createPanel.add(tagField, gbc)

            gbc.gridx = 0; gbc.gridy = 2
            createPanel.add(javax.swing.JLabel("Idle Timeout (min):"), gbc)
            gbc.gridx = 1
            createPanel.add(idleTimeoutField, gbc)

            gbc.gridx = 0; gbc.gridy = 3
            createPanel.add(javax.swing.JLabel("Long Task Alert (min):"), gbc)
            gbc.gridx = 1
            createPanel.add(longTaskField, gbc)

            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2
            createPanel.add(addButton, gbc)

            // Action buttons panel
            val actionPanel = JPanel()
            actionPanel.add(startButton)
            actionPanel.add(pauseButton)
            actionPanel.add(resumeButton)
            actionPanel.add(stopButton)
            actionPanel.add(editButton)
            actionPanel.add(deleteButton)
            actionPanel.add(exportCsvButton)
            actionPanel.add(exportJsonButton)

            // Bottom panel containing both create and action panels
            val bottom = JPanel(java.awt.BorderLayout())
            bottom.add(createPanel, java.awt.BorderLayout.WEST)
            bottom.add(actionPanel, java.awt.BorderLayout.CENTER)

            add(bottom, java.awt.BorderLayout.SOUTH)
        }

        init {
            refresh()
            addButton.addActionListener {
                val name = nameField.text.trim()
                val tag = tagField.text.trim()
                val idleTimeout = idleTimeoutField.text.toLongOrNull()
                val longTask = longTaskField.text.toLongOrNull()

                if (name.isNotEmpty()) {
                    service.createTask(name, tag.takeIf { it.isNotEmpty() }, idleTimeout, longTask)
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

            editButton.addActionListener {
                selectedTask()?.let { task ->
                    // Populate fields with task data
                    nameField.text = task.name
                    tagField.text = task.tag ?: ""
                    idleTimeoutField.text = task.idleTimeoutMinutes?.toString() ?: "5"
                    longTaskField.text = task.longTaskMinutes?.toString() ?: "30"

                    // Change add button to update
                    val oldText = addButton.text
                    addButton.text = "Update"

                    // Store original action listener
                    val oldActionListeners = addButton.actionListeners
                    for (listener in oldActionListeners) {
                        addButton.removeActionListener(listener)
                    }

                    // Create a dedicated listener for the update action so it can be removed later
                    val updateListener = java.awt.event.ActionListener {
                        val name = nameField.text.trim()
                        val tag = tagField.text.trim()
                        val idleTimeout = idleTimeoutField.text.toLongOrNull()
                        val longTask = longTaskField.text.toLongOrNull()

                        if (name.isNotEmpty()) {
                            task.name = name
                            task.tag = tag.takeIf { it.isNotEmpty() }
                            task.idleTimeoutMinutes = idleTimeout
                            task.longTaskMinutes = longTask

                            // Reset UI
                            nameField.text = ""
                            tagField.text = ""
                            idleTimeoutField.text = "5"
                            longTaskField.text = "30"
                            addButton.text = oldText

                            // Restore original action listeners
                            addButton.removeActionListener(updateListener)
                            for (listener in oldActionListeners) {
                                addButton.addActionListener(listener)
                            }

                            refresh()
                        }
                    }

                    addButton.addActionListener(updateListener)
                }
            }

            deleteButton.addActionListener {
                selectedTask()?.let { task ->
                    val index = service.tasks.indexOf(task)
                    if (index >= 0) {
                        service.tasks.removeAt(index)
                        refresh()
                    }
                }
            }

            exportCsvButton.addActionListener {
                val fileChooser = javax.swing.JFileChooser()
                fileChooser.dialogTitle = "Export Tasks to CSV"
                fileChooser.fileSelectionMode = javax.swing.JFileChooser.FILES_ONLY
                fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv")

                if (fileChooser.showSaveDialog(mainPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    val filePath = if (!file.name.toLowerCase().endsWith(".csv")) {
                        java.io.File(file.absolutePath + ".csv")
                    } else {
                        file
                    }

                    val exporter = com.github.akhilesh170194.jbplugintasktimer.export.TaskExporter()
                    exporter.exportToCsv(service.tasks, filePath)
                }
            }

            exportJsonButton.addActionListener {
                val fileChooser = javax.swing.JFileChooser()
                fileChooser.dialogTitle = "Export Tasks to JSON"
                fileChooser.fileSelectionMode = javax.swing.JFileChooser.FILES_ONLY
                fileChooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter("JSON Files", "json")

                if (fileChooser.showSaveDialog(mainPanel) == javax.swing.JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    val filePath = if (!file.name.toLowerCase().endsWith(".json")) {
                        java.io.File(file.absolutePath + ".json")
                    } else {
                        file
                    }

                    val exporter = com.github.akhilesh170194.jbplugintasktimer.export.TaskExporter()
                    exporter.exportToJson(service.tasks, filePath)
                }
            }
        }

        private fun selectedTask() =
            service.tasks.getOrNull(table.selectedRow)

        private fun refresh() {
            model.setRowCount(0)
            service.tasks.forEach {
                model.addRow(arrayOf(
                    it.name, 
                    it.tag ?: "", 
                    it.status.name, 
                    formatDuration(it.runningTime),
                    it.startTime?.let { time -> formatDateTime(time) } ?: "",
                    it.stopTime?.let { time -> formatDateTime(time) } ?: "",
                    ""  // Actions column is handled by buttons below the table
                ))
            }
        }

        private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return dateTime.format(formatter)
        }

        private fun formatDuration(d: java.time.Duration): String {
            val h = d.toHours()
            val m = (d.toMinutes() % 60)
            val s = (d.seconds % 60)
            return String.format("%02d:%02d:%02d", h, m, s)
        }
    }
}
