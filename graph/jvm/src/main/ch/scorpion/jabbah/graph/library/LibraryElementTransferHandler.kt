package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.ui.GraphElementViewTransferable
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode


/**
 * Handles the transfer of a [LibraryElement] into a [GraphView] using drag&drop.
 */
class LibraryElementTransferHandler : TransferHandler() {

    companion object {
        val DUMMY_IMAGE = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    }

    /** ---- [TransferHandler] */

    override fun getSourceActions(c: JComponent): Int {
        return TransferHandler.COPY
    }

    override fun canImport(support: TransferHandler.TransferSupport): Boolean {
        return false
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
        return false
    }
}