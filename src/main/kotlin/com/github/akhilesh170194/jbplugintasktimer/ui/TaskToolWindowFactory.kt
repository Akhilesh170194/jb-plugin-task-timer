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
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.JTable
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.AbstractCellEditor

class TaskToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val service = project.service<TaskManagerService>()
        val panel = TaskToolWindow(service)
        val content = ContentFactory.getInstance().createContent(panel.mainPanel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    class TaskToolWindow(private val service: TaskManagerService) {
        private val columns = arrayOf(
            "Name", "Status", "Running Time", "Start Time", "Stop Time", "Tag", "Actions"
        )
        private val model = object : DefaultTableModel(columns, 0) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
        private val table = object : JBTable(model) {
            override fun isCellEditable(row: Int, column: Int): Boolean = column == 6
        }.apply {
            columnModel.getColumn(6).apply {
                val editorRenderer = ActionEditorRenderer()
                cellRenderer = editorRenderer
                cellEditor = editorRenderer
            }
            columnModel.getColumn(0).preferredWidth = 250
            columnModel.getColumn(5).preferredWidth = 25
            columnModel.getColumn(1).preferredWidth = 15
            columnModel.getColumn(2).preferredWidth = 25
        }
        private val statusLabel = JBLabel("Total Active Time: 00:00:00")
        val mainPanel: JPanel
        private val auditColumns = arrayOf("Time", "Task", "Action", "Details")
        private val auditModel = object : DefaultTableModel(auditColumns, 0) {
            override fun isCellEditable(row: Int, column: Int) = column == 6 || column == 0
        }
        private val auditTable = JBTable(auditModel)
        private val timer = javax.swing.Timer(1000) { refreshRunningTimes() }

        private val refreshTimer = javax.swing.Timer(1000) { refreshTable() }

        init {
            refreshTimer.start()

            val createButton = JButton("+ Create Task")
            createButton.addActionListener { openDialog(null) }
            val top = JPanel(BorderLayout())
            top.add(createButton, BorderLayout.WEST)

            val bottom = JPanel(BorderLayout())
            bottom.add(statusLabel, BorderLayout.EAST)

            val tabs = JBTabbedPane()
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
                model.addRow(
                    arrayOf(
                        task.name,
                        task.status,
                        formatDuration(task.runningTime),
                        task.startTime?.format(formatter) ?: "",
                        task.stopTime?.format(formatter) ?: "",
                        task.tag ?: "",
                        task
                    )
                )
            }
            updateTotalTime()
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

        private fun refreshAudit() {
            auditModel.rowCount = 0
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            service.auditLogs.forEach { log ->
                val task = service.tasks.find { it.id == log.taskId }?.name ?: log.taskId
                auditModel.addRow(
                    arrayOf(
                        log.time.format(formatter),
                        task,
                        log.action,
                        log.details
                    )
                )
            }
            updateTotalTime()
        }

        private fun formatDuration(d: Duration): String {
            val h = d.toHours()
            val m = d.toMinutes() % 60
            val s = d.seconds % 60
            return String.format("%02d:%02d:%02d", h, m, s)
        }

        inner class ActionEditorRenderer : AbstractCellEditor(), TableCellEditor, TableCellRenderer {
            private val panel = JPanel().apply { isOpaque = false }
            private val edit = JButton(com.intellij.icons.AllIcons.Actions.Edit).apply { toolTipText = "Edit Task" }
            private val delete = JButton(com.intellij.icons.AllIcons.General.Remove).apply { toolTipText = "Delete Task" }
            private val stop = JButton(com.intellij.icons.AllIcons.Actions.Suspend).apply { toolTipText = "Stop Task" }
            private val pause = JButton(com.intellij.icons.AllIcons.Actions.Pause).apply { toolTipText = "Pause Task" }
            private val resume = JButton(com.intellij.icons.AllIcons.Actions.Resume).apply { toolTipText = "Resume Task" }
            private var task: Task? = null

            init {
                panel.add(edit)
                panel.add(delete)
                panel.add(stop)
                panel.add(pause)
                panel.add(resume)

                edit.addActionListener {
                    table.cellEditor?.stopCellEditing()
                    task?.let { openDialog(it) }
                }
                delete.addActionListener {
                    table.cellEditor?.stopCellEditing()
                    task?.let {
                        service.deleteTask(it)
                        refreshTable()
                        refreshAudit()
                    }
                }
                stop.addActionListener {
                    table.cellEditor?.stopCellEditing()
                    task?.let { service.stopTask(it); refreshTable(); refreshAudit() }
                }
                pause.addActionListener {
                    table.cellEditor?.stopCellEditing()
                    task?.let { service.pauseTask(it); refreshTable(); refreshAudit() }
                }
                resume.addActionListener {
                    table.cellEditor?.stopCellEditing()
                    task?.let { service.resumeTask(it); refreshTable(); refreshAudit() }
                }
            }

            private fun configure(task: Task) {
                this.task = task
            }

            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): java.awt.Component {
                configure(value as Task)
                return panel
            }

            override fun getTableCellEditorComponent(
                table: JTable?, value: Any?, isSelected: Boolean, row: Int, column: Int
            ): java.awt.Component {
                configure(value as Task)
                return panel
            }

            override fun getCellEditorValue(): Any? = task
        }
    }
}
