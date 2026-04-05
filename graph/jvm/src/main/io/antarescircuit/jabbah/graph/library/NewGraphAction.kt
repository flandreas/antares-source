package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.AbstractAction
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.model.GraphType
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
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
		 * @param type the optionally pre-set [GraphType]. If set, the user can't choose it
		 * @return the info, or `null` if the user canceled the action
		 */
		fun requestNewGraphInfo(parent: Component, title: String, type: GraphType? = null): NewMetaGraphInfo? {
			var info: NewMetaGraphInfo
			var oldName: TranslatableText? = null

			while (true) {
				info = NewMetaGraphPanel.showAsDialog(name = oldName, type = type) ?: return null
				oldName = info.name

				try {
					MetaGraph.validateName(info.name)
					break
				} catch (e: IllegalArgumentException) {
					if (JOptionPane.showConfirmDialog(
						parent,
						e.message,
						title,
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.ERROR_MESSAGE
					) == JOptionPane.CANCEL_OPTION) {
						return null
					}
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

			val directory = controller.selectedItem as LibraryDirectory
			val library = directory.library!!
			LOG.info("$name '${info.name.getTranslation()}'")

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
