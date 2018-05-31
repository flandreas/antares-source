package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class LibrarySavable(val metaGraph: MetaGraph, val element: ContainerLibraryElement) : Savable {

    override val description: String
        get() = "${Translations.getString("library.savable.prefix")} \"${element.name}\""

    override val defined: Boolean get() = true

    override val supportsMostRecent: Boolean get() = false

    override fun open(application: Application): Boolean {
        throw UnsupportedOperationException("cannot open LibrarySavable in Application: not supported")
    }

    override fun save(application: Application): Boolean {
        element.saveMetaGraph(metaGraph)
        return true
    }
}