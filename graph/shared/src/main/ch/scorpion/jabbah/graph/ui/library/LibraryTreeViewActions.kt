package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.graph.library.AbstractLibraryAction
import ch.scorpion.jabbah.graph.ui.GraphDataViewController

open class LibraryTreeViewActions(
	controller: LibraryTreeViewController,
	application: Application
) {
	private val actions = mutableSetOf<Action>()

	fun dispose() {
		actions.forEach { it.dispose() }
	}

	protected val openContainerLibraryElementAction = register(OpenContainerLibraryElementAction(
		application.controller as GraphDataViewController, controller))

	protected fun register(action: Action): Action {
		actions.add(action)
		if (action is AbstractLibraryAction) {
			// Must not be called from constructor
			action.updateEnabled()
		}
		return action
	}
}