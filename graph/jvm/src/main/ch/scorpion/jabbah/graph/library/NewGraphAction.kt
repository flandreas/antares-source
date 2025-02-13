package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import java.awt.Frame
import javax.swing.JOptionPane

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
		 * Requests the info for a new [MetaGraph] from the user by showing a dialog.
		 * @return the info, or `null` if the user cancelled the action
		 */
		fun requestNewGraphInfo(parent: Component, title: String): NewMetaGraphInfo? {
			var info: NewMetaGraphInfo

			while (true) {
				info = NewMetaGraphPanel.showAsDialog() ?: return null

				if (info.name.isEmpty) {
					if (JOptionPane.showConfirmDialog(
							parent,
							Translations.getString("library.action.newGraph.emptyName.msg"),
							title,
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.ERROR_MESSAGE
						) == JOptionPane.CANCEL_OPTION) {
						return null
					}
				} else {
					break
				}
			}

			return info
		}
	}

	override val opensDialog: Boolean get() = true

	init {
		// Always enabled, check enabledness on execution in order to show information dialog
		enabled = true
	}

	override fun execute(event: ActionEvent) {
		if (checkEnabledness()) {
			var info = requestNewGraphInfo(Frame.getFrames()[0], name) ?: return

			LOG.info("$name '${info.name.getTranslation()}'")

			val directory = controller.selectedItem as LibraryDirectory
			val library = directory.library!!
			val metaGraph = MetaGraph.create(info.name, info.type)

			val newElement = library.libraryService.addContainerLibraryElement(library, metaGraph, directory)
			eventBus.post(OpenContainerLibraryElementRequest(newElement))
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
