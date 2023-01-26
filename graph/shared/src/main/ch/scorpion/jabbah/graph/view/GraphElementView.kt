package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement

/**
 * A selectable and editable [Component] representing the view of a model [GraphElement].
 * Implementing classes should detach from its model [GraphElement] in [dispose].
 * @param T the type of the model [GraphElement]
 */
interface GraphElementView<T : GraphElement> : Component {

    /** The model [GraphElement] that this [GraphElementView] displays.  */
    val model: T

	val isFullyConnected: Boolean

    /**
     * Called by the [GraphView] which contains this [GraphElementView] when the [GraphView] is built for a
     * particular [Graph]. Concrete classes implement this method if they need information from the [Graph]
     * other than the model [GraphElement] that they display.
     */
    fun bind(graph: Graph, deep: Boolean)
}