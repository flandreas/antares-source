package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.library.*
import ch.scorpion.jabbah.graph.library.dictionary.LibraryDictionaryService
import ch.scorpion.jabbah.graph.library.dictionary.UnimplementedLibraryDictionaryPersistenceService
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions the [ch.scorpion.jabbah.graph.project] module.
 */
object ProjectModule : AbstractModule() {

	var projectLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var projectLibraryService: LibraryService =
		LibraryService(userLibraryPersisterProvider = { projectLibraryPersistenceService })

	var projectDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	lateinit var projectManagementService: ProjectManagementService

	val projectFactory: (TranslatableText) -> Project = {
		val project = ProjectImpl(name = it, objectTypeKey = "project.project.name")
		project.author = EditAuthModule.userHolder.user.identity
		project
	}

	override fun initialize() {
		configureTypeMap(IOModule.typeMap)
	}

	override fun resetDependencies() {}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("project", ProjectImpl::class)
	}
}