package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.repository.LibraryDependencyException
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.repository.RepositoryService
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferable
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferableData
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JOptionPane
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode


/**
 * Handles drag&drop of a [LibraryElement] in [LibraryTreeViewSwing].
 */
class LibraryTreeViewTransferHandler(
	private val controller: LibraryTreeViewController,
	private val repositoryService: RepositoryService = RepositoryModule.repositoryService.invoke()
) : TransferHandler() {

	companion object {
		private val DUMMY_IMAGE = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
	}

	/** ---- [TransferHandler] */

	override fun getSourceActions(c: JComponent): Int = COPY

	override fun canImport(support: TransferSupport): Boolean {
		if (!support.isDataFlavorSupported(GraphElementViewTransferable.FLAVOR)) {
			return false
		}
		if (support.dropLocation !is JTree.DropLocation || support.component != controller.view) {
			return false
		}
		if (extractTransferElement(support) !is ContainerLibraryElement) {
			return false
		}

		return getLibraryDropLocation(support) != null
	}

	override fun createTransferable(c: JComponent): Transferable? {
		return controller.createTransferableGraphElementView()?.let {
			// Didn't find a better way to hide the default cursor rectangle in the size of the TreeNode's bounding box
			dragImage = DUMMY_IMAGE
			dragImageOffset = java.awt.Point(0, 0)

			GraphElementViewTransferable.of(it, controller.selectedItem as LibraryElement)
		}
	}

	override fun importData(support: TransferSupport): Boolean {
		getLibraryDropLocation(support)?.let {
			val elem = extractTransferElement(support) as ContainerLibraryElement
			if (!shouldMove(elem.toString(), it.directory.name.value)) {
				return false
			}
			return try {
				InvocationHandler.invoke { repositoryService.move(elem, it.directory, it.index) }
				true
			} catch (e: LibraryDependencyException) {
				JOptionPane.showConfirmDialog(
					Frame.getFrames()[0],
					Translations.getString("repository.move.dependencyError.msg", e.subGraphVertice.name!!),
					Translations.getString("repository.move.action.name"),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.ERROR_MESSAGE)
				false
			}
		}

		return false
	}

	private fun shouldMove(name: String, destination: String): Boolean {
		return JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			Translations.getString("repository.move.action.question", name, destination),
			Translations.getString("repository.move.action.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
	}

	private fun getLibraryDropLocation(support: TransferSupport): LibraryDropLocation? {
		val dropLocation = support.dropLocation as JTree.DropLocation
		return dropLocation.path?.let {
			val item = (it.lastPathComponent as DefaultMutableTreeNode).userObject
			val index = if (dropLocation.childIndex >= 0) dropLocation.childIndex else null
			if (item is LibraryDirectory) {
				LibraryDropLocation(item, index)
			} else {
				null
			}
		}
	}

	private fun extractTransferElement(support: TransferSupport): LibraryElement {
		val data = support.transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
		return data.libraryElement
	}

	private data class LibraryDropLocation(val directory: LibraryDirectory, val index: Int? = null)
}