package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.library.LibraryTreeViewSwing

/**
 * An [Action] for marking a [ContainerLibraryElement] as the default one, i.e. the one to be
 * opened when the [Project] is loaded.
 */
class DefaultContainerLibraryElementAction(
	libraryTreeView: LibraryTreeViewSwing,
	private val operationTarget: () -> Any?,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.setDefaultElement",
	operation = Operation.Change,
	libraryTreeView,
	eventBus
) {

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)

	override fun execute(event: ActionEvent) {
		val elem = libraryTreeView.getSelectedItem() as ContainerLibraryElement
		val library = elem.library!!
		if (selected) {
			library.libraryService.setDefaultElement(library, elem.uuid)
		} else {
			library.libraryService.setDefaultElement(library, null)
		}
	}

	override fun handleSelectionChanged() {
		selected = if (libraryTreeView.getSelectedItem() is ContainerLibraryElement) {
			val elem = libraryTreeView.getSelectedItem() as ContainerLibraryElement
			elem.library?.defaultElementUUID == elem.uuid
		} else {
			false
		}
	}
}