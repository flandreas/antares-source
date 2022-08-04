package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.ActionWrapperSwing
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.swing.DialogBuilder
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.app.ApplicationMode
import ch.scorpion.jabbah.graph.app.ConstantApplicationModeHolder
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.event.TreeSelectionListener

/** An [Action] for editing a [Library] by using [LibraryCompositionPanel].*/
class EditLibraryAction(
	controller: LibraryTreeViewController,
	private val application: Application
) : AbstractLibraryAction(
	actionBaseName = "library.composition.action",
	operation = Change,
	controller
) {
	override fun execute(event: ActionEvent) {
		LibraryCompositionPanel.showAsDialog(controller.library!!, Frame.getFrames()[0], application, eventBus)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness() && controller.library != null
}

/**
 * A [JPanel] for composing a [Library] by choosing another [Library] from which to
 * drag in [MetaGraph]s.
 */
class LibraryCompositionPanel(
	private val destinationLibrary: Library,
	private val libraryManagementService: LibraryManagementService = LibraryModule.libraryManagementService,
	eventBus: EventBus = BaseModule.eventBus,
	application: Application,
	private val closeHandler: () -> Unit
) : JPanel() {

	companion object {
		fun showAsDialog(library: Library, parent: Frame, application: Application, eventBus: EventBus) {
			DialogBuilder<LibraryCompositionPanel>(parent)
				.title(Translations.getString("library.composition.title"))
				.content { dialog -> LibraryCompositionPanel(destinationLibrary = library, application = application, eventBus = eventBus) {
					dialog.dispose()
				} }
				.defaultButton { it.closeButton }
				.resizable()
				.show()
		}
	}

	private val sourceTreeController: LibraryTreeViewController
	private val sourceTreeView: LibraryTreeViewSwing

	private val destinationTreeController = LibraryTreeViewController(
		type = LibraryTreeViewType.CompositionDestination,
		library = destinationLibrary,
		applicationModeHolder = ConstantApplicationModeHolder(ApplicationMode.EDIT),
		eventBus = eventBus
	)

	private val destinationTreeView = LibraryTreeViewSwing(
		destinationTreeController,
		application = application,
		showWorkspaceNode = false)

	private val copyAction = CopyAction()

	private val sourceLibraries = JComboBox<LibraryDictionaryEntry>()

	private val isSourceElementSelected: Boolean get() = sourceTreeController.selectedItem is LibraryElement

	private val isDestinationFolderSelected: Boolean get() = destinationTreeController.selectedItem is LibraryDirectory

	private val selectedSourceLibraryId: LibraryIdentification get() = (sourceLibraries.selectedItem as LibraryDictionaryEntry).identification

	private val librarySelectionListener = TreeSelectionListener {
		copyAction.enabled = isSourceElementSelected && isDestinationFolderSelected
	}

	val closeButton = JButton(ActionWrapperSwing(CancelAction()))

	init {
		fillSourceLibraries()

		sourceTreeController = LibraryTreeViewController(
			type = LibraryTreeViewType.CompositionSource,
			applicationModeHolder = ConstantApplicationModeHolder(ApplicationMode.EDIT),
			library = getSelectedSourceLibrary(),
			eventBus = eventBus
		)

		// TODO With showWorkshopNode = false, Tree is empty after current Library has been changed?
		sourceTreeView = LibraryTreeViewSwing(
			sourceTreeController,
			application = application,
			showWorkspaceNode = true)

		sourceLibraries.addActionListener {
			sourceTreeController.library = getSelectedSourceLibrary()
		}

		sourceTreeView.addTreeSelectionListener(librarySelectionListener)
		destinationTreeView.addTreeSelectionListener(librarySelectionListener)

		copyAction.enabled = false

		buildUI()
	}

	fun dispose() {
		sourceTreeController.dispose()
		destinationTreeController.dispose()
	}

	private fun getSelectedSourceLibrary(): Library =
		libraryManagementService.loadLibrary(selectedSourceLibraryId)

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
		buttonPanel.add(closeButton)
		add(buttonPanel, BorderLayout.SOUTH)
	}

	private fun buildContentPanel(): JComponent {
		val panel = JPanel()
		val layout = GroupLayout(panel)
		panel.layout = layout

		layout.autoCreateGaps = true
		layout.autoCreateContainerGaps = true

		val sourceScrollPane = JScrollPane(sourceTreeView)
		val destinationScrollPane = JScrollPane(destinationTreeView)
		val originLabel = JLabel(Translations.getString("library.composition.source.text"))
		val destinationLabel = JLabel(Translations.getString("library.composition.destination.text"))

		val copyButton = JButton(ActionWrapperSwing(copyAction))
		copyButton.text = null
		copyButton.icon = UiUtil.themedIcon("/img/right-18.png")

		val addButton = JButton(ActionWrapperSwing(destinationTreeView.actions.addLibraryFolderAction))
		addButton.text = null
		addButton.icon = UiUtil.themedIcon("/img/plus-18.png")

		val removeButton = JButton(ActionWrapperSwing(destinationTreeView.actions.deleteLibraryFolderAction))
		removeButton.text = null
		removeButton.icon = UiUtil.themedIcon("/img/minus-18.png")

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
			val sourceElement = sourceTreeController.selectedItem as LibraryElement
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
				destinationTreeController.selectedItem as LibraryDirectory)
		}
	}

	private inner class CancelAction : AbstractAction("library.composition.close.action") {
		override fun execute(event: ActionEvent) {
			closeHandler.invoke()
		}
	}
}