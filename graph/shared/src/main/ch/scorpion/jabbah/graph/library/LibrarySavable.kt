package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class LibrarySavable(
    val metaGraph: MetaGraph,
    val element: ContainerLibraryElement,
    private val library: Library = LibraryModule.libraryHolder.library,
    private val service: LibraryService = LibraryModule.libraryService.invoke(),
    private val eventBus: EventBus = BaseModule.eventBus
) : Savable {

	/** ---- [Savable] */

    override val description: String get() = "${Translations.getString("library.savable.prefix")} \"${element.name}\""

    override val defined: Boolean get() = true

    override val supportsMostRecent: Boolean get() = true

    override fun open(application: Application): Boolean {
        eventBus.post(OpenContainerLibraryElementRequest(element))
	    return true
    }

    override fun save(application: Application): Boolean {
	    service.updateContainerLibraryElement(library, metaGraph, element)
        return true
    }

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (other !is LibrarySavable) {
			return false
		}
		return library.name == other.library.name && element.uuid == other.element.uuid
	}
}