package io.antarescircuit.jabbah.graph.ui.desktop

import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import javax.swing.SwingUtilities
import javax.swing.TransferHandler

/** Handles DnD drop gestures on [DockingGraphDesktopViewSwing].*/
class DockingTransferHandler(
    private val desktop: DockingGraphDesktopViewSwing,
    private val flavour: DataFlavor = GraphDesktopViewItemTransferable.FLAVOR
) : TransferHandler() {

    private val dropTarget = DockingDropTarget()

    init {
        desktop.glassPane.dropTarget = dropTarget
    }

    private inner class DockingDropTarget : DropTarget() {

        override fun dragOver(dtde: DropTargetDragEvent) {
            super.dragOver(dtde)
            if (!dtde.isDataFlavorSupported(flavour)) {
                return
            }
            desktop.setDropLocation(dtde.location)
        }

        override fun drop(dtde: DropTargetDropEvent) {
            if (dtde.isDataFlavorSupported(flavour)) {
                val transferData = dtde.transferable.getTransferData(flavour)
                if (transferData is GraphDesktopViewItem) {
                    SwingUtilities.invokeLater {
                        desktop.handleDrop(transferData)
                    }
                }
            }
            dtde.dropComplete(true)
        }
    }

    override fun canImport(support: TransferSupport): Boolean =
        support.isDataFlavorSupported(flavour)
}