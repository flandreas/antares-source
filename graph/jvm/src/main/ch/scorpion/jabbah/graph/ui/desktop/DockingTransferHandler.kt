package ch.scorpion.jabbah.graph.ui.desktop

import ch.scorpion.jabbah.base.logger
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetEvent
import javax.swing.TransferHandler

class DockingTransferHandler(
    private val desktop: DockingGraphDesktopViewSwing,
    private val flavour: DataFlavor = GraphDesktopViewItemTransferable.FLAVOR
) : TransferHandler() {

    companion object {
        private val LOG by logger(DockingTransferHandler::class)
    }

    private val dropTarget = DockingDropTarget()

    init {
        desktop.glassPane.dropTarget = dropTarget
    }

    private inner class DockingDropTarget : DropTarget() {

        override fun dragEnter(dtde: DropTargetDragEvent) {
            super.dragEnter(dtde)
            if (!dtde.isDataFlavorSupported(flavour)) {
                return
            }
            LOG.trace("dragEnter")
        }

        override fun dragExit(dte: DropTargetEvent?) {
            super.dragExit(dte)
            LOG.trace("dragExit")
        }

        override fun dragOver(dtde: DropTargetDragEvent) {
            super.dragOver(dtde)
            if (!dtde.isDataFlavorSupported(flavour)) {
                return
            }
            desktop.setDropLocation(dtde.location)
        }
    }

    override fun canImport(support: TransferSupport): Boolean =
        support.isDataFlavorSupported(flavour)
}