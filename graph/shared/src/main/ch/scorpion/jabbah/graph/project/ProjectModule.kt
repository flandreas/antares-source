package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
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

	var projectLibraryService: () -> LibraryService = { LibraryService(userLibraryPersister = projectLibraryPersistenceService) }

	var projectDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	lateinit var projectManagementService: ProjectManagementService

	val projectFactory: (TranslatableText) -> Project = { ProjectImpl(name = it, libraryService = projectLibraryService.invoke(), objectTypeKey = "project.project.name") }

	override fun initialize() {
		configureTypeMap(IOModule.typeMap)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("project", ProjectImpl::class)
	}
}