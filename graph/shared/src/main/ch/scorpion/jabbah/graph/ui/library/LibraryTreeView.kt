package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.CurrentSavableEvent
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.PreferencesChangedEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.view.GraphElementView

enum class LibraryTreeViewType {
	Main,
	CompositionSource,
	CompositionDestination
}

/** Posted on a [LibraryTreeViewController]'s [EventBus] if the current selection in its [LibraryTreeView] has changed.*/
data class LibrarySelectionChangedEvent(val controller: LibraryTreeViewController)

interface LibraryTreeView : UIView {

	val folderOfSelectedItem: LibraryDirectory?

	fun refresh()

	/**
	 * Expand the tree to the node that contains the opened [ContainerLibraryElement].
	 * This is primarily needed when the request originates from opening a [Project].
	 */
	fun expandTo(element: ContainerLibraryElement)

	/**
	 * Expands the tree to (and inclusive) the [LibraryFolder] with the specified english name.
	 * Does nothing if the displayed [Library] doesn't contain such a folder.
	 */
	fun expandFolder(folderName: String)

	fun expandToCurrentSavable()

	fun expandAllFromSelection()

	fun collapseAtSelection()

	fun openMainLibrary(library: Library)

	fun closeMainLibrary()

	fun handle(event: LibraryItemAddedEvent)

	fun handle(event: LibraryItemRemovedEvent)

	/**
	 * Updates the user object of the tree node that contains the updated [LibraryItem] with the new one.
	 * This is necessary to reflect the possibly changed [LibraryItem] name in the tree node.
	 */
	fun handle(event: LibraryItemUpdatedEvent)

	fun handle(event: LibraryItemMovedEvent)

	fun handle(event: LibraryDirectoryRenamedEvent)

	fun handle(event: ContainerLibraryElementRenamedEvent)
}

/**
 * Displays the current [Library] and its imported [Libraries][Library] as a tree.
 *
 * Instances of this class post the following events on the [EventBus]:
 * - A [LibrarySelectionChangedEvent] when the user selects a [LibraryItem]
 */
