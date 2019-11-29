package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.UnimplementedLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions the the [ch.scorpion.jabbah.graph.project] module.
 */
object ProjectModule : AbstractModule() {

	var projectHolder: ProjectHolder = ProjectHolder()

	var projectLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var projectLibraryService: () -> LibraryService = {
		LibraryService(
			libraryAccessor = { ProjectModule.projectHolder.project as Library? },
			persistenceService = projectLibraryPersistenceService)
	}

	var projectDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	var projectManagementService: ProjectManagementService = UnimplementedProjectManagementService()

	val projectFactory: (String) -> Project = { ProjectImpl(name = it, libraryService = projectLibraryService.invoke(), descriptionKey = "project.project.name") }

	override fun initialize() {
		configureTypeMap(IOModule.typeMap)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("project", ProjectImpl::class)
	}
}