package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * Closes the currently open [Library]
 */
open class CloseLibraryAction(
	baseName: String = "library.action.close",
	private val managementService: AbstractLibraryManagementService = LibraryModule.libraryManagementService,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(baseName) {

	private val currentLibraryHandler: EventHandler<CurrentLibraryEvent> = { updateEnabledness() }

	init {
		eventBus.register(CurrentLibraryEvent::class, currentLibraryHandler)
		updateEnabledness()
	}

	override fun dispose() {
		super.dispose()
		eventBus.unregister(currentLibraryHandler)
	}

	private fun updateEnabledness() {
		enabled = libraryHolder.l != null
	}

	override fun execute(event: ActionEvent) {
		managementService.close()
	}
}