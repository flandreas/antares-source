package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.CurrentLibraryEvent
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	private val managementService: ProjectManagementService = ProjectModule.projectManagementService.invoke(),
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction("project.action.close") {

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