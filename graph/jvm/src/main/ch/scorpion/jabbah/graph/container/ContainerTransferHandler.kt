package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.edit.ComponentTransferable
import ch.scorpion.jabbah.edit.Component
import java.awt.Point
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Handles the transfer of a [Component] from the [ContainerTreeView] into the [ContainerDrawing]
 * using drag&drop.
 */
class ContainerTransferHandler : TransferHandler() {

    private val dummyImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)

    override fun getSourceActions(c: JComponent?): Int = MOVE

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun canImport(comp: JComponent?, transferFlavors: Array<out DataFlavor>?): Boolean = false

    override fun createTransferable(c: JComponent?): Transferable? {
        val treeNode = (c as JTree).selectionPath.lastPathComponent as DefaultMutableTreeNode
        if (treeNode.userObject !is DraggableTreeItem) {
            return null
        }

        val component = (treeNode.userObject as DraggableTreeItem).factory.invoke()

        dragImage = dummyImage
        dragImageOffset = Point(0, 0)

        return ComponentTransferable(component)
    }
}
