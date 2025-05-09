package ch.scorpion.jabbah.graph.ui.documentation

import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.graph.model.Document
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A request to open the documentation [Document] of a [SubGraphVerticeView]'s [MetaGraph].
 */
data class OpenDocumentationRequest(
    val drawingView: DrawingView<GraphView>,
    val subGraphVerticeView: SubGraphVerticeView<*>,
    val documentation: Document,
    val metaGraphName: String
)