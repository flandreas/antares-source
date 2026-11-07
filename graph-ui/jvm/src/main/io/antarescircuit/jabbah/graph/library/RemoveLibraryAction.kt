package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.invocation.InvocationHandler
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.project.Project
import io.antarescircuit.jabbah.graph.project.ProjectModule
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JFrame
import javax.swing.JOptionPane

/**
 * Removes the currently selected [Library] from the list of imported [Libraries][Library]
 * of the current main [Library] (or main [Project]).
 */
class RemoveLibraryAction(
	controller: LibraryTreeViewController,
	private val libraryHolder: LibraryHolder = LibraryModule.libraryHolder
) : AbstractLibraryAction("library.removeImport.action", Operation.Change, controller) {

	override fun calculateEnabled(): Boolean =
		super.calculateEnabled()
			&& selectedItem is Library
			&& (selectedItem!!.library!!.isBrokenImport || libraryHolder.library.importedLibraryIds.contains(selectedItem!!.library!!.uuid))

	override fun execute(event: ActionEvent) {
		if (JOptionPane.showConfirmDialog(
			JFrame.getFrames()[0],
			Translations.getString("library.removeImport.confirmation", selectedItem!!.library!!.name.value),
			name,
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE
		) != JOptionPane.OK_OPTION) {
			return
		}

		InvocationHandler.invoke {
			val referenceEvaluation = libraryHolder.library.libraryService.evaluateLibraryReferences(libraryHolder.library, selectedItem!!.library!!)
			if (referenceEvaluation.hasNonSystemReferences) {
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
				if (libraryHolder.library is Project) {
					ProjectModule.projectManagementService.removeImport(selectedItem!!.library!!.uuid, referenceEvaluation.systemReferences)
				} else {
					LibraryModule.libraryManagementService.removeImport(selectedItem!!.library!!.uuid, referenceEvaluation.systemReferences)
				}
			}
		}
	}
}