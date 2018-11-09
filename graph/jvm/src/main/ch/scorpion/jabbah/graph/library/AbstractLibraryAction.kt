package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import javax.swing.Action
import javax.swing.tree.DefaultMutableTreeNode

/**
 * A base class for implementing [Action]s that operate on items of [LibraryTreeView].
 * Listens for [LibrarySelectionChangedEvent]s and remembers the source [LibraryTreeView].
 */
abstract class AbstractLibraryAction(
	actionBaseName: String,
	protected val eventBus: EventBus
) : AbstractAction(actionBaseName) {

	protected var libraryTreeView: LibraryTreeView? = null
		private set

	protected val selectedItem: LibraryItem? get() = libraryTreeView?.getSelectedItem()

	protected val folderOfSelectedItem: LibraryDirectory? get() =
		(libraryTreeView!!.selectionPath.parentPath.lastPathComponent as DefaultMutableTreeNode).userObject as LibraryDirectory?

	init {
		enabled = false
		eventBus.register(LibrarySelectionChangedEvent::class) {
			libraryTreeView = it.libraryTreeView
			updateEnabledness()
			handleSelectionChanged()
		}
	}

	protected fun updateEnabledness() {
		enabled = calculateEnabledness()
	}

	/**
	 * Called by [AbstractLibraryAction] when the selection in [LibraryTreeView] has changed.
	 * Subclasses can overwrite this method in order to update their state, such as their selection state.
	 * This implementation is empty.
	 */
	protected open fun handleSelectionChanged() {
		// empty
	}

	protected abstract fun calculateEnabledness(): Boolean
}

/** An [Action] that is only enabled if the selected item is a [LibraryDirectory].*/
abstract class AbstractLibraryFolderAction(
	actionBaseName: String,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, eventBus) {

	override fun calculateEnabledness(): Boolean = selectedItem is LibraryDirectory
}

/** An [Action] that is only enabled if the selected item is a [ContainerLibraryElement].*/
abstract class AbstractContainerLibraryElementAction(
	actionBaseName: String,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, eventBus) {

	override fun calculateEnabledness(): Boolean = selectedItem is ContainerLibraryElement
}

/** An [Action] that is only enabled if the selected item is a [BaseLibraryElement].*/
abstract class AbstractBaseLibraryElementAction(
	actionBaseName: String,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, eventBus) {

	override fun calculateEnabledness(): Boolean = selectedItem is BaseLibraryElement
}