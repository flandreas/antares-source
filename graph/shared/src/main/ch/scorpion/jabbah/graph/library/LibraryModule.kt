package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.io.IOModule
import ch.scorpion.jabbah.io.TypeMap

/**
 * Module definitions for the [ch.scorpion.jabbah.graph.library] module.
 */
object LibraryModule : AbstractModule() {

    var libraryFactory: () -> Library = {throw UnsupportedOperationException()}

    var libraryHolder: LibraryHolder = LibraryHolder()

    var libraryService: LibraryService = UnimplementedLibraryService()

    override fun initialize() {
        configureTypeMap(IOModule.typeMap)
    }

    private fun configureTypeMap(typeMap: TypeMap) {
        typeMap.register("baseLibraryElement", BaseLibraryElement::class)
        typeMap.register("libraryFolder", LibraryFolder::class)
        typeMap.register("containerLibraryElement", ContainerLibraryElement::class)
    }
}