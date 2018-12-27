package ch.scorpion.jabbah.edit.model.text

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/** A [JPanel] for editing a [TextProperty] value in a multi-line text area of a popup-dialog.*/
class TextPropertyPanel(text: String) : JPanel() {

	companion object {
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			text: String
		): String? {
			val panel = TextPropertyPanel(text)
			return when (
				JOptionPane.showConfirmDialog(
					parent,
					panel,
					title,
					JOptionPane.OK_CANCEL_OPTION,
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
		buildUI()
	}

	private fun buildUI() {
		layout = BorderLayout()

		val scrollPane = JScrollPane()
		scrollPane.setViewportView(textField)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.preferredSize = Dimension(400, 500)
		add(scrollPane, BorderLayout.CENTER)
	}
}