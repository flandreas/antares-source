package ch.scorpion.jabbah.graph.ui

import ch.scorpion.jabbah.graph.model.GraphElement
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.graph.library.LibraryElement
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

/**
 * A drag&drop [Transferable] for [GraphElementView]s.
 */
class GraphElementViewTransferable(
    private val data: GraphElementViewTransferableData
) : Transferable {

    companion object {
        val FLAVOR = DataFlavor("${DataFlavor.javaJVMLocalObjectMimeType};class=\"${String::class.java.name}\"")
	    fun of(graphElementView: GraphElementView<GraphElement>, libraryElement: LibraryElement): GraphElementViewTransferable {
		    return GraphElementViewTransferable(GraphElementViewTransferableData(graphElementView, libraryElement))
	    }
    }

    /** ---- [Transferable] */

    override fun getTransferDataFlavors(): Array<DataFlavor> {
        return arrayOf(FLAVOR)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
        return flavor == FLAVOR
    }

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != FLAVOR) {
            throw UnsupportedFlavorException(flavor)
        }
        return data
    }
}

data class GraphElementViewTransferableData(
	val graphElementView: GraphElementView<GraphElement>,
	val libraryElement: LibraryElement
)