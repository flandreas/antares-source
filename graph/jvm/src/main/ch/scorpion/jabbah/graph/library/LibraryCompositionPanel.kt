package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.BusyHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.TreeSelectionListener

/** An [Action] for editing a [Library] by using [LibraryCompositionPanel].*/
class EditLibraryAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction("library.composition.action", libraryTreeView, eventBus) {

	override fun execute(event: ActionEvent) {
		LibraryCompositionPanel.showAsDialog(libraryTreeView.library, Frame.getFrames()[0], eventBus)
	}

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser
	}
}

/**
 * A [JPanel] for composing a [Library] by choosing another [Library] from which to
 * drag in [MetaGraph]s.
 */
class LibraryCompositionPanel(
	private val destinationLibrary: Library,
	private val libraryManagementService: LibraryManagementService = LibraryModule.libraryManagementService,
	eventBus: EventBus = BaseModule.eventBus,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(library: Library, parent: Frame, eventBus: EventBus) {
			val dialog = JDialog(parent, true)
			BusyHandler.register(dialog, null)
			val panel = LibraryCompositionPanel(
				destinationLibrary = library,
				closeHandler = {
					dialog.isVisible = false
					dialog.dispose()
				},
				eventBus = eventBus)
			dialog.title = Translations.getString("library.composition.title")
			dialog.contentPane.add(panel)
			dialog.pack()
			dialog.setLocationRelativeTo(parent)
			dialog.addWindowListener(object : WindowAdapter() {
				override fun windowClosed(e: WindowEvent?) {
					panel.dispose()
					BusyHandler.deregister(dialog)
				}
			})
			dialog.isVisible = true
		}
	}

	private val sourceLibraryTree: LibraryTreeView

	private val destinationLibraryTree = LibraryTreeView(
		type = LibraryTreeViewType.CompositionDestination,
		library = destinationLibrary,
		project = null,
		eventBus = eventBus,
		showWorkspaceNode = false)

	private val copyAction = CopyAction()

	private val addAction = AddAction()

	private val removeAction = RemoveAction()

	private val sourceLibraries = JComboBox<LibraryDictionaryEntry>()

	private val isSourceElementSelected: Boolean get() = sourceLibraryTree.getSelectedItem() is LibraryElement

	private val isDestinationFolderSelected: Boolean get() = destinationLibraryTree.getSelectedItem() is LibraryDirectory

	private val selectedSourceLibraryUuid: UUID get() = (sourceLibraries.selectedItem as LibraryDictionaryEntry).uuid

	private val librarySelectionListener = TreeSelectionListener {
		copyAction.enabled = isSourceElementSelected && isDestinationFolderSelected
		addAction.enabled = isDestinationFolderSelected
	}

	init {
		fillSourceLibraries()

		// TODO With showWorkshopNode = false, Tree is empty after current Library has been changed?
		sourceLibraryTree = LibraryTreeView(
			type = LibraryTreeViewType.CompositionSource,
			library = getSelectedSourceLibrary(),
			project = null,
			eventBus = eventBus,
			showWorkspaceNode = true)

		sourceLibraries.addActionListener {
			sourceLibraryTree.library = getSelectedSourceLibrary()
		}

		sourceLibraryTree.addTreeSelectionListener(librarySelectionListener)
		destinationLibraryTree.addTreeSelectionListener(librarySelectionListener)

		copyAction.enabled = false
		addAction.enabled = false
		removeAction.enabled = false

		buildUI()
	}

	fun dispose() {
		sourceLibraryTree.dispose()
		destinationLibraryTree.dispose()
	}

	private fun getSelectedSourceLibrary(): Library {
		return libraryManagementService.loadLibrary(selectedSourceLibraryUuid)
	}

	private fun fillSourceLibraries() {
		libraryManagementService
			.getLibraryDirectoryEntries()
			.filter { it.uuid != destinationLibrary.uuid }
			.sortedBy { it.name.value }
			.forEach { sourceLibraries.addItem(it) }
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(700, 500)
		border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

		add(buildContentPanel(), BorderLayout.CENTER)

		val buttonPanel = JPanel()
		buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.LINE_AXIS)
		buttonPanel.add(Box.createHorizontalGlue())
		buttonPanel.add(JButton(ActionWrapperSwing(CancelAction())))
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun buildContentPanel(): JComponent {
		val panel = JPanel()
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

	/** Copies the currently selected source [LibraryElement] to the currently selected destination [LibraryDirectory].*/
	private inner class CopyAction : AbstractAction("library.composition.copy.action") {
		override fun execute(event: ActionEvent) {
			val sourceElement = sourceLibraryTree.getSelectedItem() as LibraryElement
			if (sourceElement is ContainerLibraryElement && !libraryManagementService.canCopyContainerLibraryElement(sourceElement, destinationLibrary)) {
				if (JOptionPane.showConfirmDialog(
						this@LibraryCompositionPanel,
						Translations.getString("library.composition.copy.incomplete.msg"),
						description,
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION
				) {
					return
				}
			}

			libraryManagementService.copyLibraryElement(
				sourceElement,
				destinationLibraryTree.getSelectedItem() as LibraryDirectory)
		}
	}

	private inner class CancelAction : AbstractAction("library.composition.close.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}
}