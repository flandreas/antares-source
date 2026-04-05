package io.antarescircuit.jabbah.edit.model.text

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/** A [JPanel] for editing a [TextProperty] value in a multi-line text area of a popup-dialog.*/
open class TextPropertyPanel(
	text: String,
	font: Font? = null,
	editable: Boolean = true
) : JPanel() {

	companion object {

		/**
		 * Allows the user to edit a text in a popup dialog.
		 * @return the edited text, or `null` if the user closed the popup dialog with 'Cancel'.
		 */
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			text: String,
			font: Font? = null,
			editable: Boolean = true
		): String? {
			val panel = TextPropertyPanel(text, font, editable)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					if (editable) JOptionPane.OK_CANCEL_OPTION else JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE)
				) {
					JOptionPane.OK_OPTION -> panel.text
					else -> null
				}
		}
	}

	private val textField = JTextArea(text)

	val text: String get() = textField.text

	init {
		textField.wrapStyleWord = true
		textField.lineWrap = true
		textField.wrapStyleWord = true
		textField.isEditable = editable

		font?.let { textField.font = it }

		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout()

		val scrollPane = JScrollPane()
		scrollPane.setViewportView(textField)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.preferredSize = Dimension(600, 500)
		add(scrollPane, BorderLayout.CENTER)
	}
}