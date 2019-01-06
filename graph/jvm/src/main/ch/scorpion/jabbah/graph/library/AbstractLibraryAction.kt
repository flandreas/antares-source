package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.module.AppModule
import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.graph.ApplicationMode
import ch.scorpion.jabbah.graph.ApplicationModeEvent
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

	/** The selectedItem might be `null` during boot-strap.*/
	protected val isLibraryOwnedByUser: Boolean get() = selectedItem == null || AppModule.userHolder.user.uuid == selectedItem?.library?.author

	protected var applicationMode: ApplicationMode = ApplicationMode.EDIT
		set(value) {
			if (value != field) {
				field = value
				updateEnabledness()
			}
		}

	init {
		enabled = true
		eventBus.register(LibrarySelectionChangedEvent::class) {
			libraryTreeView = it.libraryTreeView
			updateEnabledness()
			handleSelectionChanged()
		}
		eventBus.register(ApplicationModeEvent::class) {
			applicationMode = it.applicationMode
		}
	}

	private fun updateEnabledness() {
		enabled = applicationMode == ApplicationMode.EDIT && calculateEnabledness()
	}

	/**
	 * Called by [AbstractLibraryAction] when the selection in [LibraryTreeView] has changed.
	 * Subclasses can overwrite this method in order to update their state, such as their selection state.
	 * This implementation is empty.
	 */
	protected open fun handleSelectionChanged() {
		// empty
	}

	/** Implemented by subclasses to further decide whether this [Action] should be enabled*/
	protected abstract fun calculateEnabledness(): Boolean
}

/** An [Action] that is only enabled if the selected item is a [LibraryDirectory].*/
abstract class AbstractLibraryFolderAction(
	actionBaseName: String,
	eventBus: EventBus
) : AbstractLibraryAction(actionBaseName, eventBus) {

	val selectedFolder: LibraryDirectory get() = selectedItem as LibraryDirectory

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