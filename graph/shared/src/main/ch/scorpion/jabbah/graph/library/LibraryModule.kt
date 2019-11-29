package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.graph.library.dictionary.*
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph.library] module.
 */
object LibraryModule : AbstractModule() {

	var libraryFactory: LibraryFactory = UnimplementedLibraryFactory()

	var libraryHolder: LibraryHolder = LibraryHolder()

	var libraryPersistenceService: LibraryPersistenceService = UnimplementedLibraryPersistenceService()

	var libraryService: () -> LibraryService = { LibraryService() }

	var baseLibraryElementRepository: BaseLibraryElementRepository = BaseLibraryElementRepository()

	var libraryDictionaryService: LibraryDictionaryService = LibraryDictionaryService(UnimplementedLibraryDictionaryPersistenceService())

	var libraryManagementService: LibraryManagementService = UnimplementedLibraryManagementService()

	override fun initialize() {
		configureTypeMap(IOModule.typeMap)
	}

	private fun configureTypeMap(typeMap: TypeMap) {
		typeMap.register("library", LibraryImpl::class)
		typeMap.register("baseLibraryElement", BaseLibraryElement::class)
		typeMap.register("libraryFolder", LibraryFolder::class)
		typeMap.register("containerLibraryElement", ContainerLibraryElement::class)
		typeMap.register("libraryDictionary", LibraryDictionary::class)
		typeMap.register("libraryDictionaryEntry", LibraryDictionaryEntry::class)
	}
}