package io.antarescircuit.jabbah.app.find

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.PlaceholderTextField
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/** Discovers existing application commands without duplicating their actions or keyboard shortcuts. */
class FindMenuAction(
	private val owner: JFrame,
	private val menuBar: JMenuBar,
) : AbstractAction(Translations.getString("application.findAction.name")) {
	init {
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
			java.awt.Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx or InputEvent.SHIFT_DOWN_MASK))
	}

	override fun actionPerformed(e: ActionEvent?) {
		val previousFocus = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner
		val dialog = JDialog(owner, Translations.getString("application.findAction.name"), true)
		val query = PlaceholderTextField(Translations.getString("application.findAction.placeholder"), 40, true)
		val model = DefaultListModel<MenuCommand>()
		val results = JList(model)
		val status = JLabel()
		val commands = MenuCommandSearch.collect(menuBar)
		query.accessibleContext.accessibleName = Translations.getString("application.findAction.name")
		results.accessibleContext.accessibleName = Translations.getString("application.findAction.resultList")
		results.selectionMode = ListSelectionModel.SINGLE_SELECTION
		results.cellRenderer = CommandRenderer()
		results.visibleRowCount = 12

		fun update() {
			val matches = MenuCommandSearch.filter(commands, query.text)
			model.clear()
			matches.forEach { model.addElement(it) }
			if (!model.isEmpty) results.selectedIndex = 0
			status.text = Translations.getString("application.findAction.results", matches.size)
		}
		fun close() {
			dialog.dispose()
			previousFocus?.requestFocusInWindow()
		}
		fun execute() {
			val command = results.selectedValue ?: return
			if (!command.enabled) return
			close()
			// Let the owner recover focus before actions inspect the active editor.
			SwingUtilities.invokeLater { command.execute() }
		}
		fun bind(key: KeyStroke, name: String, action: () -> Unit) {
			dialog.rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, name)
			dialog.rootPane.actionMap.put(name, object : AbstractAction() {
				override fun actionPerformed(e: ActionEvent?) = action()
			})
		}
		fun move(delta: Int) {
			if (!model.isEmpty) {
				results.selectedIndex = (results.selectedIndex + delta).coerceIn(0, model.size - 1)
				results.ensureIndexIsVisible(results.selectedIndex)
			}
		}
		query.document.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) = update()
			override fun removeUpdate(e: DocumentEvent?) = update()
			override fun changedUpdate(e: DocumentEvent?) = update()
		})
		query.addActionListener { execute() }
		// Bind arrows on the query itself, where text field defaults otherwise consume them.
		for ((key, delta) in listOf(KeyEvent.VK_DOWN to 1, KeyEvent.VK_UP to -1)) {
			val name = "move$delta"
			query.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, 0), name)
			query.actionMap.put(name, object : AbstractAction() {
				override fun actionPerformed(e: ActionEvent?) = move(delta)
			})
		}
		results.addMouseListener(object : MouseAdapter() {
			override fun mouseClicked(e: MouseEvent) {
				if (e.clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
					val index = results.locationToIndex(e.point)
					if (index >= 0 && results.getCellBounds(index, index).contains(e.point)) execute()
				}
			}
		})
		bind(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close", ::close)
		bind(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "execute", ::execute)
		val panel = JPanel(BorderLayout(0, 8))
		panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
		panel.add(query, BorderLayout.NORTH)
		panel.add(JScrollPane(results).apply { preferredSize = Dimension(640, 400) }, BorderLayout.CENTER)
		panel.add(status, BorderLayout.SOUTH)
		dialog.contentPane = panel
		dialog.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
		update()
		dialog.pack()
		dialog.setLocationRelativeTo(owner)
		SwingUtilities.invokeLater { query.requestFocusInWindow() }
		dialog.isVisible = true
	}

	internal class CommandRenderer : ListCellRenderer<MenuCommand> {
		private val panel = JPanel(BorderLayout(8, 2))
		private val title = JLabel()
		private val path = JLabel()
		private val shortcut = JLabel()

		init {
			panel.border = BorderFactory.createEmptyBorder(6, 8, 6, 8)
			panel.add(title, BorderLayout.CENTER)
			panel.add(path, BorderLayout.SOUTH)
			panel.add(shortcut, BorderLayout.EAST)
		}

		override fun getListCellRendererComponent(list: JList<out MenuCommand>, value: MenuCommand, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
			title.text = value.name
			path.text = value.path
			shortcut.text = value.item.accelerator?.let {
				"${InputEvent.getModifiersExText(it.modifiers)}+${KeyEvent.getKeyText(it.keyCode)}"
			}.orEmpty()
			panel.background = if (isSelected) list.selectionBackground else list.background
			panel.isEnabled = value.enabled
			panel.accessibleContext.accessibleName = listOf(value.name, value.path, shortcut.text).filter { it.isNotBlank() }.joinToString(", ")
			for (label in listOf(title, path, shortcut)) {
				label.foreground = if (isSelected) list.selectionForeground else list.foreground
				label.isEnabled = value.enabled
			}
			return panel
		}
	}
}
