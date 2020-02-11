package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * An [Action] for deleting the currently selected [LibraryElement].
 */
class DeleteLibraryElementAction(
	libraryTreeView: LibraryTreeView,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryAction(BASE_RESOURCE_NAME, libraryTreeView, eventBus) {

	companion object {
		private const val BASE_RESOURCE_NAME = "graph.action.deleteBaseElement"
		private const val CONTAINER_RESOURCE_NAME = "graph.action.deleteContainerElement"
		private const val DIRECTORY_RESOURCE_NAME = "graph.action.deleteLibraryDirectory"
	}

	private val baseName: String get() = when (selectedItem) {
		is BaseLibraryElement -> BASE_RESOURCE_NAME
		is ContainerLibraryElement -> CONTAINER_RESOURCE_NAME
		is LibraryDirectory -> DIRECTORY_RESOURCE_NAME
		else -> throw IllegalStateException()
	}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val libraryItem = libraryTreeView.getSelectedItem()
		if (JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(libraryTreeView),
				Translations.getString("$baseName.question", libraryTreeView.getSelectedItem()!!),
				name,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			val library = libraryItem!!.library!!
			library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem, folderOfSelectedItem as LibraryDirectory)
		}
	}

	override fun calculateEnabledness(): Boolean {
		return isLibraryOwnedByUser && (selectedItem is BaseLibraryElement || selectedItem is ContainerLibraryElement)
	}

	override fun handleSelectionChanged() {
		setBaseName(baseName)
	}
}