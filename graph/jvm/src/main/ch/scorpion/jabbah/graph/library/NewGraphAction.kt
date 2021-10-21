package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Creates a new [ContainerLibraryElement] with an empty [MetaGraph] as a child of the currently selected [LibraryDirectory].
 */
class NewGraphAction(
	controller: LibraryTreeViewController,
	private val operationTarget: () -> Any?
) : AbstractLibraryFolderAction(
	actionBaseName = "library.action.newGraph",
	operation = Operation.Change,
	controller
) {

	companion object {
		private val LOG by logger(NewGraphAction::class)
	}

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    val newName = JOptionPane.showInputDialog(
		    SwingUtilities.getWindowAncestor(controller.view as Component),
		    Translations.getString("library.action.newGraph.question"),
		    name,
		    JOptionPane.QUESTION_MESSAGE
	    )
	    if (StringUtils.isEmpty(newName)) {
		    return
	    }

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
