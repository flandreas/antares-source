package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.model.image.ImageLibraryElement
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewType
import java.awt.Component
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

/**
 * An [Action] for deleting the currently selected [LibraryItem].
 */
class DeleteLibraryItemAction(
	controller: LibraryTreeViewController,
) : AbstractLibraryAction(
	BASE_RESOURCE_NAME,
	operation = Operation.Change,
	controller
) {

	companion object {
		private val LOG by logger(DeleteLibraryItemAction::class)
		private const val BASE_RESOURCE_NAME = "graph.action.deleteBaseElement"
		private const val IMAGE_RESOURCE_NAME = "graph.action.deleteImage"
		private const val CONTAINER_RESOURCE_NAME = "graph.action.deleteContainerElement"
		private const val DIRECTORY_RESOURCE_NAME = "graph.action.deleteLibraryDirectory"
		private const val ITEM_RESOURCE_NAME = "graph.action.deleteItem"
	}

	override val opensDialog: Boolean get() = true

	private val baseName: String
		get() = when (selectedItem) {
			is BaseLibraryElement -> BASE_RESOURCE_NAME
			is ImageLibraryElement -> IMAGE_RESOURCE_NAME
			is ContainerLibraryElement -> CONTAINER_RESOURCE_NAME
			is LibraryDirectory -> DIRECTORY_RESOURCE_NAME
			else -> ITEM_RESOURCE_NAME
		}

	override fun execute(event: ch.scorpion.jabbah.base.event.ActionEvent) {
		val libraryItem = controller.selectedItem
		if (JOptionPane.showConfirmDialog(
				SwingUtilities.getWindowAncestor(controller.view as Component),
				Translations.getString("$baseName.question", controller.selectedItem!!),
				name,
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			val library = libraryItem!!.library!!
			LOG.userTrail("Delete library element ${libraryItem.name.getOptionalTranslation()}")
			library.libraryService.removeLibraryItem(libraryItem.library!!, libraryItem)
		}
	}

	override fun calculateEnabled(): Boolean {
		// Deletion of directories is handled by a separate Action
		return super.calculateEnabled() && (
			controller.type == LibraryTreeViewType.CompositionDestination && selectedItem is BaseLibraryElement
				|| selectedItem is UndoableStateLibraryItem<*>
				|| selectedItem is ImageLibraryElement
		)
	}

	override fun handleSelectionChanged() {
		setBaseName(baseName)
	}
}