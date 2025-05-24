package com.github.akhilesh170194.jbplugintasktimer.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.time.ZoneId
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Configurable for Task Timer global settings.
 */
class TaskTimerSettingsConfigurable : Configurable {
    private val settings = service<TaskTimerSettings>()
    private var idleTimeoutField: JBTextField? = null
    private var longTaskField: JBTextField? = null
    private var timeZoneComboBox: JComboBox<String>? = null

    override fun getDisplayName(): String = "Task Timer"

    override fun createComponent(): JComponent {
        idleTimeoutField = JBTextField(settings.state.idleTimeoutMinutes.toString())
        longTaskField = JBTextField(settings.state.longTaskMinutes.toString())
        
        // Create timezone dropdown
        val availableZones = ZoneId.getAvailableZoneIds().sorted()
        timeZoneComboBox = JComboBox(availableZones.toTypedArray())
        timeZoneComboBox?.selectedItem = settings.state.timeZoneId
        
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Default idle timeout (minutes):", idleTimeoutField!!)
            .addLabeledComponent("Default long task threshold (minutes):", longTaskField!!)
            .addLabeledComponent("Time zone:", timeZoneComboBox!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val currentIdleTimeout = idleTimeoutField?.text?.toLongOrNull() ?: return false
        val currentLongTask = longTaskField?.text?.toLongOrNull() ?: return false
        val currentTimeZone = timeZoneComboBox?.selectedItem as? String ?: return false
        
        return currentIdleTimeout != settings.state.idleTimeoutMinutes ||
               currentLongTask != settings.state.longTaskMinutes ||
               currentTimeZone != settings.state.timeZoneId
    }

    override fun apply() {
        val idleTimeout = idleTimeoutField?.text?.toLongOrNull() ?: return
        val longTask = longTaskField?.text?.toLongOrNull() ?: return
        val timeZone = timeZoneComboBox?.selectedItem as? String ?: return
        
        settings.state.idleTimeoutMinutes = idleTimeout
        settings.state.longTaskMinutes = longTask
        settings.state.timeZoneId = timeZone
    }

    override fun reset() {
        idleTimeoutField?.text = settings.state.idleTimeoutMinutes.toString()
        longTaskField?.text = settings.state.longTaskMinutes.toString()
        timeZoneComboBox?.selectedItem = settings.state.timeZoneId
    }
}