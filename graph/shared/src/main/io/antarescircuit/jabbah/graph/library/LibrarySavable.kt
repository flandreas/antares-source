package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.app.Savable
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.graph.MetaGraph

/**
 * Saves the edited [MetaGraph] of a [ContainerLibraryElement] in the containing [LibraryDirectory].
 */
open class LibrarySavable(
	element: ContainerLibraryElement,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementSavable(element) {

	private val library: Library get() = element.library!!

	/** ---- [Savable] */

	override val description: String get() = "${Translations.getString("graph.savable.prefix")} \"${element.name.value}\""

	override fun open(application: Application): Boolean {
		if (element.library == null) {
			// Library has been disposed in the meantime
			if (LibraryModule.libraryHolder.l == null) {
				return false
			}
			LibraryModule.libraryHolder.getContainerLibraryElement(element.uuid)?.let {
				eventBus.post(OpenContainerLibraryElementRequest(it))
				return true
			}
		}

		eventBus.post(OpenContainerLibraryElementRequest(element))
		return true
	}

	override fun save(appDataViewController: ApplicationDataViewController): Boolean {
		library.libraryService.updateContainerLibraryElement(library, appDataViewController.data!!.content as MetaGraph, element)
		appDataViewController.data = appDataViewController.data!!.withSavable(this)
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