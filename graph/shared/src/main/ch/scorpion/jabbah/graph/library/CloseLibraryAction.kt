package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Closes the currently open [Library]
 */
open class CloseLibraryAction(
	baseName: String = "library.action.close",
	private val managementService: AbstractLibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	controller: LibraryTreeViewController,
) : AbstractLibraryAction(baseName, Operation.View, controller) {

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { updateEnabled() }

	init {
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		updateEnabled()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentLibraryHandler)
	}

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled() && selectedItem === libraryHolder.l

	override fun execute(event: ActionEvent) {
		managementService.close()
	}
}
