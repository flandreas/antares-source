package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.edit.app.ComponentCustomizer
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.library.LibraryElement
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
        val FLAVOR = DataFlavor("${DataFlavor.javaJVMLocalObjectMimeType};class=\"${GraphElementViewTransferable::class.java.name}\"")

	    fun of(
            graphElementView: GraphElementView<GraphElement>,
            libraryElement: LibraryElement,
            customizer: ComponentCustomizer? = null
        ): GraphElementViewTransferable {
		    return GraphElementViewTransferable(
                GraphElementViewTransferableData(graphElementView, libraryElement, customizer)
            )
	    }
    }

    /** ---- [Transferable] */

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(FLAVOR)

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = flavor == FLAVOR

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != FLAVOR) {
            throw UnsupportedFlavorException(flavor)
        }
        return data
    }
}