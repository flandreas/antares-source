package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.Translations
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * A reusable UI component for specifying a file system directory.
 * Consists of an editable [JTextField] and a button that opens a [JFileChooser].
 */
class DirectorySelectionField(
	text: String = "",
	private val labelText: String? = null,
	preferredWidth: Int = 300,
	private val title: String = Translations.getString("base.action.chooseDirectory.title")
) : JPanel() {

	private val textField = JTextField(text)

	private val selectAction = SelectAction()

	/** Returns the selected path as a String. */
	val path: String get() = textField.text

	init {
		buildUI()

		textField.isEditable = false
		textField.alignmentX = Component.LEFT_ALIGNMENT
		textField.preferredSize = Dimension(preferredWidth, textField.preferredSize.height)
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		if (labelText != null) {
			val label = JLabel(labelText)
			label.alignmentX = Component.LEFT_ALIGNMENT
			add(label)
			add(Box.createHorizontalStrut(5))
		}
		add(textField)
		add(Box.createHorizontalStrut(5))

		val selectButton = JButton(selectAction)
		selectButton.alignmentX = Component.LEFT_ALIGNMENT
		add(selectButton)
	}

	private inner class SelectAction : AbstractAction(Translations.getString("base.action.select.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser(textField.text);
			fileChooser.dialogTitle = title
			fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

			if (fileChooser.showOpenDialog(this@DirectorySelectionField) == JFileChooser.APPROVE_OPTION) {
				textField.text = fileChooser.selectedFile.absolutePath
			}
		}
	}
}