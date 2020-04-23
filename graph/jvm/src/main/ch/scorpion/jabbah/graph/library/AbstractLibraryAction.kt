package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import javax.swing.Action
import javax.swing.tree.DefaultMutableTreeNode

/**
 * A base class for implementing [Action]s that operate on items of [LibraryTreeView].
 * Listens for [LibrarySelectionChangedEvent]s and remembers the source [LibraryTreeView].
 */
abstract class AbstractLibraryAction(
	actionBaseName: String,
	protected val libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractApplicationModeEditAction(actionBaseName, eventBus = eventBus) {

	private val librarySelectionChangeHandler: EventHandler<LibrarySelectionChangedEvent> = {
		if (it.libraryTreeView === libraryTreeView) {
			updateEnabledness()
			handleSelectionChanged()
		}
	}

	protected val selectedItem: LibraryItem? get() = libraryTreeView.getSelectedItem()

	protected val folderOfSelectedItem: LibraryDirectory?
		get() = (libraryTreeView.selectionPath?.parentPath?.lastPathComponent as DefaultMutableTreeNode?)?.userObject as LibraryDirectory?

	/** The selectedItem might be `null` during boot-strap.*/
	protected val isLibraryOwnedByUser: Boolean get() = selectedItem == null || AppModule.userHolder.user.uuid == selectedItem?.library?.author

	init {
		enabled = false
		eventBus.register(LibrarySelectionChangedEvent::class, librarySelectionChangeHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(librarySelectionChangeHandler)
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
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, eventBus) {

	val selectedFolder: LibraryDirectory get() = selectedItem as LibraryDirectory

	override fun calculateEnabledness(): Boolean = selectedItem is LibraryDirectory
}

/** An [Action] that is only enabled if the selected item is a [ContainerLibraryElement].*/
abstract class AbstractContainerLibraryElementAction(
	actionBaseName: String,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, eventBus) {

	override fun calculateEnabledness(): Boolean = selectedItem is ContainerLibraryElement
}

/** An [Action] that is only enabled if the selected item is a [BaseLibraryElement].*/
abstract class AbstractBaseLibraryElementAction(
	actionBaseName: String,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, libraryTreeView, eventBus) {

	override fun calculateEnabledness(): Boolean = selectedItem is BaseLibraryElement
}