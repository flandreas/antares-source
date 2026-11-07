package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.LogLevel.*
import io.antarescircuit.jabbah.base.LogSystem.PROP_LOG_LEVEL
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.preferences.AbstractPreference
import io.antarescircuit.jabbah.base.preferences.PreferencesPanel
import javax.swing.JComboBox

class LogLevelPreference(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractPreference(id = PROP_LOG_LEVEL, nameKey = "base.preferences.logLevel") {

	private val editor = JComboBox<LogLevel>()

	private val value: LogLevel get() = valueOf(panel!!.preferences.getString(id))

	override var editable: Boolean = true
		set(value) {
			field = value
			editor.isEnabled = value
		}

	init {
		values().forEach { editor.addItem(it) }
		editor.addActionListener {
			if (panel != null) {
				panel?.preferences?.customize(this, (editor.selectedItem as LogLevel).name)
			}
		}
		registerEditor(editor)
		eventBus.register(PreferencesChangedEvent::class) { LogSystem.level = valueOf(BaseModule.properties.getString(PROP_LOG_LEVEL)) }
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}
}
