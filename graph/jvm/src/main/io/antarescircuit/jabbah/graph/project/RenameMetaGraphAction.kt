package io.antarescircuit.jabbah.graph.project

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.AbstractContainerLibraryElementAction
import io.antarescircuit.jabbah.graph.library.ContainerLibraryElement
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * An [Action] for renaming the [MetaGraph] in a [ContainerLibraryElement].
 */
class RenameMetaGraphAction(
	controller: LibraryTreeViewController,
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.renameMetaGraph",
	operation = Operation.Change,
	controller
) {
	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val element = selectedItem as ContainerLibraryElement

		val newName = requestNewName(element.name.value)
			?: return

		controller.renameContainerLibraryElement(element, newName)
	}

	private fun requestNewName(name: String): String? {
		var oldName = name
		while (true) {
			val newName = JOptionPane.showInputDialog(
				SwingUtilities.getWindowAncestor(controller.view as Component),
				Translations.getString("library.action.renameMetaGraph.question"),
				name,
				JOptionPane.QUESTION_MESSAGE,
				null,
				null,
				oldName
			) as String? ?: return null

			oldName = newName

			try {
				MetaGraph.validateName(newName)
				return newName
			} catch (e: IllegalArgumentException) {
				if (JOptionPane.showConfirmDialog(
					SwingUtilities.getWindowAncestor(controller.view as Component),
					e.message,
					name,
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE
				) == JOptionPane.CANCEL_OPTION) {
					return null
				}
			}
		}
	}
}