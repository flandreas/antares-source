package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.UUID
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.library.dictionary.*
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph.library] module.
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