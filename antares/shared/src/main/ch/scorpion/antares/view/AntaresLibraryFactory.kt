package ch.scorpion.antares.view

import ch.scorpion.antares.view.module.AntaresViewModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.library.*

class AntaresLibraryFactory : LibraryFactory {

	override fun createEmptyLibrary(properties: LibraryProperties, importedLibraryId: LibraryIdentification?): Library {
		val library = LibraryImpl(properties, libraryService = LibraryModule.libraryService)
		library.author = EditAuthModule.userHolder.user.identity
		importedLibraryId?.let { library.addImport(it.uuid) }
		return library
	}

	override fun createBaseLibrary(properties: LibraryProperties): Library {
		val library = createEmptyLibrary(properties)
		AntaresViewModule.fillBaseElementLibrary(library)
		return library
	}
}