package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.project.Project
import ch.scorpion.jabbah.graph.project.ProjectModule
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JFrame
import javax.swing.JOptionPane

class RemoveLibraryAction(
	controller: LibraryTreeViewController,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractLibraryAction("library.removeImport.action", Operation.Change, controller) {

	override fun calculateEnabledness(): Boolean =
		super.calculateEnabledness()
			&& selectedItem is Library
			&& (selectedItem!!.library!!.isBrokenImport || libraryHolder.library.importedLibraryIds.contains(selectedItem!!.library!!.uuid))

	override fun execute(event: ActionEvent) {
		val service = if (selectedItem!!.library is Project) {
			ProjectModule.projectManagementService.invoke()
		} else {
			LibraryModule.libraryManagementService
		}

		InvocationHandler.invoke {
			if (service.containsLibraryReference(libraryHolder.library, selectedItem!!.library!!)) {
				val text = if (libraryHolder.library is Project) {
					Translations.getString("library.removeImport.stateReferenceFromProject.txt")
				} else {
					Translations.getString("library.removeImport.staleReferenceFromLibrary.txt")
				}
				JOptionPane.showConfirmDialog(
					JFrame.getFrames()[0],
					text,
					name,
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
			} else {
				service.removeImport(selectedItem!!.library!!)
			}
		}
	}
}