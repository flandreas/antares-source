package ch.scorpion.antares

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.graph.library.*

class AntaresLibraryFactory : LibraryFactory {

	override fun createEmptyLibrary(properties: LibraryProperties): Library {
		return LibraryImpl(properties, libraryService = LibraryModule.libraryService)
	}

	override fun createBaseLibrary(properties: LibraryProperties): Library {
		val library = createEmptyLibrary(properties)
		AntaresViewModule.fillBaseElementLibrary(library)
		return library
	}
}