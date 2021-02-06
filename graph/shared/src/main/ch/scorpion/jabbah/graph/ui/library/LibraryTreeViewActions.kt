package ch.scorpion.jabbah.graph.ui.library

import ch.scorpion.jabbah.app.Application

open class LibraryTreeViewActions(
	controller: LibraryTreeViewController,
	application: Application
) {

	protected val openContainerLibraryElementAction = OpenContainerLibraryElementAction(application, controller)
}