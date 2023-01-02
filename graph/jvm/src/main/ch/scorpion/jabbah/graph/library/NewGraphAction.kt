package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Creates a new [ContainerLibraryElement] with an empty [MetaGraph] as a child of the
 * currently selected [LibraryDirectory].
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
	}

	private val operationTarget: () -> Any? get() = {
		if (selectedItem is LibraryDirectory) {
			selectedFolder.library
		} else {
			null
		}
	}

	override fun execute(event: ActionEvent) {
		var info: NewMetaGraphInfo

		while (true) {
			info = NewMetaGraphPanel.showAsDialog() ?: return

			if (info.name.isEmpty) {
				if (JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					Translations.getString("library.action.newGraph.emptyName.msg"),
					"$name",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.ERROR_MESSAGE
				) == JOptionPane.CANCEL_OPTION) {
					return
				}
			} else {
				break
			}
		}

		LOG.info("$name '${info.name.getTranslation()}'")

		val directory = controller.selectedItem as LibraryDirectory
		val library = directory.library!!
		val metaGraph = MetaGraph.withName(info.name, info.type)

		val newElement = library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
		eventBus.post(OpenContainerLibraryElementRequest(newElement))
	}

	override val operationAuthorized: Boolean
		get() = operationTarget.invoke() != null && Authorizer.isCurrentUserAuthorizedTo(operation, operationTarget.invoke()!!)
}
