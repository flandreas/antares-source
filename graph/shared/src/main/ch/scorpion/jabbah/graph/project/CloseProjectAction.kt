package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.graph.library.CloseLibraryAction
import ch.scorpion.jabbah.graph.library.LibraryHolder
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	managementService: ProjectManagementService = ProjectModule.projectManagementService,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	controller: LibraryTreeViewController,
) : CloseLibraryAction("project.action.close", managementService, libraryHolder, controller)
