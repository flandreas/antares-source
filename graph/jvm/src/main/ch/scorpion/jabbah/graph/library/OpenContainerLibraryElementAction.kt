package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.DesktopApplication
import ch.scorpion.jabbah.app.Savable
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import javax.swing.JFrame
import javax.swing.JOptionPane

/**
 * An [Action] for opening the [ContainerLibraryElement] that is currently selected in the
 * [LibraryTreeView] for editing.
 */
class OpenContainerLibraryElementAction(
	private val application: DesktopApplication,
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus
) : AbstractContainerLibraryElementAction("graph.action.openContainerLibraryElement", libraryTreeView, eventBus) {

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

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
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
			application.open(ApplicationData(element.metaGraph!!, library.createSavable(element)))
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