package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.CommandEvent
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import javax.swing.tree.DefaultMutableTreeNode

/**
 * A base class for implementing [Action]s that operate on items of [LibraryTreeView].
 * Listens for [LibrarySelectionChangedEvent]s and remembers the source [LibraryTreeView].
 *
 * @param changesPersistentState `true` if this [Action] changes the persistent state of the [Library],
 * in which case the [Action] is not enabled if the main application data has changed (which could otherwise
 * lead to inconsistent state when undoing/redoing [Command]s).
 */
abstract class AbstractLibraryAction(
	actionBaseName: String,
	protected val libraryTreeView: LibraryTreeView,
	private val changesPersistentState: Boolean,
	eventBus: EventBus,
	private val commandManager: CommandManager = EditModule.commandManager
) : AbstractApplicationModeEditAction(actionBaseName, eventBus = eventBus) {

	private val librarySelectionChangeHandler: EventHandler<LibrarySelectionChangedEvent> = {
		if (it.libraryTreeView === libraryTreeView) {
			updateEnabledness()
			handleSelectionChanged()
		}
	}

	private val commandEventHandler: EventHandler<CommandEvent> = {
		updateEnabledness()
	}

	protected val selectedItem: LibraryItem? get() = libraryTreeView.getSelectedItem()

	protected val folderOfSelectedItem: LibraryDirectory?
		get() = (libraryTreeView.selectionPath?.parentPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject as LibraryDirectory?

	/** The selectedItem might be `null` during boot-strap.*/
	protected val isLibraryOwnedByUser: Boolean get() = selectedItem == null || AppModule.userHolder.user.uuid == selectedItem?.library?.author

	init {
		eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionChangeHandler)
		eventBus.register(CommandEvent::class, commandEventHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(librarySelectionChangeHandler)
		eventBus.unregister(commandEventHandler)
	}

	override fun calculateEnabledness(): Boolean {
		return !changesPersistentState || !commandManager.canUndo()
	}

	/**
	 * Called by [AbstractLibraryAction] when the selection in [LibraryTreeView] has changed.
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
	changesPersistentState: Boolean,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, changesPersistentState, eventBus) {

	val selectedFolder: LibraryDirectory get() = selectedItem as LibraryDirectory

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is LibraryDirectory
}

/** An [Action] that is only enabled if the selected item is a [ContainerLibraryElement].*/
abstract class AbstractContainerLibraryElementAction(
	actionBaseName: String,
	changesPersistentState: Boolean,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, changesPersistentState, eventBus) {

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is ContainerLibraryElement
}

/** An [Action] that is only enabled if the selected item is a [BaseLibraryElement].*/
abstract class AbstractBaseLibraryElementAction(
	actionBaseName: String,
	changesPersistentState: Boolean,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, changesPersistentState, eventBus) {

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && selectedItem is BaseLibraryElement
}