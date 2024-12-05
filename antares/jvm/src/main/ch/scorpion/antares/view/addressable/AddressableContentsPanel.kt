package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.*
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.ui.UIBasics
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.GraphView
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.FocusListener
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
	private val view: DrawingView<GraphView>?,
	private val applicationContextHolder: GraphApplicationContextHolder,
	link: ObjectLink<Addressable>,
	private val cmdManager: CommandManager,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val closeHandler: ((AddressableContentsPanel) -> Unit)? = null
) : JPanel() {

	companion object {
		private val LOG by logger(AddressableContentsPanel::class)
		private const val PREF_WIDTH = 800
		private const val PREF_HEIGHT = 500

		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			view: DrawingView<GraphView>,
			applicationContextHolder: GraphApplicationContextHolder,
			name: String,
			link: ObjectLink<Addressable>,
			cmdManager: CommandManager
		) {
			DialogBuilder<AddressableContentsPanel>(parent)
				.content { dialog -> AddressableContentsPanel(view, applicationContextHolder, link, cmdManager) { dialog.dispose()} }
				.title(Translations.getString("antares.action.memory.contents.title", name))
				.defaultButton { it.closeButton }
				.resizable()
				.onWindowClosed { it.dispose() }
				.show()
		}
	}

	val addressableRef = AddressableReference(link, view, eventBus)

	private val memoryDisplayPanel = AddressableDisplayPanel(addressableRef, { editable } , applicationContextHolder)

	private val addressableDataListener = object : AddressableDataListener {
		override fun dataChanged(event: AddressableDataEvent) {
			handle()
		}

		override fun commentChanged(event: AddressableCommentEvent) {
			handle()
		}

		private fun handle() {
			invalidate()
			repaint()
		}
	}

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = { updateEditable() }

	private val importAction = ImportAction()
	private val importButton = JButton(importAction)

	private val exportAction = ExportAction()
	private val exportButton = JButton(exportAction)

	private val clearAction = ClearAction()
	private val clearButton = JButton(clearAction)

	private val closeAction = CloseAction()

	private var editable: Boolean = false

	var closeButton: JButton? = null

	init {
		addressableRef.addDataListener(addressableDataListener)
		buildUI()
		updateEditable()
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationModeHandler)
		addressableRef.removeDataListener(addressableDataListener)
		addressableRef.dispose()
		memoryDisplayPanel.dispose()
	}

	/** ---- [AddressableContentsPanel] */

	private fun updateEditable() {
		editable = if (addressableRef.addressable.storesCells) {
			(view == null || view.editable) && applicationContextHolder.applicationModeHolder.currentMode.isEdit()
		} else {
			applicationContextHolder.applicationModeHolder.currentMode.isExecute()
		}

		importAction.isEnabled = editable
		clearAction.isEnabled = editable
		memoryDisplayPanel.refresh()
	}

	private fun buildUI() {
		layout = BorderLayout()

		val contentsView = JPanel(BorderLayout())
		contentsView.preferredSize = Dimension(PREF_WIDTH, PREF_HEIGHT)
		contentsView.add(memoryDisplayPanel)
		add(contentsView, BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.border = UIBasics.createDialogBorder()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)

		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(importButton)

		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(exportButton)
		buttonPanel.add(Box.createHorizontalStrut(UIBasics.BUTTON_GAP))
		buttonPanel.add(clearButton)

		if (closeHandler != null) {
			closeButton = JButton(closeAction)
			buttonPanel.add(Box.createHorizontalGlue())
			buttonPanel.add(closeButton)
		}

		add(buttonPanel, BorderLayout.SOUTH)
	}

	fun addViewActivationFocusListener(focusListener: FocusListener) {
		memoryDisplayPanel.addViewActivationFocusListener(focusListener)
		importButton.addFocusListener(focusListener)
		exportButton.addFocusListener(focusListener)
		clearButton.addFocusListener(focusListener)
	}

	private fun executeCommand(command: Command) {
		if (addressableRef.addressable.storesCells) {
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
					executeCommand(AddressableContentsCommand(view, addressableRef.link, addressableRef.addressable.dataWidth, fileChooser.selectedFile!!.absolutePath))
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
			executeCommand(AddressableClearCommand(view, addressableRef.link, addressableRef.addressable.dataWidth))
			memoryDisplayPanel.refresh()
		}
	}

	private inner class ExportAction : AbstractAction(Translations.getString("antares.action.memory.export.name")) {
		override fun actionPerformed(e: ActionEvent?) {
			val fileChooser = JFileChooser()
			if (fileChooser.showSaveDialog(this@AddressableContentsPanel) == JFileChooser.APPROVE_OPTION) {
				val contents = MemoryDump.write(addressableRef.addressable.memory, addressableRef.addressable.dataWidth)
				try {
					Files.write(Paths.get(fileChooser.selectedFile.absolutePath), contents.toByteArray())
				} catch (e: Throwable) {
					LOG.error("Error while exporting memory to '${fileChooser.selectedFile.absolutePath}'")
				}
			}
		}
	}
}