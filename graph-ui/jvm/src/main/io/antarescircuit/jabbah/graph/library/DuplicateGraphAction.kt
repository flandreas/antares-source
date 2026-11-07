package io.antarescircuit.jabbah.graph.library

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.edit.auth.Operation
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * Creates a new [ContainerLibraryElement] with a duplicate of a [MetaGraph] as a child of the [LibraryDirectory]
 * that contains the source [MetaGraph].
 */
class DuplicateGraphAction(
	controller: LibraryTreeViewController,
) : AbstractContainerLibraryElementAction(
	actionBaseName = "library.action.duplicateGraph",
	operation = Operation.Change,
	controller
) {

	override val opensDialog: Boolean get() = true

	override fun execute(event: ActionEvent) {
		val element = selectedItem as ContainerLibraryElement

		val newGraphName = JOptionPane.showInputDialog(
			SwingUtilities.getWindowAncestor(controller.view as Component),
			Translations.getString("library.action.newGraph.question"),
			name,
			JOptionPane.QUESTION_MESSAGE,
			null,
			null,
			Translations.getString("library.action.duplicateGraph.newName", element.name.value)
		) as String?

		if (StringUtils.isEmpty(newGraphName)) {
			return
		}

		val library = element.library

		val duplicate = library!!.libraryService.duplicateContainerLibraryElement(folderOfSelectedItem!!, element, TranslatableText(newGraphName!!))
		System.invokeLater {
			controller.eventBus.post(OpenContainerLibraryElementRequest(duplicate))
		}
	}
}