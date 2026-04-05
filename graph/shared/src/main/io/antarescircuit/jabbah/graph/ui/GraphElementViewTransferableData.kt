package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.edit.app.ComponentCustomizer
import io.antarescircuit.jabbah.graph.library.LibraryElement
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.view.GraphElementView

/**
 * The data being transferred when dragging a [LibraryElement] from a library tree view
 * into a drawing canvas.
 *
 * @param graphElementView the [GraphElementView] being dragged
 * @param libraryElement the [LibraryElement] where dragging originated
 */
data class GraphElementViewTransferableData(
	val graphElementView: GraphElementView<GraphElement>,
	val libraryElement: LibraryElement,
	val customizer: ComponentCustomizer? = null
)