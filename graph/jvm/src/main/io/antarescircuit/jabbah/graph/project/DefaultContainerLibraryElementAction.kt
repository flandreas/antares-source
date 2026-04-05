package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController

/**
 * An [Action] for marking a [ContainerLibraryElement] as the default one, i.e. the one to be
 * opened when the [Project] is loaded.
 */
class DefaultContainerLibraryElementAction(
	controller: LibraryTreeViewController,
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.setDefaultElement",
	operation = Operation.Change,
	controller
) {

	override fun execute(event: ActionEvent) {
		val elem = controller.selectedItem as ContainerLibraryElement
		val library = elem.library!!
		if (selected) {
			library.libraryService.setDefaultElement(library, elem.uuid)
		} else {
			library.libraryService.setDefaultElement(library, null)
		}
	}

	override fun handleSelectionChanged() {
		selected = if (controller.selectedItem is ContainerLibraryElement) {
			val elem = controller.selectedItem as ContainerLibraryElement
			elem.library?.defaultElementUUID == elem.uuid
		} else {
			false
		}
	}
}