class LibraryTreeViewController (
	val type: LibraryTreeViewType,
	library: Library?,
	val applicationModeHolder: ApplicationModeHolder,
	val eventBus: EventBus = BaseModule.eventBus
) : AbstractUIController<LibraryTreeView>() {

	companion object {
		private val LOG by logger(LibraryTreeViewController::class)
	}

	private val preferencesChangedHandler: EventHandler<PreferencesChangedEvent> = { view.refresh() }

	private val libraryItemAddedHandler: EventHandler<LibraryItemAddedEvent> = {
		if (displaysLibrary(it.item.library)) { view.handle(it) }
	}

	private val libraryItemRemovedHandler: EventHandler<LibraryItemRemovedEvent> = {
		if (displaysLibrary(it.parent.library)) { view.handle(it) }
	}

	private val libraryItemUpdatedHandler: EventHandler<LibraryItemUpdatedEvent> = {
		if (displaysLibrary(it.item.library)) { view.handle(it) }
	}

	private val libraryItemMovedHandler: EventHandler<LibraryItemMovedEvent> = {
		if (displaysLibrary(it.item.library)) { view.handle(it) }
	}

	private val libraryItemDirectoryRenamedHandler: EventHandler<LibraryDirectoryRenamedEvent> = {
		if (displaysLibrary(it.directory.library)) { view.handle(it) }
	}

	private val openContainerLibraryElementHandler: EventHandler<OpenContainerLibraryElementRequest> = {
		if (displaysLibrary(it.element.library)) { view.expandTo(it.element) }
	}

	private val renameContainerLibraryElementHandler: EventHandler<ContainerLibraryElementRenamedEvent> = {
		if (displaysLibrary(it.element.library)) { view.handle(it) }
	}

	private val currentSavableHandler: EventHandler<CurrentSavableEvent> = {
		currentSavable = if (it.savable is AbstractLibraryItemSavable) {
			it.savable
		} else {
			null
		}
	}

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		if (it.source === applicationModeHolder) {
			updateActive()
		}
	}

	private val libraryImportsHandler: EventHandler<LibraryImportsEvent> = {
		if (displaysLibrary(it.library)) {
			view.openMainLibrary(it.library)
		}
	}

	var active: Boolean = applicationModeHolder.currentMode.isEdit()
		private set(value) {
			if (field != value) {
				field = value
				view.refresh()
			}
		}

	/** Holds the [Library] to display.*/
	var library: Library? = library
		set(value) {
			if (field !== value) {
				field = value
				if (field != null) {
					view.openMainLibrary(value!!)
				} else {
					view.closeMainLibrary()
				}
			}
		}

	var currentSavable: Savable? = null
		private set(value) {
			if (field != value) {
				field = value
				view.refresh()
			}
		}

	/** Set by [LibraryTreeView] whenever the selection has changed. */
	var selectedItem: LibraryItem? = null
		set(value) {
			if (field !== value) {
				field = value
				LOG.trace("Selected TreeNode '${field.toString()}'")
				eventBus.post(LibrarySelectionChangedEvent(this))
			}
		}

	init {
		eventBus.register(PreferencesChangedEvent::class, preferencesChangedHandler)
		eventBus.register(LibraryItemAddedEvent::class, libraryItemAddedHandler)
		eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
		eventBus.register(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)
		eventBus.register(LibraryItemMovedEvent::class, libraryItemMovedHandler)
		eventBus.register(LibraryDirectoryRenamedEvent::class, libraryItemDirectoryRenamedHandler)
		eventBus.register(ContainerLibraryElementRenamedEvent::class, renameContainerLibraryElementHandler)
		eventBus.register(LibraryImportsEvent::class, libraryImportsHandler)

		eventBus.register(CurrentSavableEvent::class, currentSavableHandler)
		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(OpenContainerLibraryElementRequest::class, openContainerLibraryElementHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(preferencesChangedHandler)
		eventBus.unregister(libraryItemAddedHandler)
		eventBus.unregister(libraryItemRemovedHandler)
		eventBus.unregister(libraryItemUpdatedHandler)
		eventBus.unregister(libraryItemMovedHandler)
		eventBus.unregister(libraryItemDirectoryRenamedHandler)
		eventBus.unregister(renameContainerLibraryElementHandler)
		eventBus.unregister(libraryImportsHandler)

		eventBus.unregister(currentSavableHandler)
		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(openContainerLibraryElementHandler)
	}

	override fun onViewInitialized() {
		updateActive()
	}

	private fun updateActive() {
		active = applicationModeHolder.currentMode.isEdit()
	}

	fun isCurrentItem(item: LibraryItem): Boolean =
		currentSavable is AbstractLibraryItemSavable && (currentSavable as AbstractLibraryItemSavable).item == item

	fun isDefaultElement(element: ContainerLibraryElement): Boolean =
		element.library?.defaultElementUUID == element.uuid

	fun allowMove(item: LibraryItem, destination: LibraryDirectory): Boolean {
		if (!canEdit(destination.library!!) || !canEdit(item.library!!)) {
			return false
		}
		if (item is LibraryElement) {
			return true
		}
		if (item is LibraryFolder) {
			// Moving LibraryFolders between Libraries not yet supported
			return item !== destination && item.library === destination.library
		}
		return false
	}

	private fun canEdit(library: Library): Boolean =
		Authorizer.isCurrentUserAuthorizedTo(Operation.Change, library)

	/**
	 * Creates a new [GraphElementView] instance from the currently selected [LibraryElement]
	 * to be dragged and dropped
	 * @return `null` if nothing is selected, or the selected [LibraryItem] isn't a [LibraryElement]
	 */
	fun createTransferableGraphElementView(): GraphElementView<GraphElement>? {
		if (selectedItem == null || selectedItem !is LibraryElement) {
			return null
		}
		return (selectedItem as LibraryElement).getNewInstance()
	}

	private fun displaysLibrary(library: Library?): Boolean =
		library != null && this.library?.expandedImports?.contains(library.uuid) == true
}

