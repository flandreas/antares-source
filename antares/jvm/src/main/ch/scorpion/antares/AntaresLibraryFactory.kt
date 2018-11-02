package ch.scorpion.antares

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.graph.library.*

class AntaresLibraryFactory : LibraryFactory {

	override fun createEmptyLibrary(name: String): Library {
		return LibraryImpl(name, libraryService = LibraryModule.libraryService.invoke())
	}

	override fun createBaseLibrary(name: String): Library {
		val library = createEmptyLibrary(name)
		AntaresViewModule.fillBaseElementLibrary(library)
		return library
	}
}