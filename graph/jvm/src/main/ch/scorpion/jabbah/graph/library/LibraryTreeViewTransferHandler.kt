package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.repository.RepositoryModule
import ch.scorpion.jabbah.graph.repository.RepositoryService
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferable
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferableData
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode


/**
 * Handles drag&drop of a [LibraryElement] in [LibraryTreeView].
 */
class LibraryTreeViewTransferHandler(
	private val treeView: LibraryTreeView,
	private val repositoryService: RepositoryService = RepositoryModule.repositoryService.invoke()
) : TransferHandler() {

    companion object {
	    private val LOG by logger(LibraryTreeViewTransferHandler::class)
        private val DUMMY_IMAGE = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    }

    /** ---- [TransferHandler] */

    override fun getSourceActions(c: JComponent): Int = TransferHandler.COPY

    override fun canImport(support: TransferHandler.TransferSupport): Boolean {
	    if (!support.isDataFlavorSupported(GraphElementViewTransferable.FLAVOR)) {
		    return false
	    }
	    if (support.dropLocation !is JTree.DropLocation || support.component != treeView) {
		    return false
	    }
	    if (extractTransferElement(support) !is ContainerLibraryElement) {
		    return false
	    }

	    return getDropLibraryDirectory(support) != null
    }

    override fun createTransferable(c: JComponent): Transferable? {
        val tree = c as JTree
        val treeNode = tree.selectionPath.lastPathComponent as DefaultMutableTreeNode
        if (treeNode.userObject !is LibraryElement) {
            return null
        }

        val libraryElement = treeNode.userObject as LibraryElement
        val newInstance = libraryElement.getNewInstance<GraphElement>()

        // Didn't find a better way to hide the default cursor rectangle in the size of the TreeNode's bounding box
        dragImage = DUMMY_IMAGE
        dragImageOffset = java.awt.Point(0, 0)

        return GraphElementViewTransferable.of(newInstance, libraryElement)
    }

    override fun importData(support: TransferHandler.TransferSupport): Boolean {
	    getDropLibraryDirectory(support)?.let {
		    val elem = extractTransferElement(support) as ContainerLibraryElement
		    val dest = getDropLibraryDirectory(support)
		    repositoryService.move(elem, dest!!)
		    return true
	    }

	    return false
    }

	private fun getDropLibraryDirectory(support: TransferHandler.TransferSupport): LibraryDirectory? {
		val dropLocation = support.dropLocation as JTree.DropLocation
		return (dropLocation.path.lastPathComponent as DefaultMutableTreeNode).userObject as? LibraryDirectory
	}

	private fun extractTransferElement(support: TransferHandler.TransferSupport): LibraryElement {
		val data = support.transferable.getTransferData(GraphElementViewTransferable.FLAVOR) as GraphElementViewTransferableData
		return data.libraryElement
	}
}