package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.invocation.InvocationHandler
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.graph.repository.LibraryDependencyException
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.repository.RepositoryService
import ch.scorpion.jabbah.graph.ui.*
import ch.scorpion.jabbah.graph.ui.library.LibraryTreeViewController
import java.awt.Frame
import java.awt.Image
import java.awt.Point
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

		/** The [Image] to be used for drag&drop when there is no image path available. */
		private val DUMMY_IMAGE = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

		/** Maps image resource paths to the corresponding [Image]. */
		private val ICON_CACHE = mutableMapOf<String, Image>()

		/** Gets the icon [Image] to be used for drag&drop. */
		private fun getIcon(libraryItem: LibraryItem): Image {
			return if (libraryItem is ContainerLibraryElement) {
				MetaGraphIconProvider.provideImage(libraryItem.type)
			} else {
				libraryItem.iconPath?.let { iconPath ->
					ICON_CACHE.getOrPut(iconPath) { UiUtil.themedIcon(iconPath).image }
				} ?: DUMMY_IMAGE
			}
		}
	}

	/** ---- [TransferHandler] */

	/**
	 * Dragging within [LibraryTreeViewSwing] leads to a "Move", dragging into a drawing always
	 * leads to "Copy", but we don't want to force the user to press "ALT" in that case.
	 * These two cases together can only be represented by using [TransferHandler.MOVE].
	 */
	override fun getSourceActions(c: JComponent): Int = MOVE

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
			dragImage = getIcon(controller.selectedItem as LibraryElement)
			dragImageOffset = Point(0, 0)
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