package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.model.port.InconsistentNetError

open class GraphLibraryFactory : LibraryFactory {

    override fun createEmptyLibrary(properties: LibraryProperties, importedLibraryId: LibraryIdentification?): Library {
        val library = LibraryImpl(properties)
        library.author = EditAuthModule.userHolder.user.identity
        importedLibraryId?.let { library.addImport(it.uuid) }
        fillPreferences(library)
        return library
    }

    override fun createBaseLibrary(properties: LibraryProperties): Library =
        createEmptyLibrary(properties)

    override fun fillPreferences(library: Library) {
        library.preferences.set(InconsistentNetError.PROP_ALLOWED_DURATION, BaseModule.properties.getInt(InconsistentNetError.PROP_ALLOWED_DURATION))
    }
}