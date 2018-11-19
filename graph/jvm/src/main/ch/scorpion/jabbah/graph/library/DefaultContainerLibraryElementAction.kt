package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule

/**
 * An [Action] for marking a [ContainerLibraryElement] as the default one, i.e. the one to be
 * opened when the [Library] is loaded.
 */
class DefaultContainerLibraryElementAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction("library.action.setDefaultElement", eventBus) {

	override fun execute(event: ActionEvent) {
		val elem = libraryTreeView!!.getSelectedItem() as ContainerLibraryElement
		val library = elem.library!!
		if (selected) {
			library.libraryService.setDefaultElement(library, elem.uuid)
		} else {
			library.libraryService.setDefaultElement(library, null)
		}
	}

	override fun handleSelectionChanged() {
		selected = if (libraryTreeView!!.getSelectedItem() is ContainerLibraryElement) {
			val elem = libraryTreeView!!.getSelectedItem() as ContainerLibraryElement
			elem.library?.defaultElementUUID == elem.uuid
		} else {
			false
		}
	}

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
	}
}