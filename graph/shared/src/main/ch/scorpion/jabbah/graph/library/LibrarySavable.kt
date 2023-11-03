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
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementSavable(element) {

	private val library: Library get() = element.library!!

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("library.savable.prefix")} \"${element.name.value}\""

	override val editable: Boolean get() = element.library != null && Authorizer.isCurrentUserAuthorizedTo(Change, library)

	override fun open(application: Application): Boolean {
		if (element.library == null) {
			// Library has been disposed in the meantime
			if (LibraryModule.libraryHolder.l == null) {
				return false
			}
			LibraryModule.libraryHolder.getContainerLibraryElement(element.uuid)?.let {
				eventBus.post(OpenContainerLibraryElementRequest(it))
				return true
			} ?: return false
		}

		eventBus.post(OpenContainerLibraryElementRequest(element))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		library.libraryService.updateContainerLibraryElement(library, appDataViewController.data!!.content as MetaGraph, element)
		return true
	}

	/** ---- [Any] */

	override fun equals(other: Any?): Boolean {
		if (other !is LibrarySavable) {
			return false
		}
		if (element.library == null) {
			return false
		}
		return element.uuid == other.element.uuid
	}
}