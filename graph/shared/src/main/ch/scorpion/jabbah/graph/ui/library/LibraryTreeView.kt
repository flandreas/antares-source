package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.app.ApplicationModeEvent
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.view.GraphElementView

enum class LibraryTreeViewType {
	Main,
	CompositionSource,
	CompositionDestination
}

/** Posted on a [LibraryTreeViewController]'s [EventBus] if the current selection in its [LibraryTreeView] has changed.*/
data class LibrarySelectionChangedEvent(val controller: BasicLibraryTreeViewController<*>)

interface LibraryTreeView : BasicLibraryTreeView {

	val folderOfSelectedItem: LibraryDirectory?

	fun handle(event: LibraryItemAddedEvent)

	fun handle(event: LibraryItemRemovedEvent)

	/**
	 * Updates the user object of the tree node that contains the updated [LibraryItem] with the new one.
	 * This is necessary to reflect the possibly changed [LibraryItem] name in the tree node.
	 */
	fun handle(event: LibraryItemUpdatedEvent)

	fun handle(event: LibraryItemMovedEvent)

	fun handle(event: LibraryRenamedEvent)
}

/**
 * Displays the current [Library] and its imported [Libraries][Library] as a tree.
 *
 * Instances of this class post the following events on the [EventBus]:
 * - A [LibrarySelectionChangedEvent] when the user selects a [LibraryItem]
 */
class LibraryTreeViewController (
	type: LibraryTreeViewType,
	library: Library?,
	val applicationModeHolder: ApplicationModeHolder,
	private val editor: Editor? = null,
	eventBus: EventBus = BaseModule.eventBus
) : BasicLibraryTreeViewController<LibraryTreeView>(type, library, eventBus) {

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

	private val openContainerLibraryElementHandler: EventHandler<OpenContainerLibraryElementRequest> = {
		if (displaysLibrary(it.element.library)) { view.expandTo(it.element) }
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

	private val libraryRenamedHandler: EventHandler<LibraryRenamedEvent> = {
		if (displaysLibrary(it.library)) {
			view.handle(it)
		}
	}

	var active: Boolean = applicationModeHolder.currentMode.isEdit()
		private set(value) {
			if (field != value) {
				field = value
				view.refresh()
			}
		}

	init {
		eventBus.register(LibraryItemAddedEvent::class, libraryItemAddedHandler)
		eventBus.register(LibraryItemRemovedEvent::class, libraryItemRemovedHandler)
		eventBus.register(LibraryItemUpdatedEvent::class, libraryItemUpdatedHandler)
		eventBus.register(LibraryItemMovedEvent::class, libraryItemMovedHandler)
		eventBus.register(LibraryImportsEvent::class, libraryImportsHandler)
		eventBus.register(LibraryRenamedEvent::class, libraryRenamedHandler)

		eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
		eventBus.register(OpenContainerLibraryElementRequest::class, openContainerLibraryElementHandler)
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(libraryItemAddedHandler)
		eventBus.unregister(libraryItemRemovedHandler)
		eventBus.unregister(libraryItemUpdatedHandler)
		eventBus.unregister(libraryItemMovedHandler)
		eventBus.unregister(libraryImportsHandler)
		eventBus.unregister(libraryRenamedHandler)

		eventBus.unregister(applicationModeHandler)
		eventBus.unregister(openContainerLibraryElementHandler)
	}

	override fun onViewInitialized() {
		updateActive()
	}

	private fun updateActive() {
		active = applicationModeHolder.currentMode.isEdit()
	}

	fun allowMove(item: LibraryItem, destination: LibraryDirectory): Boolean {
		if (!canEdit(destination.library!!) || !canEdit(item.library!!)) {
			return false
		}
		if (item is ImageLibraryElement) {
			// Moving images between Libraries not yet supported
			return item !== destination && item.library === destination.library
		}
		if (item is LibraryElement) {
			return true
		}
		if (item is LibraryFolder) {
			// Moving LibraryFolders between Libraries not yet supported
			return item !== destination && item.library === destination.library
		}
		if (item is UndoableStateLibraryItem<*>) {
			return true
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
		val newInstance: GraphElementView<GraphElement>? = (selectedItem as LibraryElement).getNewInstance()
		newInstance?.let {
			if (editor != null) {
				EditModule.drawingAppService.customizeAddedComponent(it, editor.view.drawing)
			}
		}
		return newInstance
	}

	fun renameContainerLibraryElement(element: ContainerLibraryElement, newName: String) {
		element.library!!.libraryService.renameContainerLibraryElement(element, TranslatableText(newName))
	}
}

