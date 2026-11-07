package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.preferences.AbstractPreference
import io.antarescircuit.jabbah.base.preferences.PreferencesPanel
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPasswordField
import javax.swing.UIManager
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * The preference for the OpenRouter API key of the circuit assistant.
 *
 * A dedicated preference (instead of a plain string preference) because the key must be masked
 * and because the user has to be told that the `OPENROUTER_API_KEY` environment variable wins over
 * the entered value, and that the entered value is stored unencrypted in the preferences file.
 */
class ApiKeyPreference : AbstractPreference(
	id = OpenRouterConfig.PROP_API_KEY,
	nameKey = "antares.ai.preferences.apiKey"
) {

	private val editor = JPasswordField(24)

	private val hint = JLabel()

	private val documentListener = object : DocumentListener {
		override fun insertUpdate(e: DocumentEvent?) = commit()
		override fun removeUpdate(e: DocumentEvent?) = commit()
		override fun changedUpdate(e: DocumentEvent?) = commit()
	}

	override var editable: Boolean = true
		set(value) {
			field = value
			editor.isEnabled = value
		}

	init {
		editor.maximumSize = Dimension(Int.MAX_VALUE, editor.preferredSize.height)
		editor.document.addDocumentListener(documentListener)
		registerEditor(editor)
	}

	override fun addToPanel(panel: PreferencesPanel) {
		this.panel = panel
		panel.addLabeledRow(name, editor)
		hint.text = when (OpenRouterConfig.keySource()) {
			OpenRouterConfig.KeySource.Environment ->
				Translations.getString("antares.ai.preferences.apiKey.fromEnvironment", OpenRouterConfig.ENV_API_KEY)
			else ->
				Translations.getString("antares.ai.preferences.apiKey.hint", OpenRouterConfig.ENV_API_KEY)
		}
		UIManager.getColor("Label.disabledForeground")?.let { hint.foreground = it }
		panel.addRow(hint)
	}

	override fun load() {
		val stored = panel?.preferences?.getString(id) ?: ""
		if (String(editor.password) != stored) {
			editor.document.removeDocumentListener(documentListener)
			editor.text = stored
			editor.document.addDocumentListener(documentListener)
		}
	}

	private fun commit() {
		panel?.preferences?.customize(this, String(editor.password))
	}
}
