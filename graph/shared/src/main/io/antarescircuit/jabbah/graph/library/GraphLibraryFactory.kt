package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.EditAuthModule
import io.antarescircuit.jabbah.graph.model.port.InconsistentNetError

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