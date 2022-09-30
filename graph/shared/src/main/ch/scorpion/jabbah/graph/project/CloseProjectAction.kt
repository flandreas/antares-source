package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.library.CloseLibraryAction
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	managementService: ProjectManagementService = ProjectModule.projectManagementService.invoke(),
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	eventBus: EventBus = BaseModule.eventBus
) : CloseLibraryAction("project.action.close", managementService, libraryHolder, eventBus)
