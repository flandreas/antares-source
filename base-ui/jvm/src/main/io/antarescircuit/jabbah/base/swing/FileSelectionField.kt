package io.antarescircuit.jabbah.base.swing

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.filechooser.FileFilter

/**
 * A reusable UI component for specifying a file system file.
 * Consists of a [JComboBox] and a button that opens a [JFileChooser].
 * If [usage] is provided, paths selected through the file chooser are persisted as
 * a six-entry history for that usage.
 */
class FileSelectionField(
	private val mode: Mode = Mode.Directory,
	text: String = "",
	private val labelText: String? = null,
	preferredWidth: Int = 300,
	private val title: String = mode.title,
	private val filter: FileFilter? = null,
	private val usage: String? = null,
	private val selectHandler: (String) -> Unit = {}
) : JPanel() {

	companion object {
		private const val MAX_HISTORY_SIZE = 6
		private const val HISTORY_PROPERTY_PREFIX = "base.fileSelectionField.history"
	}

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

	private val pathComboBox = JComboBox<String>()

	private val selectAction = SelectAction()

	/** Returns the selected path as a String. */
	var path: String
		get() = pathComboBox.selectedItem as? String ?: ""
		set(value) { setPath(value) }

	/** Adds the current path to this field's history, if history is enabled. */
	fun rememberPath() {
		storeHistory(path)
	}

	var selectionEnabled: Boolean = true
		set(value) {
			if (value != field) {
				field = value
				selectAction.isEnabled = value
				pathComboBox.isEnabled = value
			}
		}

	init {
		loadPaths(text)
		buildUI()

		pathComboBox.isEditable = false
		pathComboBox.alignmentX = LEFT_ALIGNMENT
		pathComboBox.preferredSize = Dimension(preferredWidth, pathComboBox.preferredSize.height)
		pathComboBox.addActionListener { selectHandler(path) }
	}

	private fun buildUI() {
		layout = BoxLayout(this, BoxLayout.LINE_AXIS)
		if (labelText != null) {
			val label = JLabel(labelText)
			label.alignmentX = LEFT_ALIGNMENT
			add(label)
			add(Box.createHorizontalStrut(5))
		}
		add(pathComboBox)
		add(Box.createHorizontalStrut(5))

		val selectButton = JButton(selectAction)
		selectButton.alignmentX = LEFT_ALIGNMENT
		add(selectButton)
	}

	private inner class SelectAction : AbstractAction(Translations.getString("base.action.select.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser(path)
			fileChooser.dialogTitle = title
			fileChooser.fileSelectionMode = mode.selectionMode
			fileChooser.fileFilter = filter

			if (fileChooser.showOpenDialog(this@FileSelectionField) == JFileChooser.APPROVE_OPTION) {
				setPath(fileChooser.selectedFile.absolutePath, addToHistory = true)
			}
		}
	}

	private fun loadPaths(initialPath: String) {
		val history = usage?.let { loadHistory(it) }.orEmpty().toMutableList()
		if (initialPath.isNotBlank() && initialPath !in history) {
			history.add(0, initialPath)
		}
		history.take(MAX_HISTORY_SIZE).forEach(pathComboBox::addItem)
		if (initialPath.isNotBlank()) {
			pathComboBox.selectedItem = initialPath
		}
	}

	private fun setPath(value: String, addToHistory: Boolean = false) {
		if (value.isNotBlank() && (0 until pathComboBox.itemCount).none { pathComboBox.getItemAt(it) == value }) {
			pathComboBox.insertItemAt(value, 0)
			while (pathComboBox.itemCount > MAX_HISTORY_SIZE) {
				pathComboBox.removeItemAt(pathComboBox.itemCount - 1)
			}
		}
		pathComboBox.selectedItem = value
		if (addToHistory) {
			storeHistory(value)
		}
	}

	private fun loadHistory(usage: String): List<String> =
		(0 until MAX_HISTORY_SIZE)
			.mapNotNull { BaseModule.settings.getString(historyProperty(usage, it), "").takeIf(String::isNotBlank) }

	private fun storeHistory(path: String) {
		val usage = usage ?: return
		if (path.isBlank()) {
			return
		}
		val history = listOf(path) + loadHistory(usage).filter { it != path }
		(0 until MAX_HISTORY_SIZE).forEach { index ->
			val property = historyProperty(usage, index)
			if (index < history.size) {
				BaseModule.settings.set(property, history[index])
			} else {
				BaseModule.settings.remove(property)
			}
		}
		pathComboBox.removeAllItems()
		history.take(MAX_HISTORY_SIZE).forEach(pathComboBox::addItem)
		pathComboBox.selectedItem = path
	}

	private fun historyProperty(usage: String, index: Int): String =
		"$HISTORY_PROPERTY_PREFIX.$usage.$index"
}
