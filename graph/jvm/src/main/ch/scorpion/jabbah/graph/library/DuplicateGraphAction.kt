package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.MetaGraph
import java.awt.Frame
import javax.swing.JOptionPane

/**
 * Creates a new [ContainerLibraryElement] with a duplicate of a [MetaGraph] as a child of the [LibraryDirectory]
 * that contains the source [MetaGraph].
 */
class DuplicateGraphAction(
	eventBus: EventBus = BaseModule.eventBus
) : AbstractContainerLibraryElementAction("library.action.duplicateGraph", eventBus) {

	override fun execute(event: ActionEvent) {
		val element = selectedItem as ContainerLibraryElement
		var newGraphName = "Copy of ${element.name}"

		newGraphName = JOptionPane.showInputDialog(
			Frame.getFrames()[0],
			Translations.getString("library.action.newGraph.question"),
			name,
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			newGraphName
		) as String

		if (StringUtils.isEmpty(newGraphName)) {
			return
		}

		val library = element.library

		val duplicate = library!!.libraryService.duplicateContainerLibraryElement(folderOfSelectedItem!!, element, newGraphName)
		eventBus.post(OpenContainerLibraryElementRequest(duplicate))
	}

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && super.calculateEnabledness()
	}
}