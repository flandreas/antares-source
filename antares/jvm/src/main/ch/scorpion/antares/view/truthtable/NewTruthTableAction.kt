package ch.scorpion.antares.view.truthtable

import ch.scorpion.antares.model.truthtable.OpenTruthTableItemRequest
import ch.scorpion.antares.model.truthtable.TruthTable
import ch.scorpion.antares.model.truthtable.TruthTableLibraryItem
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryFolderAction
import ch.scorpion.jabbah.graph.library.LibraryDirectory
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class NewTruthTableAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?
) : AbstractLibraryFolderAction(
	actionBaseName = "library.action.newTruthTable",
	operation = Operation.Change,
	controller
) {
	override fun execute(event: ActionEvent) {
		val newName = JOptionPane.showInputDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("library.action.newTruthTable.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)
		if (StringUtils.isEmpty(newName)) {
			return
		}

		val directory = controller.selectedItem as LibraryDirectory
		val library = directory.library!!
		val truthTableItem = TruthTableLibraryItem(TruthTable(newName, listOf("A", "B", "C"), listOf("Y", "X")))

		library.libraryService.addLibraryItem(library, truthTableItem, directory)
		eventBus.post(OpenTruthTableItemRequest(truthTableItem))
	}

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)
}