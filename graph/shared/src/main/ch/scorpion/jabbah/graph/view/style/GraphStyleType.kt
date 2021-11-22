package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.style.EditStyleType

/**
 * Defines [StyleType]s of the [ch.scorpion.jabbah.graph.view] package.
 */
class GraphStyleType(
	name: String,
	descriptionKey: String,
	isSystem: Boolean = false,
	isBackdrop: Boolean = false
) : EditStyleType(name, descriptionKey, isSystem, isBackdrop) {

    companion object {
        val VERTICE = GraphStyleType("vertice", "graph.styleType.vertice.name")
        val EDGE = GraphStyleType("edge", "graph.styleType.edge.name")
        val EXPLANATION = GraphStyleType("explanation", "graph.styleType.explanation.name", isSystem = true)
        val SUBSYSTEM = GraphStyleType("subsystem", "graph.styleType.subsystem.name", isBackdrop = true)
    }
}