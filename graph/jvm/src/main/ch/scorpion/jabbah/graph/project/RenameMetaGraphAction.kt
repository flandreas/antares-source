package ch.scorpion.jabbah.graph.project

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.AbstractContainerLibraryElementAction
import ch.scorpion.jabbah.graph.library.ContainerLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * An [Action] for renaming the [MetaGraph] in a [ContainerLibraryElement].
 */
class RenameMetaGraphAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.renameMetaGraph",
	operation = Operation.Change,
	controller
) {
	override val opensDialog: Boolean get() = true

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)

	override fun execute(event: ActionEvent) {
		val element = selectedItem as ContainerLibraryElement

		val newName = JOptionPane.showInputDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("library.action.renameMetaGraph.question"),
			name,
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			element.name
		) as String?

		if (StringUtils.isEmpty(newName)) {
			return
		}

		controller.renameContainerLibraryElement(element, newName!!)
	}
}