package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.edit.auth.UserHolder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class LibrarySavable(
	element: ContainerLibraryElement,
	val library: Library = LibraryModule.libraryHolder.library,
	val service: LibraryService = LibraryModule.libraryService,
	private val userHolder: UserHolder = EditAuthModule.userHolder,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibrarySavable(element, service) {

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("library.savable.prefix")} \"${element.name.value}\""

	override val readOnly: Boolean get() = library.author != userHolder.user.uuid

	override fun open(application: Application): Boolean {
		eventBus.post(OpenContainerLibraryElementRequest(element))
		return true
	}

	override fun save(application: Application): Boolean {
		service.updateContainerLibraryElement(library, element)
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