package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.GraphElement
import io.antarescircuit.jabbah.graph.model.param.GraphParamValue

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
     * Returns `true` if this [GraphElementView] has a property that depends on a [GraphParamValue],
     * and [graphParamsChanged] need to be called from outside whenever its values have changed.
     * This is a rare case. Normally, [GraphParamValue GraphParamValues] live on the model layer.
     */
    val hasGraphParameter: Boolean get() = false

    /**
     * Called by the [GraphView] which contains this [GraphElementView] when the [GraphView] is built for a
     * particular [Graph]. Concrete classes implement this method if they need information from the [GraphView]
     * other than the model [GraphElement] that they display.
     */
    fun bind(graphView: GraphView, deep: Boolean)

    /**
     * Notifies this [GraphElementView] that a [GraphParamValue] on which one of its properties might
     * depend has changed. Only called if [hasGraphParameter] returns `true`.
     */
    fun graphParamsChanged(graph: Graph) {}
}