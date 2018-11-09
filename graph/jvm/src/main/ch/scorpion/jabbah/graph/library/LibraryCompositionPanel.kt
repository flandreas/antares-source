package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


/** An [Action] for editing a [Library] by using [LibraryCompositionPanel].*/
class EditLibraryAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction("library.composition.action", eventBus) {

	override fun execute(event: ActionEvent) {
		LibraryCompositionPanel.showAsDialog(libraryTreeView!!.libraryHolder.library, Frame.getFrames()[0])
	}

	override fun calculateEnabledness(): Boolean {
		return true
	}
}

/**
 * A [JPanel] for composing a [Library] by choosing another [Library] from which to
 * drag in [MetaGraph]s.
 */
class LibraryCompositionPanel(
	private val library: Library,
	private val libraryManagementService: LibraryManagementService = LibraryModule.libraryManagementService,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		private val LOG by logger(LibraryCompositionPanel::class)

		fun showAsDialog(library: Library, parent: Frame) {
			val dialog = JDialog(parent, true)
			BusyHandler.register(dialog, null)
			dialog.title = Translations.getString("library.composition.title")
			dialog.contentPane.add(LibraryCompositionPanel(library = library, closeHandler = {
				dialog.isVisible = false
				dialog.dispose()
			}))
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					BusyHandler.deregister(dialog)
				}
			})
			dialog.isVisible = true
		}
	}

	private val sourceLibraryTree = JTree()

	private val destinationLibraryTree = JTree()

	private val copyAction = CopyAction()

	private val addAction = AddAction()

	private val removeAction = RemoveAction()

	private val sourceLibraries = JComboBox<String>()

	init {
		fillSourceLibraries()
		buildUI()
	}

	private fun fillSourceLibraries() {
		libraryManagementService
			.getLibraryNames()
			.filter { it != library.name }
			.forEach { sourceLibraries.addItem(it) }
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(700, 500)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		add(buildContentPanel(), BorderLayout.CENTER)

		val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun buildContentPanel(): JComponent {
		val panel = JPanel();
		val layout = GroupLayout(panel)
		panel.layout = layout

		layout.autoCreateGaps = true
		layout.autoCreateContainerGaps = true

		val sourceScrollPane = JScrollPane(sourceLibraryTree)
		val destinationScrollPane = JScrollPane(destinationLibraryTree)
		val originLabel = JLabel(Translations.getString("library.composition.source.text"))
		val destinationLabel = JLabel(Translations.getString("library.composition.destination.text"))

		val copyButton = JButton(ActionWrapperSwing(copyAction))
		copyButton.text = null
		copyButton.icon = ImageIcon(LibraryCompositionPanel::class.java.getResource("/img/right-18.png"))

		val addButton = JButton(ActionWrapperSwing(addAction))
		addButton.text = null
		addButton.icon = ImageIcon(LibraryCompositionPanel::class.java.getResource("/img/plus-18.png"))

		val removeButton = JButton(ActionWrapperSwing(removeAction))
		removeButton.text = null
		removeButton.icon = ImageIcon(LibraryCompositionPanel::class.java.getResource("/img/minus-18.png"))

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(originLabel)
					.addComponent(sourceLibraries)
					.addComponent(sourceScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Int.MAX_VALUE))
				.addComponent(copyButton)
				.addGroup(layout.createParallelGroup()
					.addComponent(destinationLabel)
					.addGroup(layout.createSequentialGroup()
						.addComponent(addButton)
						.addComponent(removeButton))
					.addComponent(destinationScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Int.MAX_VALUE))
		)

		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup()
					.addComponent(originLabel)
					.addComponent(sourceLibraries)
					.addComponent(sourceScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Int.MAX_VALUE))
				.addComponent(copyButton)
				.addGroup(layout.createSequentialGroup()
					.addComponent(destinationLabel)
					.addGroup(layout.createParallelGroup()
						.addComponent(addButton)
						.addComponent(removeButton))
					.addComponent(destinationScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Int.MAX_VALUE))
		)

		return panel
	}

	private inner class AddAction : AbstractAction("library.composition.add.action") {
		override fun execute(event: ActionEvent) {
			// TODO
		}
	}

	private inner class RemoveAction : AbstractAction("library.composition.remove.action") {
		override fun execute(event: ActionEvent) {
			// TODO
		}
	}


	private inner class CopyAction : AbstractAction("library.composition.copy.action") {
		override fun execute(event: ActionEvent) {
			// TODO
		}
	}

	private inner class CancelAction : AbstractAction("library.composition.close.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}
}