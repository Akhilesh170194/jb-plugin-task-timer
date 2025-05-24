package com.github.akhilesh170194.jbplugintasktimer.ui

import com.github.akhilesh170194.jbplugintasktimer.model.Task
import com.github.akhilesh170194.jbplugintasktimer.model.TaskStatus
import com.github.akhilesh170194.jbplugintasktimer.services.TaskManagerService
import com.github.akhilesh170194.jbplugintasktimer.ui.dialog.TaskDialog
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer

class TaskToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<TaskManagerService>()
        val panel = TaskToolWindow(service)
        val content = ContentFactory.getInstance().createContent(panel.mainPanel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    class TaskToolWindow(private val service: TaskManagerService) {
        private val columns = arrayOf(
            "Name", "Status", "Running Time", "Start Time", "Stop Time", "Pause Count", "Resume Count", "Tag", "Actions")
        private val model = object : DefaultTableModel(columns, 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
        private val table = JBTable(model)
        private val statusLabel = JBLabel("Total Active Time: 00:00:00")
        val mainPanel: JPanel
        private val auditColumns = arrayOf("Time", "Task", "Action", "Details")
        private val auditModel = object : DefaultTableModel(auditColumns, 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
        private val auditTable = JBTable(auditModel)
        private val timer = javax.swing.Timer(1000) { refreshRunningTimes() }

        init {
            table.columnModel.getColumn(8).cellRenderer = ActionRenderer()
            table.rowHeight = 28

            val createButton = JButton("+ Create Task")
            createButton.addActionListener { openDialog(null) }
            val top = JPanel(BorderLayout())
            top.add(createButton, BorderLayout.WEST)

            val bottom = JPanel(BorderLayout())
            bottom.add(statusLabel, BorderLayout.EAST)

            val tabs = JTabbedPane()
            tabs.addTab("Tasks", JBScrollPane(table))
            tabs.addTab("Audit Logs", JBScrollPane(auditTable))

            mainPanel = JPanel(BorderLayout())
            mainPanel.add(top, BorderLayout.NORTH)
            mainPanel.add(tabs, BorderLayout.CENTER)
            mainPanel.add(bottom, BorderLayout.SOUTH)

            refreshTable()
            refreshAudit()
            timer.start()
        }

        private fun openDialog(task: Task?) {
            val dialog = TaskDialog(task) { name, tag, idle, longTask ->
                if (task == null) {
                    service.createTask(name, tag, idle, longTask)
                } else {
                    service.updateTask(task, name, tag, idle, longTask)
                }
                refreshTable()
                refreshAudit()
            }
            dialog.showAndGet()
        }

        private fun refreshTable() {
            model.rowCount = 0
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            service.tasks.forEach { task ->
                model.addRow(arrayOf(
                    task.name,
                    task.status.name,
                    formatDuration(task.runningTime),
                    task.startTime?.format(formatter) ?: "",
                    task.stopTime?.format(formatter) ?: "",
                    task.pauseCount,
                    task.resumeCount,
                    task.tag ?: "",
                    task
                ))
            }
            updateTotalTime()
        }

        private fun refreshAudit() {
            auditModel.rowCount = 0
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            service.auditLogs.forEach { log ->
                val taskName = service.tasks.find { it.id == log.taskId }?.name ?: log.taskId
                auditModel.addRow(arrayOf(
                    log.time.format(formatter),
                    taskName,
                    log.action,
                    log.details
                ))
            }
        }

        private fun refreshRunningTimes() {
            var row = 0
            service.tasks.forEach { task ->
                if (task.status == TaskStatus.RUNNING && task.startTime != null) {
                    val running = task.runningTime.plus(Duration.between(task.startTime, LocalDateTime.now()))
                    model.setValueAt(formatDuration(running), row, 2)
                } else {
                    model.setValueAt(formatDuration(task.runningTime), row, 2)
                }
                row++
            }
            updateTotalTime()
        }

        private fun updateTotalTime() {
            var total = Duration.ZERO
            service.tasks.forEach { task ->
                var duration = task.runningTime
                if (task.status == TaskStatus.RUNNING && task.startTime != null) {
                    duration = duration.plus(Duration.between(task.startTime, LocalDateTime.now()))
                }
                total = total.plus(duration)
            }
            statusLabel.text = "Total Active Time: ${formatDuration(total)}"
        }

        private fun formatDuration(d: Duration): String {
            val h = d.toHours()
            val m = d.toMinutes() % 60
            val s = d.seconds % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }

        inner class ActionRenderer : TableCellRenderer {
            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): java.awt.Component {
                val task = value as Task
                val panel = JPanel()
                panel.isOpaque = false
                val edit = JButton(com.intellij.icons.AllIcons.Actions.Edit)
                edit.toolTipText = "Edit Task"
                edit.addActionListener { openDialog(task) }
                val delete = JButton(com.intellij.icons.AllIcons.General.Remove)
                delete.toolTipText = "Delete Task"
                delete.addActionListener {
                    service.tasks.remove(task)
                    refreshTable()
                    refreshAudit()
                }
                val stop = JButton(com.intellij.icons.AllIcons.Actions.Suspend)
                stop.toolTipText = "Stop Task"
                stop.addActionListener { service.stopTask(task); refreshTable(); refreshAudit() }
                val pause = JButton(com.intellij.icons.AllIcons.Actions.Pause)
                pause.toolTipText = "Pause Task"
                pause.addActionListener { service.pauseTask(task); refreshTable(); refreshAudit() }
                val resume = JButton(com.intellij.icons.AllIcons.Actions.Resume)
                resume.toolTipText = "Resume Task"
                resume.addActionListener { service.resumeTask(task); refreshTable(); refreshAudit() }
                panel.add(edit)
                panel.add(delete)
                panel.add(stop)
                panel.add(pause)
                panel.add(resume)
                return panel
            }
        }
    }
}
