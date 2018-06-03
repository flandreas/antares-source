package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.*

/**
 * Module definitions the the [ch.scorpion.jabbah.graph.project] module.
 */
object ProjectModule : AbstractModule() {

	var projectHolder: ProjectHolder = ProjectHolder()

	var projectLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var projectLibraryService: () -> LibraryService = { LibraryServiceImpl(persistenceService = projectLibraryPersistenceService) }

	var projectService: ProjectService = UnimplementedProjectService()

	val projectFactory: (String) -> Project = { LibraryImpl(name = it, libraryService = projectLibraryService.invoke(), descriptionKey = "project.project.name") }

	override fun initialize() {
		// empty
	}
}