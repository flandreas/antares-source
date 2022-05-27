package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
class LibrarySavable(
	element: ContainerLibraryElement,
	val library: Library = LibraryModule.libraryHolder.library,
	service: LibraryService = LibraryModule.libraryService,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementSavable(element, service) {

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("library.savable.prefix")} \"${element.name.value}\""

	override val editable: Boolean get() = Authorizer.isCurrentUserAuthorizedTo(Change, library)

	override fun open(application: Application): Boolean {
		eventBus.post(OpenContainerLibraryElementRequest(element))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		libraryService.updateContainerLibraryElement(library, appDataViewController.data!!.content as MetaGraph, element)
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