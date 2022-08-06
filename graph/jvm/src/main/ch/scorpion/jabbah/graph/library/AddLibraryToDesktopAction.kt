package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame

class AddLibraryToDesktopAction(
	controller: LibraryTreeViewController,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractLibraryAction("library.selectionDialog.action", Operation.Change, controller) {

	override fun calculateEnabledness(): Boolean = super.calculateEnabledness() && libraryHolder.l != null

	override fun execute(event: ActionEvent) {
		LibrarySelectionPanel.showAsDialog(Frame.getFrames()[0], "library.addImport.action")?.let {
			InvocationHandler.invoke {
				val service = if (libraryHolder.library is Project) {
					ProjectModule.projectManagementService.invoke()
				} else {
					LibraryModule.libraryManagementService
				}
				service.addImport(it)
			}
		}
	}
}