package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class AddLibraryToDesktopAction(
	controller: LibraryTreeViewController,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractLibraryAction("library.selectionDialog.action", Operation.Change, controller) {

	override val opensDialog: Boolean get() = true

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && libraryHolder.l != null

	override fun execute(event: ActionEvent) {
		LibrarySelectionPanel.showAsDialog(Frame.getFrames()[0], "library.addImport.action", name)?.let {
			InvocationHandler.invoke {
				libraryHolder.library.libraryService.addImport(libraryHolder.library, it)
			}
		}
	}
}