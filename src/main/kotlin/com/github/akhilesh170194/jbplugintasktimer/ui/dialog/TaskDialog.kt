package com.github.akhilesh170194.jbplugintasktimer.ui.dialog

import com.github.akhilesh170194.jbplugintasktimer.model.Task
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * Dialog for creating or editing a task.
 */
class TaskDialog(
    private val task: Task?,
    private val onOk: (name: String, tag: String?, idle: Long?, longTask: Long?) -> Unit
) : DialogWrapper(true) {
    private val nameField = JBTextField(task?.name ?: "")
    private val tagField = JBTextField(task?.tag ?: "")
    private val overrideBox = JBCheckBox("Override Global Settings")
    private val idleField = JSpinner(SpinnerNumberModel(task?.idleTimeoutMinutes?.toInt() ?: 5, 1, 10080, 1))
    private val longTaskField = JSpinner(SpinnerNumberModel(task?.longTaskMinutes?.toInt() ?: 30, 1, 10080, 1))

    init {
        title = if (task == null) "Create Task" else "Edit Task"
        setOKButtonText(if (task == null) "Create" else "Save")
        init()
        overrideBox.isSelected = task?.idleTimeoutMinutes != null || task?.longTaskMinutes != null
        toggleFields()
        overrideBox.addActionListener { toggleFields() }
    }

    private fun toggleFields() {
        idleField.isEnabled = overrideBox.isSelected
        longTaskField.isEnabled = overrideBox.isSelected
    }

    override fun createCenterPanel(): JComponent {
        val content = panel {
            row("Task Name:") {
                cell(nameField).resizableColumn().align(Align.FILL)
            }
            row("Tag:") {
                cell(tagField).resizableColumn().align(Align.FILL)
            }
            row {
                cell(overrideBox)
            }
            row("Idle Timeout (min):") {
                cell(idleField)
            }
            row("Long Task Alert (min):") {
                cell(longTaskField)
            }
        }

        // Wrap a DSL panel in a JPanel and set the preferred size
        return javax.swing.JPanel().apply {
            layout = java.awt.BorderLayout()
            preferredSize = java.awt.Dimension(450, 280) // Set your desired size here
            add(content, java.awt.BorderLayout.CENTER)
        }
    }

    override fun doOKAction() {
        val name = nameField.text.trim()
        if (name.isEmpty()) return
        val tag = tagField.text.trim().takeIf { it.isNotEmpty() }
        val idle = if (overrideBox.isSelected) (idleField.value as Number).toLong() else null
        val longTask = if (overrideBox.isSelected) (longTaskField.value as Number).toLong() else null
        onOk(name, tag, idle, longTask)
        super.doOKAction()
    }
}
