package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.Translations
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.filechooser.FileFilter

/**
 * A reusable UI component for specifying a file system file.
 * Consists of an editable [JTextField] and a button that opens a [JFileChooser].
 */
class FileSelectionField(
	private val mode: Mode = Mode.Directory,
	text: String = "",
	private val labelText: String? = null,
	preferredWidth: Int = 300,
	private val title: String = mode.title,
	private val filter: FileFilter? = null,
	private val selectHandler: (String) -> Unit = {}
) : JPanel() {

	enum class Mode {
		Directory {
			override val titleKey: String get() = "base.action.chooseDirectory.title"
			override val selectionMode: Int get() = JFileChooser.DIRECTORIES_ONLY
		},
		File {
			override val titleKey: String get() = "base.action.chooseFile.title"
			override val selectionMode: Int get() = JFileChooser.FILES_ONLY
		};

		abstract val titleKey: String
		abstract val selectionMode: Int
		val title: String get() = Translations.getString(titleKey)
	}

	private val textField = JTextField(text)

	private val selectAction = SelectAction()

	/** Returns the selected path as a String. */
	var path: String
		get() = textField.text
		set(value) { textField.text = value }

	var selectionEnabled: Boolean = true
		set(value) {
			if (value != field) {
				field = value
				selectAction.isEnabled = value
			}
		}

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
			fileChooser.fileSelectionMode = mode.selectionMode
			fileChooser.fileFilter = filter

			if (fileChooser.showOpenDialog(this@FileSelectionField) == JFileChooser.APPROVE_OPTION) {
				textField.text = fileChooser.selectedFile.absolutePath
				selectHandler.invoke(textField.text)
			}
		}
	}
}