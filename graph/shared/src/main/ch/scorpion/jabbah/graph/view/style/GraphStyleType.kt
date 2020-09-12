package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.style.EditStyleType

/**
 * Defines [StyleType]s of the [ch.scorpion.jabbah.graph.view] package.
 */
class GraphStyleType(
	name: String,
	descriptionKey: String,
	isSystem: Boolean = false
) : EditStyleType(name, descriptionKey, isSystem) {

    companion object {
        val VERTICE = GraphStyleType("vertice", "graph.styleType.vertice.name")
        val EDGE = GraphStyleType("edge", "graph.styleType.edge.name")
        val ANNOTATION = GraphStyleType("annotation", "graph.styleType.annotation.name")
        val EXPLANATION = GraphStyleType("explanation", "graph.styleType.explanation.name")
        val SUBSYSTEM = GraphStyleType("subsystem", "graph.styleType.subsystem.name")
    }
}