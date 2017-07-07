package ch.scorpion.jabbah.edit

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

/** Wraps a [Component] as a drag'n drop [Transferable]. */
class ComponentTransferable(val component: Component) : Transferable {

    companion object {
        // The second argument of the constructor is not relevant. Could just be anything.
        val FLAVOR = DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + String::class.java.name + "\"")
    }

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != FLAVOR) {
            throw UnsupportedFlavorException(flavor)
        }
        return component
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        return flavor == FLAVOR
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(FLAVOR)
    }
}