package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Creates a new [ContainerLibraryElement] with an empty [MetaGraph] as a child of the
 * currently selected [LibraryDirectory].
 */
class NewGraphAction(
	private val controller: LibraryTreeViewController,
	private val eventBus: EventBus = BaseModule.eventBus
) : AbstractAction(
	baseName = "library.action.newGraph"
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

	init {
		// Always enabled, check enabledness on execution in order to show information dialog
		enabled = true
	}

    override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
	    if (checkEnabledness()) {
		    val newName = requestNewGraphName(controller.view as JComponent, name)
			    ?: return

		    LOG.info("$name '$newName'")

		    val directory = controller.selectedItem as LibraryDirectory
		    val library = directory.library!!
		    val metaGraph = MetaGraph.withName(newName)

		    val element = library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
		    eventBus.post(OpenContainerLibraryElementRequest(element))
	    }
    }

	private fun checkEnabledness(): Boolean {
		getEnablednessProhibitionKey()?.let {
			JOptionPane.showMessageDialog(
				Frame.getFrames()[0],
				Translations.getString(it),
				Translations.getString("library.action.newGraph.name"),
				JOptionPane.ERROR_MESSAGE)
			return false
		}
		return true
	}

	private fun getEnablednessProhibitionKey(): String? {
		if (controller.applicationModeHolder.currentMode.isExecute()) {
			return "library.action.newGraph.notEditMode.error"
		}
		if (EditModule.commandManager.canUndo()) {
			return "library.action.newGraph.unsavedChanges.error"
		}
		if (controller.selectedItem !is LibraryDirectory) {
			return "library.action.newGraph.noDirectory.error"
		}
		if (!AbstractLibraryAction.isAuthorized(Operation.Change, controller.selectedItem?.library)) {
			return "library.action.newGraph.notAuthorized.error"
		}

		return null
	}
}
