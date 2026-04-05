package io.antarescircuit.jabbah.graph.ui.desktop

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class GraphDesktopViewItemTransferable(val item: GraphDesktopViewItem) : Transferable {

    companion object {
        // The second argument of the constructor is not relevant. Could just be anything.
        val FLAVOR = DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + String::class.java.name + "\"")
    }

    override fun getTransferDataFlavors(): Array<out DataFlavor?>? = arrayOf(FLAVOR)

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor == FLAVOR

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != FLAVOR) {
            throw UnsupportedFlavorException(flavor)
        }
        return item
    }
}