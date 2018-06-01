package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.LibraryPersistenceService
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.LibraryServiceImpl
import ch.scorpion.jabbah.graph.library.UnimplementedLibraryPersistenceService

/**
 * Module definitions the the [ch.scorpion.jabbah.graph.project] module.
 */
object ProjectModule : AbstractModule() {

	var projectLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var projectLibraryService: () -> LibraryService = { LibraryServiceImpl(persistenceService = projectLibraryPersistenceService) }

	var projectService: ProjectService = UnimplementedProjectService()

	override fun initialize() {
		// empty
	}

}