package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.app.ApplicationModeHolder
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.AbstractApplicationModeEditAction
import java.awt.Frame

class AddLibraryToDesktopAction(
	applicationModeHolder: ApplicationModeHolder,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractApplicationModeEditAction("library.selectionDialog.action", applicationModeHolder) {

	override fun calculateEnabledness(): Boolean = true

	override fun execute(event: ActionEvent) {
		LibrarySelectionPanel.showAsDialog(Frame.getFrames()[0])?.let {
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