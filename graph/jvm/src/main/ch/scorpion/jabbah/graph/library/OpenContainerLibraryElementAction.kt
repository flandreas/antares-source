package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import javax.swing.JFrame
import javax.swing.JOptionPane

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeViewSwing] for viewing. Whether it can be edited is decided by the view that displays it.
 */
class OpenContainerLibraryElementAction(
	private val application: Application,
	libraryTreeView: LibraryTreeViewSwing,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction(
	actionBaseName = "graph.action.openContainerLibraryElement",
	operation = Operation.View,
	libraryTreeView,
	eventBus
) {

	companion object {
		private val LOG by logger(OpenContainerLibraryElementAction::class)
	}

	init {
		eventBus.register(OpenContainerLibraryElementRequest::class) {
			if (!applicationMode.isEdit()) {
				eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "graph.action.cannotOpenWhileExecuting.msg"))
			} else {
				openAsSavable(it.element)
			}
		}
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		openAsSavable()
	}

	/**
	 * Opens the currently selected [ContainerLibraryElement] as the current [Savable] in the application.
	 */
	private fun openAsSavable() {
		openAsSavable(libraryTreeView.getSelectedItem() as ContainerLibraryElement)
	}

	private fun openAsSavable(element: ContainerLibraryElement) {
		try {
			val library = element.library!!
			library.libraryService.loadMetaGraph(library, element)
			application.controller.open(ApplicationData(element.metaGraph!!, library.createSavable(element), eventBus))
		} catch (e: Throwable) {
			LOG.error("Error while loading ${element.uuid}: ${e.message}")
			JOptionPane.showConfirmDialog(
				JFrame.getFrames()[0],
				Translations.getString("graph.action.load.error.general.desc"),
				name,
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE)
		}
	}
}