package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.DialogBuilder
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.base.ui.UIBasics
import io.antarescircuit.jabbah.edit.auth.Authorizer
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.auth.Operation.Change
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationMode
import io.antarescircuit.jabbah.graph.app.ConstantApplicationModeHolder
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryEntry
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewType
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
	companion object {
		private val LOG by logger(EditLibraryAction::class)
	}

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		with(controller.library!!) {
			LOG.userTrail("Edit library '${name.value}' ($uuid)")
			LibraryCompositionPanel.showAsDialog(this, Frame.getFrames()[0], application, controller.eventBus)
		}
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && controller.selectedItem === controller.library
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
	private val closeHandler: (LibraryCompositionPanel) -> Unit
) : JPanel() {

	companion object {
		private val LOG by logger(LibraryCompositionPanel::class)

		fun showAsDialog(library: Library, parent: Frame, application: Application, eventBus: EventBus) {
			DialogBuilder<LibraryCompositionPanel>(parent)
				.title(Translations.getString("library.composition.title"))
				.content { dialog -> LibraryCompositionPanel(destinationLibrary = library, application = application, eventBus = eventBus) {
					it.dispose()
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
		showWorkspaceNode = false,
		includeImports = false)

	private val copyAction = CopyAction()

	private val sourceLibraries = JComboBox<LibraryDictionaryEntry>()

	private val isSourceElementSelected: Boolean get() = sourceTreeController.selectedItem is LibraryElement

	private val isDestinationFolderSelected: Boolean get() = destinationTreeController.selectedItem is LibraryDirectory

	private val selectedSourceLibraryId: LibraryIdentification get() = (sourceLibraries.selectedItem as LibraryDictionaryEntry).identification

	private val librarySelectionListener = TreeSelectionListener {
		UiUtil.invokeLater {
			copyAction.enabled = isSourceElementSelected && isDestinationFolderSelected
		}
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

		// TODO With showWorkspaceNode = false, Tree is empty after current Library has been changed?
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
			.filter { Authorizer.isCurrentUserAuthorizedTo(Operation.View, it) }
			.sortedBy { it.name.value }
			.forEach { sourceLibraries.addItem(it) }
	}

	private fun buildUI() {
		layout = BorderLayout()
		preferredSize = Dimension(700, 500)
		border = UIBasics.createDialogBorder()

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
		addButton.toolTipText = destinationTreeView.actions.addLibraryFolderAction.name
		addButton.icon = UiUtil.themedIcon("/img/plus-18.png")

		val removeButton = JButton(ActionWrapperSwing(destinationTreeView.actions.deleteLibraryFolderAction))
		removeButton.text = null
		removeButton.toolTipText = destinationTreeView.actions.deleteLibraryFolderAction.name
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
			LOG.userTrail("End editing library")
			closeHandler.invoke(this@LibraryCompositionPanel)
		}
	}
}