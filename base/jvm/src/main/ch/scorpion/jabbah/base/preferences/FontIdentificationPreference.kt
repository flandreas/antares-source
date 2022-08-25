package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.Translations
import org.drjekyll.fontchooser.FontDialog
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxRenderer


class FontIdentificationRenderer : BasicComboBoxRenderer() {

	override fun getListCellRendererComponent(
		list: JList<*>?,
		value: Any?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
		val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as BasicComboBoxRenderer
		if (value == null) {
			label.text = Translations.getString("base.font.choose")
		} else {
			label.text = (value as FontIdentification).toString()
		}
		return this
	}
}

class FontIdentificationPreference : AbstractPreference(
	id = FontIdentification.PROP_FONT_IDENTIFICATION,
	nameKey = "base.preference.font",
	needsRestart = true
) {

	private val editor = JComboBox<FontIdentification>()

	private val value: FontIdentification get() = FontIdentification.parse(panel!!.preferences.getString(id))

	init {
		editor.renderer = FontIdentificationRenderer()
		editor.addActionListener {
			if (panel != null) {
				val selectedItem = editor.selectedItem
				if (selectedItem == null) {
					showChooser()?.let {
						panel?.preferences?.customize(this, it.externalize())
						SwingUtilities.invokeLater {
							refreshEditor(it)
							editor.selectedItem = it
						}
					}
				} else {
					panel?.preferences?.customize(this, (selectedItem as FontIdentification).externalize())
				}
			}
		}
	}

	private fun showChooser(): FontIdentification? {
		val dialog = FontDialog(Frame.getFrames()[0], name, true)
		dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE;
		dialog.setLocationRelativeTo(panel)
		dialog.isVisible = true
		if (!dialog.isCancelSelected) {
			return FontIdentification(
				dialog.selectedFont.family,
				dialog.selectedFont.style,
				dialog.selectedFont.size)
		}
		return null
	}

	override fun addToPanel(panel: PreferencesPanel) {
		if (editor.itemCount == 0) {
			refreshEditor(FontIdentification.load())
		}
		this.panel = panel
		panel.addLabeledRow(name, editor)
	}

	override fun load() {
		editor.selectedItem = value
	}

	private fun refreshEditor(storedValue: FontIdentification) {
		editor.model = DefaultComboBoxModel()
		editor.addItem(storedValue)

		if (storedValue != FontIdentification.DEFAULT_VALUE) {
			editor.addItem(FontIdentification.DEFAULT_VALUE)
		}
		editor.addItem(null)

		editor.selectedIndex = 0
	}
}