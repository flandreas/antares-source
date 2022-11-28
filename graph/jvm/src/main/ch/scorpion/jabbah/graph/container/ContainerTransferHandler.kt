package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.ComponentTransferable
import java.awt.Image
import java.awt.Point
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Handles the transfer of a [Component] from the [ContainerTreeView] into the [ContainerDrawing]
 * using drag&drop.
 */
class ContainerTransferHandler : TransferHandler() {

	companion object {

		/** Maps image resource paths to the corresponding [Image]. */
		private val ICON_CACHE = mutableMapOf<String, Image>()

		private fun getIcon(item: DraggableTreeItem): Image =
			ICON_CACHE.getOrPut(item.iconPath) { UiUtil.themedIcon(item.iconPath). image }
	}

    override fun getSourceActions(c: JComponent?): Int = MOVE

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun canImport(comp: JComponent?, transferFlavors: Array<out DataFlavor>?): Boolean = false

    override fun createTransferable(c: JComponent?): Transferable? {
        val treeNode = (c as JTree).selectionPath.lastPathComponent as DefaultMutableTreeNode
        if (treeNode.userObject !is DraggableTreeItem) {
            return null
        }

        val component = (treeNode.userObject as DraggableTreeItem).factory.invoke()

        dragImage = getIcon(treeNode.userObject as DraggableTreeItem)
        dragImageOffset = Point(0, 0)

        return ComponentTransferable(component)
    }
}
