package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.graph.library.CloseLibraryAction
import io.antarescircuit.jabbah.graph.library.LibraryHolder
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * Closes the currently open [Project].
 */
class CloseProjectAction(
	managementService: ProjectManagementService = ProjectModule.projectManagementService,
	libraryHolder: LibraryHolder = LibraryModule.libraryHolder,
	controller: LibraryTreeViewController,
) : CloseLibraryAction("project.action.close", managementService, libraryHolder, controller)
