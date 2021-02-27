package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.graph.ui.GraphDataViewController

open class LibraryTreeViewActions(
	controller: LibraryTreeViewController,
	application: Application
) {

	protected val openContainerLibraryElementAction = OpenContainerLibraryElementAction(
		application.controller as GraphDataViewController, controller)
}