package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.library.UndoableStateLibraryItem
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class UndoableStateLibraryItemTransferable(
    private val data: UndoableStateLibraryItem<*>
) : Transferable {
    companion object {
        val FLAVOR = DataFlavor("${DataFlavor.javaJVMLocalObjectMimeType};class=\"${UndoableStateLibraryItem::class.java.name}\"")
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(FLAVOR)

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor == FLAVOR

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != FLAVOR) {
            throw UnsupportedFlavorException(flavor)
        }
        return data
    }
}