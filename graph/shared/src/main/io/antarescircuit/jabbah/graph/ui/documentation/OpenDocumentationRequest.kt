package io.antarescircuit.jabbah.graph.ui.documentation

import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.model.Document
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A request to open the documentation [Document] of a [SubGraphVerticeView]'s [MetaGraph].
 */
data class OpenDocumentationRequest(
    val drawingView: DrawingView<GraphElementView<*>, GraphView>,
    val subGraphVerticeView: SubGraphVerticeView<*>,
    val documentation: Document,
    val metaGraphName: String
)