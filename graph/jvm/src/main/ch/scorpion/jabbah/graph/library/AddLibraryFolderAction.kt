package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation.Change
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.ui.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Asks the user for the name of the new [LibraryDirectory] and adds a new [LibraryDirectory] as a child of
 * the currently selected [LibraryDirectory].
 */
class AddLibraryFolderAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryFolderAction(
	actionBaseName = "library.action.addFolder",
	operation = Change,
	controller,
	eventBus
) {

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)

	override fun execute(event: ActionEvent) {
		val name = JOptionPane.showInputDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("library.action.addFolder.question"),
			name,
			JOptionPane.QUESTION_MESSAGE
		)

		if (StringUtils.isEmpty(name)) {
			return
		}

		val directory = controller.selectedItem as LibraryDirectory
		directory.library!!.libraryService.addFolder(directory.library!!, TranslatableText(name), directory)
	}
}