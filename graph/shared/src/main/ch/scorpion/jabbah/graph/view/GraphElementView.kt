package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphElement

/**
 * A selectable and editable [Component] representing the view of a model [GraphElement].
 * @param T the type of the model [GraphElement]
 */
interface GraphElementView<T : GraphElement> : Component {

    /** The model [GraphElement] that this [GraphElementView] displays. Can only be `null` during deserialization.*/
    val model: T?

    /** Disposes this [GraphElementView] by detaching it from its model [GraphElement].*/
    fun dispose()

    /**
     * Called by the [GraphView] which contains this [GraphElementView] when the [GraphView] is built for a
     * particular [Graph]. Concrete classes implement this method if they need information from the [Graph]
     * other than the model [GraphElement] that they display.
     */
    fun bind(graph: Graph)
}