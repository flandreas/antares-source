package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Creates a new [ContainerLibraryElement] with an empty [MetaGraph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(
	controller: LibraryTreeViewController,
) : AbstractLibraryFolderAction(
	actionBaseName = "library.action.newGraph",
	operation = Operation.Change,
	controller
) {

	companion object {
		private val LOG by logger(NewGraphAction::class)

		/**
		 * Requests the name of a new [MetaGraph] from the user by showing a dialog.
		 * @return the new name, or `null` if the user cancelled the action or provided an empty name
		 */
		fun requestNewGraphName(parent: JComponent, title: String): String? {
			val newName = JOptionPane.showInputDialog(
				SwingUtilities.getWindowAncestor(parent),
				Translations.getString("library.action.newGraph.question"),
				title,
				JOptionPane.QUESTION_MESSAGE
			)

			return if (StringUtils.isEmpty(newName)) {
				null
			} else {
				newName
			}
		}
	}

	private val operationTarget: () -> Any? get() = {
		if (selectedItem is LibraryDirectory) {
			selectedFolder.library
		} else {
			null
		}
	}

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    val newName = requestNewGraphName(controller.view as JComponent, name)
		    ?: return

	    LOG.info("$name '$newName'")

        val directory = controller.selectedItem as LibraryDirectory
	    val library = directory.library!!
	    val metaGraph = MetaGraph.withName(newName)

	    val element = library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
		eventBus.post(OpenContainerLibraryElementRequest(element))
    }

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)
}
