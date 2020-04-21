package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.Addressable
import ch.scorpion.antares.model.memory.Memory
import ch.scorpion.antares.model.memory.MemoryDump
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.DrawingViewContent
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.model.GraphElementAdapter
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.ui.AbstractGraphDesktopItemPanel
import ch.scorpion.jabbah.graph.ui.GraphDesktopItem
import ch.scorpion.jabbah.graph.ui.GraphDesktopItemHeaderPanel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*


class MemoryContentGraphDesktopItem(
	memory: Memory,
	addressable: Addressable,
	title: String,
	cmdManager: CommandManager = EditModule.commandManager,
	readonly: Boolean = false,
	contextColor: CompositeColor
) : AbstractGraphDesktopItemPanel() {

	private val memoryContentPanel = MemoryContentsPanel(memory, addressable, cmdManager, readonly)

	private val headerPanel = GraphDesktopItemHeaderPanel(this, JLabel(title), allowClose = true)

	init {
		buildUI(contextColor)
	}

	private fun buildUI(contextColor: CompositeColor) {
		layout = BorderLayout()
		add(headerPanel, BorderLayout.NORTH)
		add(memoryContentPanel, BorderLayout.CENTER)
		super.contextColor = contextColor
	}

	/** ---- [GraphDesktopItem] */

	override val drawingView: DrawingView<GraphView>?
		get() = null

	override fun dispose() {
		memoryContentPanel.dispose()
	}

	override fun findContent(condition: (DrawingViewContent<GraphView>) -> Boolean): DrawingViewContent<*>? = null

	override fun addContextColorBorder(color: Color) {
		memoryContentPanel.border = createContextColorBorder(color)
	}

	override fun removeContextColorBorder() {
		memoryContentPanel.border = null
	}
}

/**
 * Displays the contents of a [Memory].
 */
class MemoryContentsPanel(
	private val memory: Memory,
	private val addressable: Addressable,
	private val cmdManager: CommandManager,
	readonly: Boolean = false,
	private val closeHandler: ((MemoryContentsPanel) -> Unit)? = null
) : JPanel() {

	companion object {
		private val LOG by logger(MemoryContentsPanel::class)
		private const val PREF_WIDTH = 800
		private const val PREF_HEIGHT = 500

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			name: String,
			memory: Memory,
			addressable: Addressable,
			cmdManager: CommandManager,
			readonly: Boolean
		) {
			val editable = GraphViewModule.applicationModeHolder.currentMode.isEdit() && addressable.editableWhileEditingAction

			val dialog = JDialog(parent, true)
			val contentsPanel = MemoryContentsPanel(memory, addressable, cmdManager, readonly || !editable) {
				dialog.isVisible = false
				it.dispose()
			}
			dialog.title = Translations.getString("antares.action.memory.contents.title", name)
			dialog.contentPane.add(contentsPanel)
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.isVisible = true
		}
	}

	private val memoryDisplayPanel = MemoryDisplayPanel(addressable, !readonly)

	private val addressableListener = object : GraphElementAdapter() {
		override fun stateChanged(e: GraphElementEvent) {
			invalidate()
			repaint()
		}
	}

	init {
		addressable.addGraphElementListener(addressableListener)
		buildUI(readonly)
	}

	fun dispose() {
		addressable.removeGraphElementListener(addressableListener)
	}

	/** ---- [MemoryContentsPanel] */

	private fun buildUI(readonly: Boolean) {
		layout = BorderLayout()

		val contentsView = JPanel(BorderLayout())
		contentsView.preferredSize = Dimension(PREF_WIDTH, PREF_HEIGHT)
		contentsView.add(memoryDisplayPanel)
		add(contentsView, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())

		if (!readonly) {
			buttonPanel.add(JButton(ImportAction()))
		}
		buttonPanel.add(JButton(ExportAction()))
		if (!readonly) {
			buttonPanel.add(JButton(ClearAction()))
		}
		if (closeHandler != null) {
			buttonPanel.add(JButton(CloseAction()))
		}

		add(buttonPanel, BorderLayout.SOUTH)
	}

	private inner class CloseAction : AbstractAction(Translations.getString("file.action.close.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val changes = memoryDisplayPanel.changes
			LOG.debug("User has changed ${changes.size} memory cells")
			cmdManager.register(MemoryCellChangeCommand(memory, changes))
			closeHandler?.invoke(this@MemoryContentsPanel)
		}
	}

	private inner class ImportAction : AbstractAction(Translations.getString("antares.action.memory.import.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser()
			if (fileChooser.showOpenDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
				try {
					cmdManager.execute(MemoryContentsCommand(memory, addressable.dataWidth, fileChooser.selectedFile!!.absolutePath))
					memoryDisplayPanel.refresh()
				} catch (e: IllegalArgumentException) {
					LOG.error("Invalid data in memory file: ${e.message}")
					JOptionPane.showConfirmDialog(
						JFrame.getFrames()[0],
						Translations.getString("antares.memory.invalidData.text"),
						getValue(Action.NAME) as String,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE)
				} catch (e: Throwable) {
					LOG.error("General error while reading memory file: ${e.message}")
					JOptionPane.showConfirmDialog(
						JFrame.getFrames()[0],
						Translations.getString("antares.memory.cannotReadFile.text"),
						getValue(Action.NAME) as String,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.ERROR_MESSAGE)
				}
			}
		}
	}

	private inner class ClearAction : AbstractAction(Translations.getString("antares.action.memory.clear.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			cmdManager.execute(MemoryClearCommand(addressable, memory, addressable.dataWidth))
			memoryDisplayPanel.refresh()
		}
	}

	private inner class ExportAction : AbstractAction(Translations.getString("antares.action.memory.export.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser()
			if (fileChooser.showSaveDialog(this@MemoryContentsPanel) == JFileChooser.APPROVE_OPTION) {
				val contents = MemoryDump.write(memory, addressable.dataWidth)
				try {
					Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
				} catch (e: Throwable) {
					LOG.error("Error while exporting memory to '${fileChooser.selectedFile.absolutePath}'")
				}
			}
		}
	}
}