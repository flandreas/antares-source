package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.library.dictionary.*
import io.antarescircuit.jabbah.graph.model.image.ImageLibraryElement
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.TypeMap

/**
 * Module definitions for the [io.antarescircuit.jabbah.graph.library] module.
 */
object LibraryModule : AbstractModule() {

	lateinit var DEF_LIBRARY_UUID: UUID

	var libraryFactory: LibraryFactory = GraphLibraryFactory()

	var libraryHolder: LibraryHolder = LibraryHolderImpl()
		set(value) {
			field = value
			EditModule.imageRepository = field
		}

	var userLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var systemLibraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	val libraryServiceCallbacks = mutableListOf<LibraryServiceCallback>()

	var libraryService: LibraryService = LibraryService()

	var baseLibraryElementRepository: BaseLibraryElementRepository = BaseLibraryElementRepository()

	var userLibraryDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	var systemLibraryDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	lateinit var libraryManagementService: LibraryManagementService

	override fun initialize() {
		configureTypeMap(IOModule.typeMap)
		EditModule.imageRepository = libraryHolder
	}

	override fun resetDependencies() {}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("library", LibraryImpl::class)
		typeMap.register("baseLibraryElement", BaseLibraryElement::class)
		typeMap.register("libraryFolder", LibraryFolder::class)
		typeMap.register("containerLibraryElement", ContainerLibraryElement::class)
		typeMap.register("libraryDictionary", LibraryDictionary::class)
		typeMap.register("libraryDictionaryEntry", LibraryDictionaryEntry::class)
		typeMap.register("imageLibraryElement", ImageLibraryElement::class)
	}
}