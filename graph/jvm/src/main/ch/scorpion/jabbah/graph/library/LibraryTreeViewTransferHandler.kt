package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.graph.repository.LibraryDependencyException
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.repository.RepositoryService
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferable
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferableData
import ch.scorpion.jabbah.graph.ui.LibraryFolderTransferable
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
		if (support.dropLocation !is JTree.DropLocation || support.component != controller.view) {
			return false
		}

		val item = extractTransferItem(support)
		if (item !is LibraryElement && item !is LibraryFolder) {
			return false
		}
		return getLibraryDropLocation(support)?.let {
			controller.allowMove(item, it.directory)
		} ?: false
	}

	override fun createTransferable(c: JComponent): Transferable? {
		return if (controller.selectedItem is LibraryElement) {
			createLibraryElementTransferable()
		} else if (controller.selectedItem is LibraryFolder && controller.selectedItem !is Library) {
			createFolderTransferable(controller.selectedItem as LibraryFolder)
		} else {
			null
		}
	}

	override fun importData(support: TransferSupport): Boolean {
		getLibraryDropLocation(support)?.let {
			val item = extractTransferItem(support)

			if (item == null || !confirmMove(item, it.directory)) {
				return false
			}

			return try {
				when (item) {
					is ContainerLibraryElement -> {
						InvocationHandler.invoke { repositoryService.move(item, it.directory, it.index) }
						true
					}
					is LibraryFolder -> {
						InvocationHandler.invoke { item.library!!.libraryService.move(item.library!!, item, it.directory, it.index) }
						true
					}
					else -> false
				}
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

	private fun createLibraryElementTransferable(): Transferable? =
		controller.createTransferableGraphElementView()?.let {
			// Didn't find a better way to hide the default cursor rectangle in the size of the TreeNode's bounding box
			dragImage = DUMMY_IMAGE
			dragImageOffset = java.awt.Point(0, 0)

			GraphElementViewTransferable.of(it, controller.selectedItem as LibraryElement)
		}

	private fun createFolderTransferable(folder: LibraryFolder): Transferable =
		LibraryFolderTransferable(folder)

	private fun confirmMove(item: LibraryItem, destination: LibraryDirectory): Boolean {
		val otherDirectory = if (destination !== item.library!!.libraryService.getDirectoryOf(item.library!!, item)) {
			destination
		} else {
			null
		}

		return when (item) {
			is LibraryElement -> confirmMoveLibraryElement(item, otherDirectory)
			is LibraryFolder -> confirmMoveLibraryFolder(item, otherDirectory)
			else -> false
		}
	}

	private fun confirmMoveLibraryElement(item: LibraryElement, otherDirectory: LibraryDirectory?): Boolean {
		val question = if (otherDirectory != null) {
			Translations.getString("repository.action.moveGraphToOtherDirectory.question", item.name.value, otherDirectory.name.value)
		} else {
			Translations.getString("repository.action.moveGraph.question", item.name.value)
		}
		return JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			question,
			Translations.getString("repository.action.moveGraph.name"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION
	}

	private fun confirmMoveLibraryFolder(folder: LibraryFolder, otherDirectory: LibraryDirectory?): Boolean {
		val question = if (otherDirectory != null) {
			Translations.getString("repository.action.moveFolderToOtherDirectory.question", folder.name.value, otherDirectory.name.value)
		} else {
			Translations.getString("repository.action.moveFolder.question", folder.name.value)
		}
		return JOptionPane.showConfirmDialog(
			Frame.getFrames()[0],
			question,
			Translations.getString("repository.action.moveFolder.name"),
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

	private fun extractTransferItem(support: TransferSupport): LibraryItem? {
		if (support.isDataFlavorSupported(GraphElementViewTransferable.FLAVOR)) {
			val data = support.transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
			return data.libraryElement
		}
		if (support.isDataFlavorSupported(LibraryFolderTransferable.FLAVOR)) {
			return support.transferable.getTransferData(LibraryFolderTransferable.FLAVOR) as LibraryFolder
		}

		return null
	}

	private data class LibraryDropLocation(val directory: LibraryDirectory, val index: Int? = null)
}