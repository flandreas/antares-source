package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.app.ApplicationMode.EDIT
import ch.scorpion.jabbah.graph.app.ApplicationModeObserver
import ch.scorpion.jabbah.graph.ui.library.LibrarySelectionChangedEvent
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeView
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * An [AbstractAction] that is only enabled if [LibraryHolder] has an open [Library].
 */
abstract class AbstractCurrentLibraryAction(
	actionBaseName: String,
	protected val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(actionBaseName) {

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { updateEnabledness() }

	init {
	    eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		// Must not call updateEnabledness()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentLibraryHandler)
	}

	protected fun updateEnabledness() {
		enabled = calculateEnabledness()
	}

	protected open fun calculateEnabledness(): Boolean = LibraryModule.libraryHolder.l != null
}

/**
 * A base class for implementing [Action]s that operate on items of [LibraryTreeView].
 * Listens for [LibrarySelectionChangedEvent]s and remembers the source [LibraryTreeView].
 */
abstract class AbstractLibraryAction(
	actionBaseName: String,
	protected val operation: Operation,
	protected val controller: LibraryTreeViewController,
	onlyEnabledInEditMode: Boolean = true,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractCurrentLibraryAction(actionBaseName, controller.eventBus) {

	companion object {
		fun isAuthorized(operation: Operation, target: Any?): Boolean =
			target != null && Authorizer.isCurrentUserAuthorizedTo(operation, target)
	}

	private val applicationModeObserver: ApplicationModeObserver? = if (onlyEnabledInEditMode) {
		ApplicationModeObserver(controller.applicationModeHolder, controller.eventBus) {
			updateEnabledness()
		}
	} else null

	private val librarySelectionChangeHandler: EventHandler<LibrarySelectionChangedEvent> = {
		if (it.controller === controller) {
			updateEnabledness()
			handleSelectionChanged()
		}
	}

	private val commandEventHandler: EventHandler<CommandEvent> = { updateEnabledness() }

	protected val selectedItem: LibraryItem? get() = controller.selectedItem

	protected val folderOfSelectedItem: LibraryDirectory? get() = controller.view.folderOfSelectedItem

	init {
		eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionChangeHandler)
		eventBus.register(CommandEvent::class, commandEventHandler)
		updateEnabledness()
	}

	override fun dispose() {
		super.dispose()
		applicationModeObserver?.dispose()
		eventBus.unregister(librarySelectionChangeHandler)
		eventBus.unregister(commandEventHandler)
	}

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness()
			&& noStateChangeInterference
			&& operationAuthorized
			&& (applicationModeObserver == null || applicationModeObserver.currentMode == EDIT)

	protected open val operationAuthorized: Boolean get() = isAuthorized(operation, LibraryModule.libraryHolder.l)

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
	controller: LibraryTreeViewController,
	onlyEnabledInEditMode: Boolean = true
) : AbstractLibraryAction(actionBaseName, operation, controller, onlyEnabledInEditMode) {

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is ContainerLibraryElement
}
