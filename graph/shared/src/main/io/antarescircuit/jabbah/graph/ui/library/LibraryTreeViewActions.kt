package io.antarescircuit.jabbah.graph.ui.library

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.graph.library.AbstractLibraryAction
import io.antarescircuit.jabbah.graph.ui.GraphDataViewController

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