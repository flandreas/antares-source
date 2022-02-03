package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.AddressableClearCommand
import ch.scorpion.antares.model.addressable.MemoryDump
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.model.GraphElementAdapter
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*

/**
 * Displays the contents of an [Addressable] using a [AddressableDisplayPanel], along with [Action]s for importing,
 * exporting and resetting the memory contents.
 *
 * Must not keep reference to [Addressable] in order to deal with changing snapshots due to [Command] execution.
 */
class AddressableContentsPanel(
	private val controller: ApplicationDataViewController,
	applicationContextHolder: GraphApplicationContextHolder,
	private val addressableId: Int,
	private val cmdManager: CommandManager,
	readonly: Boolean = false,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val closeHandler: ((AddressableContentsPanel) -> Unit)? = null
) : JPanel() {

	companion object {
		private val LOG by logger(AddressableContentsPanel::class)
		private const val PREF_WIDTH = 800
		private const val PREF_HEIGHT = 500

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			controller: ApplicationDataViewController,
			applicationContextHolder: GraphApplicationContextHolder,
			name: String,
			addressableId: Int,
			cmdManager: CommandManager,
			readonly: Boolean
		) {
			DialogBuilder<AddressableContentsPanel>(parent)
				.content { dialog -> AddressableContentsPanel(controller, applicationContextHolder, addressableId, cmdManager, readonly) { dialog.dispose()} }
				.title(Translations.getString("antares.action.memory.contents.title", name))
				.defaultButton { it.closeButton }
				.resizable()
				.show()
		}
	}

	private val graphView: GraphView get() = (controller.data!!.content as MetaGraph).graph.graphView
	private val addressable: Addressable get() = graphView.graph!!.withId(addressableId) as Addressable

	private val memoryDisplayPanel = AddressableDisplayPanel(addressable, !readonly, applicationContextHolder, controller)

	/** React to snapshot changes. */
	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { updateAddressableListener() }

	private val addressableListener = object : GraphElementAdapter() {
		override fun stateChanged(e: GraphElementEvent) {
			invalidate()
			repaint()
		}
	}

	/** The [Addressable] listened to by [addressableListener]. Must be updated with snapshot changes. */
	private var listenedAddressable: Addressable = addressable

	var closeButton: JButton? = null

	init {
		addressable.addGraphElementListener(addressableListener)
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
		buildUI(readonly)
	}

	fun dispose() {
		listenedAddressable.removeGraphElementListener(addressableListener)
		eventBus.unregister(applicationDataContentHandler)
		memoryDisplayPanel.dispose()
	}

	private fun updateAddressableListener() {
		listenedAddressable.removeGraphElementListener(addressableListener)
		listenedAddressable = addressable
		listenedAddressable.addGraphElementListener(addressableListener)
	}

	/** ---- [AddressableContentsPanel] */

	private fun buildUI(readonly: Boolean) {
		layout = BorderLayout()

		val contentsView = JPanel(BorderLayout())
		contentsView.preferredSize = Dimension(PREF_WIDTH, PREF_HEIGHT)
		contentsView.add(memoryDisplayPanel)
		add(contentsView, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		if (!readonly) {
			buttonPanel.add(Box.createHorizontalStrut(2))
			buttonPanel.add(JButton(ImportAction()))
		}

		buttonPanel.add(Box.createHorizontalStrut(2))
		buttonPanel.add(JButton(ExportAction()))
		if (!readonly) {
			buttonPanel.add(Box.createHorizontalStrut(2))
			buttonPanel.add(JButton(ClearAction()))
		}
		if (closeHandler != null) {
			closeButton = JButton(CloseAction())
			buttonPanel.add(Box.createHorizontalGlue())
			buttonPanel.add(closeButton)
		}

		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun executeCommand(command: Command) {
		if (addressable.storesCells) {
			cmdManager.execute(command)
		} else {
			command.execute()
		}
	}

	private inner class CloseAction : AbstractAction(Translations.getString("file.action.close.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			closeHandler?.invoke(this@AddressableContentsPanel)
		}
	}

	private inner class ImportAction : AbstractAction(Translations.getString("antares.action.memory.import.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser()
			if (fileChooser.showOpenDialog(this@AddressableContentsPanel) == JFileChooser.APPROVE_OPTION) {
				try {
					executeCommand(AddressableContentsCommand(controller, addressable.id, addressable.dataWidth, fileChooser.selectedFile!!.absolutePath))
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
			executeCommand(AddressableClearCommand(controller, addressable.id, addressable.dataWidth))
			memoryDisplayPanel.refresh()
		}
	}

	private inner class ExportAction : AbstractAction(Translations.getString("antares.action.memory.export.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser()
			if (fileChooser.showSaveDialog(this@AddressableContentsPanel) == JFileChooser.APPROVE_OPTION) {
				val contents = MemoryDump.write(addressable.memory, addressable.dataWidth)
				try {
					Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
				} catch (e: Throwable) {
					LOG.error("Error while exporting memory to '${fileChooser.selectedFile.absolutePath}'")
				}
			}
		}
	}
}