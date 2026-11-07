package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class AddLibraryToDesktopAction(
	controller: LibraryTreeViewController,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractLibraryAction("library.selectionDialog.action", Operation.Change, controller) {

	override val opensDialog: Boolean get() = true

	override val authorizationTarget: Any? get() = libraryHolder.l

	override fun execute(event: ActionEvent) {
		LibrarySelectionPanel.showAsDialog(Frame.getFrames()[0], "library.addImport.action", name)?.let {
			InvocationHandler.invoke {
				libraryHolder.library.libraryService.addImport(libraryHolder.library, it)
			}
		}
	}
}