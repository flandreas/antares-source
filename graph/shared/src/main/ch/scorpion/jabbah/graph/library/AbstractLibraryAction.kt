package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import ch.scorpion.jabbah.graph.ui.library.LibrarySelectionChangedEvent
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * A base class for implementing [Action]s that operate on items of [LibraryTreeView].
 * Listens for [LibrarySelectionChangedEvent]s and remembers the source [LibraryTreeView].
 */
abstract class AbstractLibraryAction(
	actionBaseName: String,
	protected val operation: Operation,
	protected val controller: LibraryTreeViewController,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractApplicationModeEditAction(actionBaseName, controller.applicationModeHolder, eventBus = controller.eventBus) {

	private val librarySelectionChangeHandler: EventHandler<LibrarySelectionChangedEvent> = {
		if (it.controller === controller) {
			updateEnabledness()
			handleSelectionChanged()
		}
	}

	private val commandEventHandler: EventHandler<CommandEvent> = { updateEnabledness() }

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { updateEnabledness() }

	protected val selectedItem: LibraryItem? get() = controller.selectedItem

	protected val folderOfSelectedItem: LibraryDirectory? get() = controller.view.folderOfSelectedItem

	init {
		eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionChangeHandler)
		eventBus.register(CommandEvent::class, commandEventHandler)
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(librarySelectionChangeHandler)
		eventBus.unregister(commandEventHandler)
		eventBus.unregister(currentLibraryHandler)
	}

	override fun calculateEnabledness(): Boolean =
		noStateChangeInterference && operationAuthorized

	protected open val operationAuthorized: Boolean get() =
		LibraryModule.libraryHolder.l != null && Authorizer.isCurrentUserAuthorizedTo(operation, LibraryModule.libraryHolder.library)

	private val noStateChangeInterference: Boolean get() =
		operation != Change || !commandManager.canUndo()

	/**
	 * Called by [AbstractLibraryAction] when the selection in [LibraryTreeViewSwing] has changed.
	 * Subclasses can overwrite this method in order to update their state, such as their selection state.
	 * This implementation is empty.
	 */
	protected open fun handleSelectionChanged() {
		// empty
	}
}

/** An [Action] that is only enabled if the selected item is a [LibraryDirectory].*/
abstract class AbstractLibraryFolderAction(
	actionBaseName: String,
	operation: Operation,
	controller: LibraryTreeViewController
) : AbstractLibraryAction(actionBaseName, operation, controller) {

	val selectedFolder: LibraryDirectory get() = selectedItem as LibraryDirectory

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is LibraryDirectory
}

/** An [Action] that is only enabled if the selected item is a [ContainerLibraryElement].*/
abstract class AbstractContainerLibraryElementAction(
	actionBaseName: String,
	operation: Operation,
	controller: LibraryTreeViewController
) : AbstractLibraryAction(actionBaseName, operation, controller) {

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is ContainerLibraryElement
}
