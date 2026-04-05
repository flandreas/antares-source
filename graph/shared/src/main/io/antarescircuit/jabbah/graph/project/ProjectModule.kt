package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.library.dictionary.LibraryDictionaryService
import io.antarescircuit.jabbah.graph.library.dictionary.UnimplementedLibraryDictionaryPersistenceService
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap

/**
 * Module definitions the [io.antarescircuit.jabbah.graph.project] module.
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